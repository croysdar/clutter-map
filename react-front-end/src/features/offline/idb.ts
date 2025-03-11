import { IDBPObjectStore, IDBPTransaction, openDB } from "idb";

/* ------------- Types ------------- */
import { Item } from "@/features/items/itemTypes";
import { OrgUnit } from "@/features/orgUnits/orgUnitsTypes";
import { Project } from "@/features/projects/projectsTypes";
import { Room } from "@/features/rooms/roomsTypes";
import { ResourceType, TimelineActionType } from "@/types/types";
import { Event } from "./eventTypes";

/* ------------- Constants ------------- */
import { API_BASE_URL, IDB_NAME, IDB_VERSION } from "@/utils/constants";

import { client } from "@/services/client";

/* ------------- Enums & Interfaces ------------- */

export enum Stores {
    Projects = 'projects',
    Rooms = 'rooms',
    OrgUnits = 'org_units',
    Items = 'items',
    Meta = 'meta',
}

export interface MoveEventGroup {
    move?: Event
    remove?: Event
    add?: Event
}

/* ------------- IndexedDB Initialization & Syncing ------------- */

export const initDB = async (testMode = false) => {
    const dbName = testMode ? 'ClutterMapDB_Test' : IDB_NAME;

    await openDB(dbName, IDB_VERSION, {
        upgrade(db, oldVersion, newVersion, transaction) {
            console.log(`Upgrading IndexedDB from v${oldVersion} to v${newVersion}`);

            // Remove existing stores to force a fresh sync
            Array.from(db.objectStoreNames).forEach((storeName) => {
                db.deleteObjectStore(storeName);
            });

            db.createObjectStore(Stores.Projects, { keyPath: 'id' });
            db.createObjectStore(Stores.Rooms, { keyPath: 'id' });
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
            db.createObjectStore(Stores.Items, { keyPath: 'id' });
            db.createObjectStore(Stores.Meta, { keyPath: 'key' });
        }
    });

    return true;
};

export const performSync = async (token: string) => {
    const lastSynced = await getLastSynced(IDB_NAME);
    const now = Date.now();
    const recent = 3 * 1000; // Date is measured in milliseconds

    if (lastSynced && now - lastSynced > recent) {
        console.log('Last-synced timestamp found. Fetching updates since last sync...');
        await partialSync(token, lastSynced);
    }
    else if (!lastSynced) {
        console.log('No last-synced timestamp found. Performing full sync...');
        await fullSync(token);
    }
    else {
        console.log("Last-synced was too recent.")
    }
};

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

    const db = await openDB(IDB_NAME, IDB_VERSION);

    const tx = db.transaction(Object.values(Stores), 'readwrite');

    const storeData = async (storeName: Stores, records: Record<number, any>) => {
        const store = tx.objectStore(storeName);
        await Promise.all(Object.values(records).map(record => store.put(record)));
    };

    await Promise.all([
        storeData(Stores.Projects, data.projects),
        storeData(Stores.Rooms, data.rooms),
        storeData(Stores.OrgUnits, data.orgUnits),
        storeData(Stores.Items, data.items),
        setLastSynced(Date.now(), tx)
    ]);

    await tx.done;

    console.log('Full sync completed.');
}

interface SyncResponse {
    projects: number[]
    events: Event[]
}

const partialSync = async (token: string, lastSynced: number) => {
    const response = await client.get<SyncResponse>(`${API_BASE_URL}/fetch-updates?since=${lastSynced}`, { headers: { Authorization: `Bearer ${token}` } });
    const events: Event[] = response.data.events;

    const projectIDs: number[] = response.data.projects;

    await syncProjectList(token, projectIDs);

    if (events.length === 0) {
        console.log("No updates found.");
        return;
    }

    const db = await openDB(IDB_NAME, IDB_VERSION);
    const transaction = db.transaction(Object.values(Stores), "readwrite");

    try {
        await processEvents(events, transaction);

        const now = Date.now();
        setLastSynced(now, transaction);

        await transaction.done;
        console.log(`Sync completed. Last synced at: ${new Date(now).toISOString()}`);
    }
    catch (error) {
        // TODO add retry logic
        console.error("Error during sync. Rolling back transaction.", error);
        transaction.abort();
    }
}

