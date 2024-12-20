import { baseApiSlice } from "@/services/baseApiSlice";
import { NewOrgUnit, NewUnassignedOrgUnit, OrgUnit, OrgUnitUpdate } from "./orgUnitsTypes";
import { Item, ItemsAssign } from "../items/itemTypes";

export const orgUnitApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getOrgUnits: builder.query<OrgUnit[], void>({
            query: () => '/org-units',
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByRoom: builder.query<OrgUnit[], string>({
            query: (roomID) => `/rooms/${roomID}/org-units`,
            providesTags: (result = [], error, roomID) => [
                { type: 'Room', id: roomID },
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByProject: builder.query<OrgUnit[], string>({
            query: (projectId) => `/projects/${projectId}/org-units`,
            providesTags: (result = [], error, projectId) => [
                { type: 'Project', id: projectId },
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnit: builder.query<OrgUnit, string>({
            query: (orgUnitId) => `/org-units/${orgUnitId}`,
            providesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg }]
        }),

        /* ------------- POST Operations ------------- */
        addNewOrgUnit: builder.mutation<OrgUnit, NewOrgUnit>({
            query: initialOrgUnit => ({
                url: '/org-units',
                method: 'POST',
                body: initialOrgUnit
            }),
            invalidatesTags: ['OrgUnit']
        }),

        addNewUnassignedOrgUnit: builder.mutation<OrgUnit, NewUnassignedOrgUnit>({
            query: initialOrgUnit => ({
                url: '/org-units',
                method: 'POST',
                body: initialOrgUnit
            }),
            invalidatesTags: ['OrgUnit']
        }),

        /* ------------- PUT Operations ------------- */
        updateOrgUnit: builder.mutation<OrgUnit, OrgUnitUpdate>({
            query: orgUnit => ({
                url: `/org-units/${orgUnit.id}`,
                method: 'PUT',
                body: orgUnit
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg.id }]
        }),

        assignItemsToOrgUnit: builder.mutation<Item[], ItemsAssign>({
            query: ({ itemIds, orgUnitId }) => ({
                url: `/org-units/${orgUnitId}/items`,
                method: 'PUT',
                body: itemIds
            }),
            invalidatesTags: (result, error, { itemIds, orgUnitId }) => [
                { type: 'OrgUnit', id: orgUnitId },
                ...itemIds.map((id) => ({ type: 'Item', id } as const))
            ]
        }),

        unassignOrgUnitsFromRoom: builder.mutation<OrgUnit[], number[]>({
            query: orgUnitIds => ({
                url: '/org-units/unassign',
                method: 'PUT',
                body: orgUnitIds
            }),
            invalidatesTags: (result, error, orgUnitIds) => [
                ...orgUnitIds.map((id) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        /* ------------- DELETE Operations ------------- */
        deleteOrgUnit: builder.mutation<{ success: boolean, id: number }, number>({
            query: orgUnitId => ({
                url: `/org-units/${orgUnitId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'OrgUnit', id }]
        }),

    })
})

export const {
    useGetOrgUnitsQuery,
    useGetOrgUnitsByRoomQuery,
    useGetOrgUnitsByProjectQuery,
    useGetOrgUnitQuery,
    useAddNewOrgUnitMutation,
    useAddNewUnassignedOrgUnitMutation,
    useUpdateOrgUnitMutation,
    useAssignItemsToOrgUnitMutation,
    useUnassignOrgUnitsFromRoomMutation,
    useDeleteOrgUnitMutation,
} = orgUnitApi;