export interface OrgUnit {
    id: number
    name: string
    description: string
    roomId?: number
    roomName?: string
}

export type NewOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { roomId: string }

export type NewUnassignedOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { projectId: string }

export type OrgUnitUpdate = Pick<OrgUnit, 'id' | 'name' | 'description'>

export interface OrgUnitsAssign {
    orgUnitIds: number[]
    roomId: number
}