export interface Item {
    id: number;
    name: string;
    description: string;
    tags: string[]
    quantity: number;
}

export type NewItem = Pick<Item, 'name' | 'description' | 'tags' | 'quantity'> | { orgUnitId: string }

export type ItemUpdate = Pick<Item, 'id' | 'name' | 'description' | 'tags' | 'quantity'>