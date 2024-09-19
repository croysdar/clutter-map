import { createAppSlice } from "@/hooks/useAppHooks";

export interface ItemsState {

}

const initialState: ItemsState = {

}

const itemsSlice = createAppSlice({
    name: 'items',
    initialState,
    reducers: {

    },
})

export default itemsSlice.reducer