import { openDB } from "idb";

/* ------------- Types ------------- */
import { Item } from "@/features/items/itemTypes";
import { OrgUnit } from "@/features/orgUnits/orgUnitsTypes";
import { Project } from "@/features/projects/projectsTypes";
import { Room } from "@/features/rooms/roomsTypes";

/* ------------- Constants ------------- */
import { API_BASE_URL, IDB_VERSION } from "@/utils/constants";

import { client } from "@/services/client";

export enum Stores {
    Projects = 'projects',
    Rooms = 'rooms',
    OrgUnits = 'org_units',
    Items = 'items',
    Meta = 'meta',
}

export const initDB = (): Promise<boolean> => {
    return new Promise((resolve, reject) => {

        const request = indexedDB.open('ClutterMapDB', IDB_VERSION);

        let needsFullSync = false;

        request.onupgradeneeded = () => {
            const db = request.result;

            needsFullSync = true;

            // Create object stores if they don't exist
            if (!db.objectStoreNames.contains(Stores.Projects)) {
                db.createObjectStore(Stores.Projects, { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains(Stores.Rooms)) {
                db.createObjectStore(Stores.Rooms, { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains(Stores.OrgUnits)) {
                db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains(Stores.Items)) {
                db.createObjectStore(Stores.Items, { keyPath: 'id' });
            }
            if (!db.objectStoreNames.contains(Stores.Meta)) {
                db.createObjectStore(Stores.Meta, { keyPath: 'key' });
            }
        };

        request.onsuccess = () => {
            console.log('IndexedDB initialized');

            if (needsFullSync) {
                console.log("Triggering full sync due to IDB upgrade...");
                const syncUpgradedIDB = async () => {
                    const token = localStorage.getItem('jwt');
                    if (token) {
                        await fullSync(token);
                    }
                }
                syncUpgradedIDB();
            }

            resolve(true);
        };

        request.onerror = (event) => {
            console.error('IndexedDB initialization failed:', event);
            reject(false);
        };
    });
};

export const getLastSynced = async (): Promise<number | null> => {
    const db = await openDB('ClutterMapDB', IDB_VERSION);
    const transaction = db.transaction('meta', 'readonly');
    const store = transaction.objectStore('meta');

    const result = await store.get('last-synced');
    return result ? result.value : null;
};

export const setLastSynced = async (timestamp: number): Promise<void> => {
    const db = await openDB('ClutterMapDB', IDB_VERSION);
    const transaction = db.transaction('meta', 'readwrite');
    const store = transaction.objectStore('meta');

    await store.put({ key: 'last-synced', value: timestamp });
};

export const performSync = async (token: string) => {
    const lastSynced = await getLastSynced();
    const now = Date.now();
    const recent = 3 * 1000;

    if (lastSynced) {
        // Fetch updates since last sync
        if (now - lastSynced > recent) {
            console.log('Last-synced timestamp found. Fetching updates since last sync...');
            await partialSync(token, lastSynced);
        }
        else {
            console.log("Last-synced was too recent.")
        }
    }
    else {
        // Perform full sync for new devices
        console.log('No last-synced timestamp found. Performing full sync...');
        await fullSync(token);
    }
};

const partialSync = async (token: string, lastSynced: number) => {
    await fullSync(token);

}

const fullSync = async (token: string) => {
    let data = {
        'projects': {} as Record<number, Project>,
        'rooms': {} as Record<number, Room>,
        'orgUnits': {} as Record<number, OrgUnit>,
        'items': {} as Record<number, Item>
    };

    const [projectsResponse, roomsResponse, orgUnitsResponse, itemsResponse] = await Promise.all([
        client.get<Project[]>(`${API_BASE_URL}/projects`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<Room[]>(`${API_BASE_URL}/rooms`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<OrgUnit[]>(`${API_BASE_URL}/org-units`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<Item[]>(`${API_BASE_URL}/items`, { headers: { Authorization: `Bearer ${token}` } })
    ]);

    projectsResponse.data.forEach(project => (data.projects[project.id] = project));
    roomsResponse.data.forEach(room => (data.rooms[room.id] = room));
    orgUnitsResponse.data.forEach(orgUnit => (data.orgUnits[orgUnit.id] = orgUnit));
    itemsResponse.data.forEach(item => (data.items[item.id] = item));

    console.log('Data fetched:', data);

    const db = await openDB('ClutterMapDB', IDB_VERSION);

    const tx = db.transaction([Stores.Projects, Stores.Rooms, Stores.OrgUnits, Stores.Items, Stores.Meta], 'readwrite');

    const storeData = async (storeName: Stores, records: Record<number, any>) => {
        const store = tx.objectStore(storeName);
        await Promise.all(Object.values(records).map(record => store.put(record)));
        };

    await Promise.all([
        storeData(Stores.Projects, data.projects),
        storeData(Stores.Rooms, data.rooms),
        storeData(Stores.OrgUnits, data.orgUnits),
        storeData(Stores.Items, data.items),
        tx.objectStore(Stores.Meta).put({ key: 'last-synced', value: Date.now() })
    ]);


    await tx.done;

            console.log('Full sync completed.');

}