import { type OrgUnit } from '../orgUnits/orgUnitsTypes';
export interface Room {
    id: number;
    name: string;
    description: string;
    orgUnits: OrgUnit[];
    orgUnitIds: number[];
    projectId: number;
}

export type NewRoom = Pick<Room, 'name' | 'description'> | { projectId: string }

export type RoomUpdate = Pick<Room, 'id' | 'name' | 'description'>
