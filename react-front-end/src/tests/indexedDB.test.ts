import { openDB, deleteDB } from 'idb';

import {
    initDB, getLastSynced, setLastSynced, Stores,
    processEvents,
    MoveEventGroup,
    processMoveRelatedEvents,
    removeDeletedProject
} from '@/features/offline/idb'

import { IDB_VERSION, TEST_IDB_NAME } from '@/utils/constants';

import { ResourceType, TimelineActionType } from '@/types/types';
import { Event } from '@/features/offline/eventTypes';
import { Project } from '@/features/projects/projectsTypes';
import { Item } from '@/features/items/itemTypes';
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes';
import { Room } from '@/features/rooms/roomsTypes';

beforeEach(async () => {
});

afterEach(async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION);
    await db.close();
    deleteDB(TEST_IDB_NAME);
});

test('initDB should initialize IndexedDB with correct stores', async () => {
    await initDB(true);
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION);

    expect(db.objectStoreNames).toContain(Stores.Projects);
    expect(db.objectStoreNames).toContain(Stores.Rooms);
    expect(db.objectStoreNames).toContain(Stores.OrgUnits);
    expect(db.objectStoreNames).toContain(Stores.Items);
    expect(db.objectStoreNames).toContain(Stores.Meta);
});

test('setLastSynced should update timestamp in IndexedDB', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.Meta, { keyPath: 'key' });
        },
    });

    const tx = db.transaction([Stores.Meta], 'readwrite');
    const timestamp = Date.now();
    await setLastSynced(timestamp, tx);
    await tx.done;

    const lastSynced = await getLastSynced(TEST_IDB_NAME);
    expect(lastSynced).toBe(timestamp);
});

test('processEvents should correctly process CREATE events', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
        },
    });
    const transaction = db.transaction([Stores.OrgUnits], 'readwrite');

    const createEvent: Event = {
        action: TimelineActionType.CREATE,
        details: JSON.stringify({ name: 'Test Org Unit', description: 'A shelf' }),
        entityId: 101,
        entityType: ResourceType.ORGANIZATIONAL_UNIT,
        timestamp: new Date(),
        userId: 1,
        userName: 'TestUser',
    };

    await processEvents([createEvent], transaction);
    await transaction.done;

    const storedEntity = await db.get(Stores.OrgUnits, 101);
    expect(storedEntity).toEqual(expect.objectContaining({ name: 'Test Org Unit', description: 'A shelf' }));
});

test('processEvents should correctly process UPDATE events', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION);
    const transaction = db.transaction([Stores.Items], 'readwrite');

    const initialItem = {
        id: 101,
        name: 'Test Item',
        description: 'Original description',
        tags: ['tool', 'metal'],
        quantity: 1,
    };

    await transaction.objectStore(Stores.Items).put(initialItem);
    await transaction.done;

    const updateEvent: Event = {
        action: TimelineActionType.UPDATE,
        details: JSON.stringify({ description: 'Updated description' }),
        entityId: 101,
        entityType: ResourceType.ITEM,
        timestamp: new Date(),
        userId: 1,
        userName: 'TestUser',
    };

    const updateTransaction = db.transaction([Stores.Items], 'readwrite');
    await processEvents([updateEvent], updateTransaction);
    await updateTransaction.done;

    const updatedItem = await db.get(Stores.Items, 101);
    expect(updatedItem).toEqual({
        id: 101,
        name: 'Test Item',
        description: 'Updated description',
        tags: ['tool', 'metal'],
        quantity: 1,
    });
});


test('processEvents should correctly process DELETE events', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION);
    const transaction = db.transaction([Stores.Items], 'readwrite');

    const initialItem = {
        id: 101,
        name: 'Test Item',
        description: 'Original description',
        tags: ['tool', 'metal'],
        quantity: 1,
    };

    await transaction.objectStore(Stores.Items).put(initialItem);
    await transaction.done;

    const updateEvent: Event = {
        action: TimelineActionType.DELETE,
        details: JSON.stringify({}),
        entityId: 101,
        entityType: ResourceType.ITEM,
        timestamp: new Date(),
        userId: 1,
        userName: 'TestUser',
    };

    const updateTransaction = db.transaction([Stores.Items], 'readwrite');
    await processEvents([updateEvent], updateTransaction);
    await updateTransaction.done;

    const deletedItem = await db.get(Stores.Items, 101);
    expect(deletedItem).toBeUndefined();
});

