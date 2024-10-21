import { createAppSlice } from "@/hooks/useAppHooks";

export interface ItemsState {

}

const initialState: ItemsState = {

}

export interface Item {
    id: number;
    name: string;
    description: string;
}

export type NewItem = Pick<Item, 'name' | 'description'> | {orgUnitId: string}

export type ItemUpdate = Pick<Item, 'id' | 'name' | 'description'>

const itemsSlice = createAppSlice({
    name: 'items',
    initialState,
    reducers: {

    },
})

export default itemsSlice.reducer