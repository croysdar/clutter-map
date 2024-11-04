export interface Item {
    id: number;
    name: string;
    description: string;
    tags: string[]
}

export type NewItem = Pick<Item, 'name' | 'description' | 'tags'> | { orgUnitId: string }

export type ItemUpdate = Pick<Item, 'id' | 'name' | 'description' | 'tags'>