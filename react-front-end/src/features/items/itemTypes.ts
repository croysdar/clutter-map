export interface Item {
    id: number;
    name: string;
    description: string;
    tags: string[]
    quantity: number;
    orgUnitId?: number,
    orgUnitName?: string,
}

export type NewItem = Pick<Item, 'name' | 'description' | 'tags' | 'quantity'> | { orgUnitId: string }

export type NewUnassignedItem = Pick<Item, 'name' | 'description' | 'tags' | 'quantity'> | { projectId: string }

export type ItemUpdate = Pick<Item, 'id' | 'name' | 'description' | 'tags' | 'quantity'>

export interface ItemsAssign {
    itemIds: number[],
    orgUnitId: number
}