const syncProjectList = async (token: string, projectList: number[]) => {
    const db = await openDB(IDB_NAME, IDB_VERSION);
    const transaction = db.transaction(Object.values(Stores), "readwrite");
    const store = transaction.objectStore(Stores.Projects);

    const storedProjectIDs = await store.getAllKeys() as number[];

    const projectSet = new Set(projectList);
    const storedSet = new Set(storedProjectIDs);

    // Check to see if any projects in the idb are not in the projectList
    for (const projectID of storedProjectIDs) {
        if (!projectSet.has(projectID)) {
            console.log(`Project ${projectID} is not in the server, deleting project`)
            removeDeletedProject(projectID, transaction);
        }
    }

    // Check to see if any projects in the projectList are not in the idb
    for (const projectID of projectList) {
        if (!storedSet.has(projectID)) {
            console.log(`New project found on server : project ${projectID}. Downloading...`)
            downloadNewProject(token, projectID, transaction);
        }
    }
}

export const removeDeletedProject = async (projectID: number, transaction: IDBPTransaction<any, Stores[], "readwrite">) => {
    // Go through all the child id lists
    const projectStore = transaction.objectStore(Stores.Projects);
    const project: Project = await projectStore.get(projectID);

    if (!project) {
        console.warn(`Attempted delete of project ${projectID} failed. Project does not exist in IndexedDB.`)
        return;
    }

    // remove all items
    const itemStore = transaction.objectStore(Stores.Items);
    project.itemIds.forEach(async (itemID) => {
        await itemStore.delete(itemID);
    })

    // remove all org units
    const orgUnitStore = transaction.objectStore(Stores.OrgUnits);
    project.orgUnitIds.forEach(async (orgUnitID) => {
        await orgUnitStore.delete(orgUnitID);
    })

    // remove all rooms
    const roomStore = transaction.objectStore(Stores.Rooms);
    project.roomIds.forEach(async (roomID) => {
        await roomStore.delete(roomID);
    })

    // remove the project
    await projectStore.delete(projectID);
}