// TODO test on backend that deleting project works 
// TODO make sure that creating a child adds to the parent
// TODO make sure tht deleting a child adds to the parent
// TODO make sure that deleting a parent deletes their children

test('processEvents should correctly process events from adding a child to a parent', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.Items, { keyPath: 'id' });
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
        },
    });

    const transaction = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');

    // Initial orphaned item (not assigned to any org unit)
    const item = { id: 101, name: 'Test Item', orgUnitId: null };
    await transaction.objectStore(Stores.Items).put(item);

    // New parent Org Unit
    const orgUnit = { id: 200, name: 'Storage Room', itemIds: [] };
    await transaction.objectStore(Stores.OrgUnits).put(orgUnit);

    await transaction.done;

    const moveEvent: MoveEventGroup = {
        move: {
            action: TimelineActionType.MOVE,
            details: JSON.stringify({ previousParentId: null, newParentId: 200, parentType: ResourceType.ORGANIZATIONAL_UNIT }),
            entityId: 101,
            entityType: ResourceType.ITEM,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
        add: {
            action: TimelineActionType.ADD_CHILD,
            details: JSON.stringify({ childId: 101, childType: ResourceType.ITEM }),
            entityId: 200,
            entityType: ResourceType.ORGANIZATIONAL_UNIT,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
    };

    // Process move event
    const tx = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');
    await processMoveRelatedEvents(moveEvent, tx);
    await tx.done;

    // Verify that the item now belongs to the org unit
    const updatedItem = await db.get(Stores.Items, 101);
    expect(updatedItem.orgUnitId).toBe(200);

    const updatedOrgUnit = await db.get(Stores.OrgUnits, 200);
    expect(updatedOrgUnit.itemIds).toContain(101);
});

test('processEvents should correctly process events from removing a child from a parent', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.Items, { keyPath: 'id' });
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
        },
    });

    const transaction = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');

    // Item inside an org unit
    const item = { id: 102, name: 'Test Item', orgUnitId: 201 };
    await transaction.objectStore(Stores.Items).put(item);

    // Existing Org Unit with the item inside
    const orgUnit = { id: 201, name: 'Office Storage', itemIds: [102] };
    await transaction.objectStore(Stores.OrgUnits).put(orgUnit);

    await transaction.done;

    // Remove event
    const removeEvent: MoveEventGroup = {
        move: {
            action: TimelineActionType.MOVE,
            details: JSON.stringify({ previousParentId: 201, newParentId: null, parentType: ResourceType.ORGANIZATIONAL_UNIT }),
            entityId: 102,
            entityType: ResourceType.ITEM,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
        remove: {
            action: TimelineActionType.REMOVE_CHILD,
            details: JSON.stringify({ childId: 102, childType: ResourceType.ITEM }),
            entityId: 201,
            entityType: ResourceType.ORGANIZATIONAL_UNIT,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
    };

    // Process move event
    const tx = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');
    await processMoveRelatedEvents(removeEvent, tx);
    await tx.done;

    // Verify that the item is now orphaned
    const updatedItem = await db.get(Stores.Items, 102);
    expect(updatedItem.orgUnitId).toBeNull();

    // Verify that the org unit no longer lists the item
    const updatedOrgUnit = await db.get(Stores.OrgUnits, 201);
    expect(updatedOrgUnit.itemIds).not.toContain(102);
});

