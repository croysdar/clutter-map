import { createAppSlice } from "@/hooks/useAppHooks";

export interface RoomsState {

}

const initialState: RoomsState = {

}

export interface Room {
    id: number;
    name: string;
    description: string;
    locations: Location[] | null;
}

export interface NewRoom {
    name: string;
    description: string;
}

const roomsSlice = createAppSlice({
    name: 'rooms',
    initialState,
    reducers: {

    },
})

export default roomsSlice.reducer