const downloadNewProject = async (token: string, projectID: number, transaction: IDBPTransaction<any, Stores[], "readwrite">) => {
    // Pull project and children from api
    const [projectResponse, roomsResponse, orgUnitsResponse, itemsResponse] = await Promise.all([
        client.get<Project>(`${API_BASE_URL}/project/${projectID}`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<Room[]>(`${API_BASE_URL}/project/${projectID}/rooms`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<OrgUnit[]>(`${API_BASE_URL}/project/${projectID}/org-units`, { headers: { Authorization: `Bearer ${token}` } }),
        client.get<Item[]>(`${API_BASE_URL}/project/${projectID}/items`, { headers: { Authorization: `Bearer ${token}` } })
    ]);

    let data = {
        'projects': {} as Record<number, Project>,
        'rooms': {} as Record<number, Room>,
        'orgUnits': {} as Record<number, OrgUnit>,
        'items': {} as Record<number, Item>
    };

    data.projects[projectID] = projectResponse.data;
    roomsResponse.data.forEach(room => (data.rooms[room.id] = room));
    orgUnitsResponse.data.forEach(orgUnit => (data.orgUnits[orgUnit.id] = orgUnit));
    itemsResponse.data.forEach(item => (data.items[item.id] = item));

    const storeData = async (storeName: Stores, records: Record<number, any>) => {
        const store = transaction.objectStore(storeName);
        await Promise.all(Object.values(records).map(record => store.put(record)));
    };

    await Promise.all([
        storeData(Stores.Projects, data.projects),
        storeData(Stores.Rooms, data.rooms),
        storeData(Stores.OrgUnits, data.orgUnits),
        storeData(Stores.Items, data.items),
    ]);
}

/* ------------- Event Processing ------------- */

export async function processEvents(events: Event[], transaction: IDBPTransaction<any, Stores[], "readwrite">) {
    const eventBuffer: Map<string, MoveEventGroup> = new Map();

    for (let event of events) {
        const eventKey = getEventKey(event.entityType, event.entityId);

        if (event.action === TimelineActionType.MOVE) {
            // init eventKey in eventBuffer
            if (!eventBuffer.has(eventKey)) {
                eventBuffer.set(eventKey, {});
            }
            eventBuffer.get(eventKey)!.move = event;
        }

        else if (event.action === TimelineActionType.REMOVE_CHILD || event.action === TimelineActionType.ADD_CHILD) {
            const { childId, childType } = JSON.parse(event.details);
            if (childId && childType) {
                const childEventKey = getEventKey(childType, childId);

                if (!eventBuffer.has(childEventKey)) {
                    eventBuffer.set(childEventKey, {});
                }

                if (event.action === TimelineActionType.REMOVE_CHILD) {
                    eventBuffer.get(childEventKey)!.remove = event;
                } else {
                    eventBuffer.get(childEventKey)!.add = event;
                }
            }
        } else {
            // Process other events immediately
            await processStandardEvent(event, transaction);
        }
    }

    // Process MOVE-related events
    for (const [_, groupedEvents] of Array.from(eventBuffer.entries())) {
        await processMoveRelatedEvents(groupedEvents, transaction);
    }
}

export async function processMoveRelatedEvents(
    { move, remove, add }: MoveEventGroup,
    transaction: IDBPTransaction<any, Stores[], "readwrite">
) {
    try {
        // if move, then we are changing the parent key of a child
        if (move) {
            const { parentType, newParentId } = JSON.parse(move.details);
            const childStore = transaction.objectStore(getStoreName(move.entityType));
            const childEntity = await childStore.get(move.entityId);
            if (!childEntity) {
                console.warn(`Skipping MOVE: Child entity ${move.entityType}-${move.entityId} not found.`);
                return;
            }
            const parentKey = getParentKeyForType(parentType);
            if (!parentKey) {
                console.warn(`Skipping MOVE: Could not determine parent key for ${parentType}`);
                return;
            }

            childEntity[parentKey] = newParentId || null;
            await childStore.put(childEntity);
            console.log(`Processed MOVE event for ${move.entityType}-${move.entityId}, new ${parentKey}: ${newParentId}`);
        }

        // Handle orphaning
        if (remove) {
            const oldParentStore = transaction.objectStore(getStoreName(remove.entityType));
            const oldParent = await oldParentStore.get(remove.entityId);

            const { childType, childId } = JSON.parse(remove.details);

            if (oldParent) {
                const childTypeListKey = getChildTypeListKeyForType(childType);
                if (childTypeListKey && Array.isArray(oldParent[childTypeListKey])) {
                    oldParent[childTypeListKey] = oldParent[childTypeListKey].filter((id: number) => id !== childId);
                    await oldParentStore.put(oldParent);
                }
            }
            console.log(`Processed REMOVE_CHILD event: removed ${childType}-${childId}, from ${remove.entityType}: ${remove.entityId}`);
        }

        // Handle adoption
        if (add) {
            const newParentStore = transaction.objectStore(getStoreName(add.entityType));
            const newParent = await newParentStore.get(add.entityId);

            const { childType, childId } = JSON.parse(add.details);

            if (newParent) {
                const childTypeListKey = getChildTypeListKeyForType(childType);
                if (childTypeListKey) {
                    if (!Array.isArray(newParent[childTypeListKey])) {
                        newParent[childTypeListKey] = [];
                    }

                    // Prevent duplicate childId
                    if (!newParent[childTypeListKey].includes(childId)) {
                        newParent[childTypeListKey].push(childId);
                        await newParentStore.put(newParent);
                    } else {
                        console.warn(`Skipping ADD_CHILD: ${childId} already exists in ${childTypeListKey} of parent ${add.entityId}`);
                    }
                }
            }
            console.log(`Processed ADD_CHILD event: added ${childType}-${childId}, to ${add.entityType}: ${add.entityId}`);
        }
    } catch (error) {
        console.error(`Error processing MOVE-related events`, error);
    }
}

async function processStandardEvent(event: Event, transaction: IDBPTransaction<any, Stores[], "readwrite">) {
    try {
        const storeName = getStoreName(event.entityType);

        const store = transaction.objectStore(storeName);
        if (!store) {
            console.warn(`Store not found: ${storeName}`);
            return;
        }

        switch (event.action) {
            case TimelineActionType.CREATE:
                await processCreateEvent(store, event);
                break;
            case TimelineActionType.UPDATE:
                await processUpdateEvent(store, event);
                break;
            case TimelineActionType.DELETE:
                await processDeleteEvent(store, event.entityId);
                break;
            default:
                console.warn(`Unhandled event action: ${event.action}`);
        }
    } catch (error) {
        console.error(`Error processing event for ${event.entityType}:`, error);
    }
}

async function processCreateEvent(store: IDBPObjectStore<any, any, any, "readwrite">, event: Event) {
    try {
        const entity = await store.get(event.entityId);
        if (entity) {
            console.warn(`Entity with ID ${entity.id} already exists in store '${store.name}'. Skipping creation.`);
            return;
        }

        const newEntity = JSON.parse(event.details);
        newEntity.id = event.entityId;

        await store.put(newEntity);
        console.log(`Created new ${event.entityType}:`, newEntity);
    } catch (error) {
        console.error("Error processing CREATE event:", error);
    }
}

async function processUpdateEvent(store: IDBPObjectStore<any, any, any, "readwrite">, event: Event) {
    try {
        const entity = await store.get(event.entityId);
        if (!entity) {
            console.warn(`Entity not found for UPDATE: ${event.entityId}`);
            // TODO log this in Meta as it likely means a full sync is necessary
            return;
        }

        const updates = JSON.parse(event.details);
        Object.assign(entity, updates);
        await store.put(entity);

        console.log(`Updated ${event.entityType} (ID: ${event.entityId}):`, updates);
    } catch (error) {
        console.error("Error processing UPDATE event:", error);
    }
}

async function processDeleteEvent(store: IDBPObjectStore<any, any, any, "readwrite">, entityId: number) {
    try {
        const entity = await store.get(entityId);
        if (!entity) {
            console.warn(`Entity not found for DELETE: ${entityId}`);
            return;
        }
        await store.delete(entityId);
        console.log(`Deleted entity ID ${entityId}`);
    } catch (error) {
        console.error("Error processing DELETE event:", error);
    }
}

/* ------------- Sync Metadata Handling ------------- */

export const getLastSynced = async (IDB_NAME: string): Promise<number | null> => {
    const db = await openDB(IDB_NAME, IDB_VERSION);
    return (await db.get(Stores.Meta, 'last-synced'))?.value || null;
}

export const setLastSynced = async (timestamp: number, transaction: IDBPTransaction<any, Stores[], "readwrite">): Promise<void> => {
    const store = transaction.objectStore(Stores.Meta);
    await store.put({ key: 'last-synced', value: timestamp });
}

/* ------------- Helper Functions ------------- */

function getStoreName(resourceType: ResourceType): Stores {
    switch (resourceType) {
        case ResourceType.PROJECT:
            return Stores.Projects;
        case ResourceType.ROOM:
            return Stores.Rooms;
        case ResourceType.ORGANIZATIONAL_UNIT:
            return Stores.OrgUnits;
        case ResourceType.ITEM:
            return Stores.Items;
        default:
            throw new Error(`Unsupported ResourceType: ${resourceType}`);
    }
}

function getParentKeyForType(parentType: ResourceType): string | null {
    switch (parentType) {
        case ResourceType.ROOM:
            return "roomId";
        case ResourceType.ORGANIZATIONAL_UNIT:
            return "orgUnitId";
        case ResourceType.PROJECT:
            return "projectId";
        default:
            console.warn(`Unknown parentType: ${parentType}`);
            return null;
    }
}

function getChildTypeListKeyForType(childType: ResourceType): string | null {
    switch (childType) {
        case ResourceType.ROOM:
            return "roomIds";
        case ResourceType.ORGANIZATIONAL_UNIT:
            return "orgUnitIds";
        case ResourceType.ITEM:
            return "itemIds";
        default:
            console.warn(`Unknown childType: ${childType}`);
            return null;
    }
}

function getEventKey(entityType: ResourceType, entityId: number): string {
    return `${entityType}-${entityId}`;
}
