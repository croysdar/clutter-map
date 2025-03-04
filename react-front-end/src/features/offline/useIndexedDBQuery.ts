import { IDB_VERSION } from '@/utils/constants';
import { openDB } from 'idb';

/**
 * Fetch all records from an IndexedDB store.
 * 
 * @template T - The expected type of the stored entities.
 * @param {string} storeName - The name of the IndexedDB store to retrieve records from.
 * @returns {Promise<{ data?: T[]; error?: { status: number; data: { message: string } } }>} 
 *          A promise resolving to an array of items if successful, or an error object if retrieval fails.
 *
 * @example
 * const { data, error } = await getAllFromIndexedDB<Item>('items');
 * if (data) {
 *    console.log('Items:', data);
 * } else {
 *    console.error('Error fetching items:', error);
 * }
 */
export const getAllFromIndexedDB = async <T>(storeName: string): Promise<{ data?: T[]; error?: any }> => {
    try {
        const db = await openDB('ClutterMapDB', IDB_VERSION);
        const transaction = db.transaction(storeName, 'readonly');
        const store = transaction.objectStore(storeName);
        const data = await store.getAll();

        return { data };
    }
    catch (error) {
        console.error(`Failed to fetch from IndexedDB store: ${storeName}`, error);
        return { error: { status: 500, data: { message: `Failed to fetch from ${storeName}` } } };
    }
};


/**
 * Retrieves a single entity from IndexedDB by its ID.
 *
 * @template T - The type of the entity being retrieved.
 * @param {string} storeName - The name of the IndexedDB object store.
 * @param {number} id - The ID of the entity to retrieve.
 * @returns {Promise<{ data?: T; error?: { status: number; data: { message: string } } }>}
 *          A promise resolving to the entity if found, or an error object if not.
 *
 * @example
 * const { data, error } = await getByIdFromIndexedDB<Item>('items', 1);
 * if (data) {
 *    console.log('Item found:', data);
 * } else {
 *    console.error('Error:', error);
 * }
 */
export const getByIdFromIndexedDB = async <T>(storeName: string, id: number): Promise<{ data?: T; error?: any }> => {
    try {
        const db = await openDB('ClutterMapDB', IDB_VERSION);
        const transaction = db.transaction(storeName, 'readonly');
        const store = transaction.objectStore(storeName);
        const data = await store.get(id);

        if (!data) {
            return {
                error: {
                    status: 404,
                    data: { message: `${id} in ${storeName} not found` },
                },
            }
        }

        return { data }
    }
    catch (error) {
        console.error(`Failed to fetch from IndexedDB store: ${storeName}`, error);
        return { error: { status: 500, data: { message: `Failed to fetch from ${storeName}` } } };
    }
};

/**
 * Fetch related entities from IndexedDB given a parent store, entity ID, related store, and property key.
 *
 * @template P - Parent entity type (e.g., Project, OrgUnit).
 * @template R - Related entity type (e.g., Item, OrgUnit).
 * @param {string} parentStore - The IndexedDB store where the parent entity is stored.
 * @param {number} parentId - The ID of the parent entity.
 * @param {string} relatedStore - The IndexedDB store that contains the related entities.
 * @param {keyof P} relatedKey - The property name in the parent entity that stores related entity IDs.
 * @returns {Promise<{ data: R[] } | { error: { status: number; data: { message: string } } }>}
 *          A promise resolving to an array of related entities if successful, or an error object if not.
 *
 * @example
 * const { data, error } = await getRelatedEntities<Project, Item>(
 *    'projects',
 *    1,
 *    'items',
 *    'itemIds'
 * );
 * if (data) {
 *    console.log('Related items:', data);
 * } else {
 *    console.error('Error fetching related items:', error);
 * }
 */
export const getRelatedEntities = async <P, R>(
    parentStore: string,
    parentId: number,
    relatedStore: string,
    relatedKey: keyof P
): Promise<{ data: R[] } | { error: { status: number, data: { message: string } } }> => {
    try {
        const db = await openDB('ClutterMapDB', IDB_VERSION);

        // Fetch parent entity
        const parentEntity = await db.get(parentStore, parentId);
        if (!parentEntity) {
            return { error: { status: 404, data: { message: `${parentStore} with ID ${parentId} not found` } } };
        }

        // Extract related entity IDs
        const relatedIds = (parentEntity[relatedKey] as unknown as number[]) || [];

        // Fetch related entities by their IDs
        const relatedEntities = (await Promise.all(relatedIds.map((id) => db.get(relatedStore, id)))).filter(Boolean) as R[];

        return { data: relatedEntities };
    } catch (error) {
        console.error(`Failed to fetch related entities from ${parentStore}:`, error);
        return { error: { status: 500, data: { message: `Failed to fetch related entities from ${parentStore}` } } };
    }
};