test('processEvents should correctly process events from moving a child from one parent to another', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.Items, { keyPath: 'id' });
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
        },
    });

    const transaction = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');

    // Item initially in Org Unit A
    const item = { id: 103, name: 'Test Item', orgUnitId: 202 };
    await transaction.objectStore(Stores.Items).put(item);

    // Org Unit A (original parent)
    const orgUnitA = { id: 202, name: 'Storage A', itemIds: [103] };
    await transaction.objectStore(Stores.OrgUnits).put(orgUnitA);

    // Org Unit B (new parent)
    const orgUnitB = { id: 203, name: 'Storage B', itemIds: [] };
    await transaction.objectStore(Stores.OrgUnits).put(orgUnitB);

    await transaction.done;

    // Move event from Org Unit A → Org Unit B
    const moveEvent: MoveEventGroup = {
        move: {
            action: TimelineActionType.MOVE,
            details: JSON.stringify({ previousParentId: 202, newParentId: 203, parentType: ResourceType.ORGANIZATIONAL_UNIT }),
            entityId: 103,
            entityType: ResourceType.ITEM,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
        remove: {
            action: TimelineActionType.REMOVE_CHILD,
            details: JSON.stringify({ childId: 103, childType: ResourceType.ITEM }),
            entityId: 202,
            entityType: ResourceType.ORGANIZATIONAL_UNIT,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
        add: {
            action: TimelineActionType.ADD_CHILD,
            details: JSON.stringify({ childId: 103, childType: ResourceType.ITEM }),
            entityId: 203,
            entityType: ResourceType.ORGANIZATIONAL_UNIT,
            timestamp: new Date(),
            userId: 1,
            userName: 'TestUser',
        },
    };

    // Process move event
    const tx = db.transaction([Stores.Items, Stores.OrgUnits], 'readwrite');
    await processMoveRelatedEvents(moveEvent, tx);
    await tx.done;

    // Verify that the item is now in Org Unit B
    const updatedItem = await db.get(Stores.Items, 103);
    expect(updatedItem.orgUnitId).toBe(203);

    // Verify that Org Unit A no longer has the item
    const updatedOrgUnitA = await db.get(Stores.OrgUnits, 202);
    expect(updatedOrgUnitA.itemIds).not.toContain(103);

    // Verify that Org Unit B now has the item
    const updatedOrgUnitB = await db.get(Stores.OrgUnits, 203);
    expect(updatedOrgUnitB.itemIds).toContain(103);
});

test('removeDeletedProject should remove a project and its associated data', async () => {
    const db = await openDB(TEST_IDB_NAME, IDB_VERSION, {
        upgrade(db) {
            db.createObjectStore(Stores.Projects, { keyPath: 'id' });
            db.createObjectStore(Stores.Rooms, { keyPath: 'id' });
            db.createObjectStore(Stores.OrgUnits, { keyPath: 'id' });
            db.createObjectStore(Stores.Items, { keyPath: 'id' });
        }
    });

    const testItem: Item = { id: 30, name: 'Test Item', projectId: 1, description: '', quantity: 1, tags: [] };
    const testOrgUnit: OrgUnit = { id: 20, name: 'Test Org Unit', projectId: 1, items: [testItem], itemIds: [30], description: '' };
    const testRoom: Room = { id: 10, name: 'Test Room', description: 'test', projectId: 1, orgUnitIds: [20], orgUnits: [testOrgUnit] };

    // Create test project with related entities
    const testProject: Project = {
        id: 1,
        name: 'Test Project',
        roomIds: [10],
        rooms: [testRoom],
        orgUnitIds: [20],
        itemIds: [30],
    };

    // Store entities in IndexedDB
    const transaction = db.transaction(Object.values(Stores), 'readwrite');
    await transaction.objectStore(Stores.Projects).put(testProject);
    await transaction.objectStore(Stores.Rooms).put(testRoom);
    await transaction.objectStore(Stores.OrgUnits).put(testOrgUnit);
    await transaction.objectStore(Stores.Items).put(testItem);
    await transaction.done;

    // Verify they exist before deletion
    expect(await db.get(Stores.Projects, 1)).toBeDefined();
    expect(await db.get(Stores.Rooms, 10)).toBeDefined();
    expect(await db.get(Stores.OrgUnits, 20)).toBeDefined();
    expect(await db.get(Stores.Items, 30)).toBeDefined();

    // Perform deletion
    const deleteTx = db.transaction(Object.values(Stores), 'readwrite');
    await removeDeletedProject(1);
    await deleteTx.done;

    // Verify everything is deleted
    expect(await db.get(Stores.Projects, 1)).toBeUndefined();
    expect(await db.get(Stores.Rooms, 10)).toBeUndefined();
    expect(await db.get(Stores.OrgUnits, 20)).toBeUndefined();
    expect(await db.get(Stores.Items, 30)).toBeUndefined();

    db.close();
});
