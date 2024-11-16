import { Room } from '../rooms/roomsTypes'

export interface Project {
    id: number;
    name: string;
    rooms: Room[];
}

export type NewProject = Pick<Project, 'name'>

export type ProjectUpdate = Pick<Project, 'id' | 'name'>