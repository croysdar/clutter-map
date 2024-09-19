import { createAppSlice } from "@/hooks/useAppHooks";

export interface RoomsState {

}

const initialState: RoomsState = {

}

const roomsSlice = createAppSlice({
    name: 'rooms',
    initialState,
    reducers: {

    },
})

export default roomsSlice.reducer