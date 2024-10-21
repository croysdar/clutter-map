import { createAppSlice } from "@/hooks/useAppHooks";
import { OrgUnit } from '../orgUnits/orgUnitsSlice'

export interface RoomsState {

}

const initialState: RoomsState = {

}

export interface Room {
    id: number;
    name: string;
    description: string;
    orgUnits: OrgUnit[];
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