import { createAppSlice } from "@/hooks/useAppHooks";
import { Project } from "../projects/projectsTypes";

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

export type NewRoom = Pick<Room, 'name' | 'description'> | {projectId: string}

export type RoomUpdate = Pick<Room, 'id' | 'name' | 'description'>

const roomsSlice = createAppSlice({
    name: 'rooms',
    initialState,
    reducers: {

    },
})

export default roomsSlice.reducer