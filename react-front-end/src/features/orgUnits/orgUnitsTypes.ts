import { type Item } from "../items/itemTypes"

export interface OrgUnit {
    id: number
    name: string
    description: string
    roomId?: number
    roomName?: string
    items: Item[]
    itemIds: number[]
    projectId: number
}

export type NewOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { roomId: string }

export type NewUnassignedOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { projectId: string }

export type OrgUnitUpdate = Pick<OrgUnit, 'id' | 'name' | 'description'>

export interface OrgUnitsAssign {
    orgUnitIds: number[]
    roomId: number
}