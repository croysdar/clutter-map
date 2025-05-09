import { useEffect, useState } from 'react';

import { getOrInitDB, Stores } from '@/features/offline/idb';

import { Item } from '@/features/items/itemTypes';
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes';
import { Room } from '@/features/rooms/roomsTypes';
import { Project } from '@/features/projects/projectsTypes';

interface EntityHierarchy {
    project?: Project;
    room?: Room;
    orgUnit?: OrgUnit;
    item?: Item;
}

export const useEntityHierarchy = (entityType: 'item' | 'orgUnit', entityId: number) => {
    const [hierarchy, setHierarchy] = useState<EntityHierarchy>({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<Error | null>(null);

    useEffect(() => {
        if (entityId === -1) {
            setLoading(false);
            return;
        }
        const fetchHierarchy = async () => {
            try {
                const db = await getOrInitDB();
                const hierarchy: EntityHierarchy = {};

                if (entityType === 'item') {
                    // Get the item
                    const item = await db.get(Stores.Items, entityId);
                    if (!item) throw new Error('Item not found');
                    hierarchy.item = item;

                    // Get the project
                    const project = await db.get(Stores.Projects, item.projectId);
                    if (project) hierarchy.project = project;

                    // If item has an org unit, get it
                    if (item.orgUnitId) {
                        const orgUnit = await db.get(Stores.OrgUnits, item.orgUnitId);
                        if (orgUnit) {
                            hierarchy.orgUnit = orgUnit;
                            // If org unit has a room, get it
                            if (orgUnit.roomId) {
                                const room = await db.get(Stores.Rooms, orgUnit.roomId);
                                if (room) {
                                    hierarchy.room = room;
                                }
                            }
                        }
                    }
                } else if (entityType === 'orgUnit') {
                    // Get the org unit
                    const orgUnit = await db.get(Stores.OrgUnits, entityId);
                    if (!orgUnit) throw new Error('Org Unit not found');
                    hierarchy.orgUnit = orgUnit;

                    // Get the project
                    const project = await db.get(Stores.Projects, orgUnit.projectId);
                    if (project) hierarchy.project = project;

                    // If org unit has a room, get it
                    if (orgUnit.roomId) {
                        const room = await db.get(Stores.Rooms, orgUnit.roomId);
                        if (room) {
                            hierarchy.room = room;
                        }
                    }
                }

                setHierarchy(hierarchy);
                setLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err : new Error('Failed to fetch hierarchy'));
                setLoading(false);
            }
        };

        fetchHierarchy();
    }, [entityType, entityId]);

    return { hierarchy, loading, error };
}; 