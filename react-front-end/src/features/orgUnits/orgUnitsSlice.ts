import { createAppSlice } from "@/hooks/useAppHooks";

export interface OrgUnitState {

}

const initialState: OrgUnitState = {

}

const orgUnitSlice = createAppSlice({
    name: 'orgUnits',
    initialState,
    reducers: {

    },
})

export default orgUnitSlice.reducer