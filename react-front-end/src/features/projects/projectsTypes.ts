import { type Room } from '../rooms/roomsTypes'

export interface Project {
    id: number
    name: string
    rooms: Room[]
    roomIds: number[]
    orgUnitIds: number[]
    itemIds: number[]
}

export type NewProject = Pick<Project, 'name'>

export type ProjectUpdate = Pick<Project, 'id' | 'name'>