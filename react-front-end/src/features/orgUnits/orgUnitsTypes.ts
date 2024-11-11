export interface OrgUnit {
    id: number;
    name: string;
    description: string;
}

export type NewOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { roomId: string }

export type OrgUnitUpdate = Pick<OrgUnit, 'id' | 'name' | 'description'>
