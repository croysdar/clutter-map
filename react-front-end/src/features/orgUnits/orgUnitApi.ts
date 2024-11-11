import { baseApiSlice } from "@/features/api/baseApiSlice";
import { NewOrgUnit, OrgUnit, OrgUnitUpdate } from "./orgUnitsTypes";

export const orgUnitApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        getOrgUnits: builder.query<OrgUnit[], void>({
            query: () => '/org-units',
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByRoom: builder.query<OrgUnit[], string>({
            query: (roomID) => `/rooms/${roomID}/org-units`,
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnit: builder.query<OrgUnit, string>({
            query: (orgUnitId) => `/org-units/${orgUnitId}`,
            providesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg }]
        }),

        updateOrgUnit: builder.mutation<OrgUnit, OrgUnitUpdate>({
            query: orgUnit => ({
                url: `/org-units/${orgUnit.id}`,
                method: 'PUT',
                body: orgUnit
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg.id }]
        }),

        deleteOrgUnit: builder.mutation<{ success: boolean, id: number }, number>({
            query: orgUnitId => ({
                url: `/org-units/${orgUnitId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'OrgUnit', id }]
        }),

        addNewOrgUnit: builder.mutation<OrgUnit, NewOrgUnit>({
            query: initialOrgUnit => ({
                url: '/org-units',
                method: 'POST',
                body: initialOrgUnit
            }),
            invalidatesTags: ['OrgUnit']
        }),
    })
})

export const {
    useGetOrgUnitsQuery,
    useGetOrgUnitsByRoomQuery,
    useGetOrgUnitQuery,
    useUpdateOrgUnitMutation,
    useAddNewOrgUnitMutation,
    useDeleteOrgUnitMutation,
} = orgUnitApi;