import { createAppSlice } from "@/hooks/useAppHooks";

export interface OrgUnitsState {

}

const initialState: OrgUnitsState = {

}

export interface OrgUnit {
    id: number;
    name: string;
    description: string;
}

export type NewOrgUnit = Pick<OrgUnit, 'name' | 'description'> | { roomId: string }

export type OrgUnitUpdate = Pick<OrgUnit, 'id' | 'name' | 'description'>

const orgUnitsSlice = createAppSlice({
    name: 'orgUnits',
    initialState,
    reducers: {

    },
})

export default orgUnitsSlice.reducer