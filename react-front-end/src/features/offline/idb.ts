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

        request.onupgradeneeded = () => {
            const db = request.result;

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
    const oneMinute = 60 * 1000;

    if (lastSynced) {
        // Fetch updates since last sync
        if (now - lastSynced > oneMinute) {
            console.log('Last-synced timestamp found. Fetching updates since last sync...');
            partialSync(token, lastSynced);
        }
        else {
            console.log("Last-synced was less than a minute ago.")
        }
    }
    else {
        // Perform full sync for new devices
        console.log('No last-synced timestamp found. Performing full sync...');
        fullSync(token);
    }
};

const partialSync = async (token: string, lastSynced: number) => {


}

const fullSync = async (token: string) => {
    let data = {
        'projects': {} as Record<number, Project>,
        'rooms': {} as Record<number, Room>,
        'orgUnits': {} as Record<number, OrgUnit>,
        'items': {} as Record<number, Item>
    };

    // GET all projects
    const projectsResponse = await client.get<Project[]>(`${API_BASE_URL}/projects`, {
        headers: { Authorization: `Bearer ${token}` }
    });

    // Go through all projects
    for (let project of projectsResponse.data) {
        // Save project in data
        let projectID = project.id;
        data.projects[projectID] = project;
    }

    // GET all rooms
    const roomsResponse = await client.get<Room[]>(`${API_BASE_URL}/rooms`, {
        headers: { Authorization: `Bearer ${token}` }
    });

    // Go through all rooms
    for (let room of roomsResponse.data) {
        // Save room in data
        let roomID = room.id;
        data.rooms[roomID] = room;
    }

    // GET all orgUnits
    const orgUnitsResponse = await client.get<OrgUnit[]>(`${API_BASE_URL}/org-units`, {
        headers: { Authorization: `Bearer ${token}` }
    });

    // Go through all orgUnits
    for (let orgUnit of orgUnitsResponse.data) {
        // Save orgUnits in data
        let orgUnitID = orgUnit.id;
        data.orgUnits[orgUnitID] = orgUnit;
    }

    // GET all items
    const itemsResponse = await client.get<Item[]>(`${API_BASE_URL}/items`, {
        headers: { Authorization: `Bearer ${token}` }
    });

    // Go through all items
    for (let item of itemsResponse.data) {
        // Save items in data
        let itemID = item.id;
        data.items[itemID] = item;
    }

    console.log(data);

    const dbRequest = indexedDB.open('ClutterMapDB', version);

    dbRequest.onsuccess = async () => {
        const db = dbRequest.result;

        // Start a readwrite transaction
        const transaction = db.transaction(
            ['projects', 'rooms', 'org_units', 'items', 'meta'],
            'readwrite'
        );

        // Get object stores
        const projectsStore = transaction.objectStore('projects');
        const roomsStore = transaction.objectStore('rooms');
        const orgUnitsStore = transaction.objectStore('org_units');
        const itemsStore = transaction.objectStore('items');
        const metaStore = transaction.objectStore('meta');

        // Helper function to store data safely
        const storeData = (store: IDBObjectStore, records: Record<number, any>) => {
            Object.values(records).forEach(record => store.put(record));
        };

        // Store the fetched data
        storeData(projectsStore, data.projects);
        storeData(roomsStore, data.rooms);
        storeData(orgUnitsStore, data.orgUnits);
        storeData(itemsStore, data.items);

        // Store last-synced timestamp
        metaStore.put({ key: 'last-synced', value: Date.now() });

        // Wait for transaction to complete
        transaction.oncomplete = () => {
            console.log('Full sync completed.');
        };

        transaction.onerror = (event) => {
            console.error('Transaction failed:', event);
        };
    };
}