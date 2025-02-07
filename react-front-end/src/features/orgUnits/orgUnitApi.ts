import { baseApiSlice } from "@/services/baseApiSlice";
import { ResourceType } from "@/types/types";
import { Item, ItemsAssign } from "../items/itemTypes";
import { Stores } from "../offline/idb";
import { getAllFromIndexedDB, getByIdFromIndexedDB, getRelatedEntities } from "../offline/useIndexedDBQuery";
import { Project } from "../projects/projectsTypes";
import { Room } from "../rooms/roomsTypes";
import { NewOrgUnit, NewUnassignedOrgUnit, OrgUnit, OrgUnitUpdate } from "./orgUnitsTypes";

export const orgUnitApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getOrgUnits: builder.query<OrgUnit[], void>({
            queryFn: async () => await getAllFromIndexedDB(Stores.OrgUnits),
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByRoom: builder.query<OrgUnit[], number>({
            queryFn: async (roomID) =>
                getRelatedEntities<Room, OrgUnit>(
                    Stores.Rooms,
                    roomID,
                    Stores.OrgUnits,
                    'orgUnitIds'
                ),
            providesTags: (result = [], error, roomID) => [
                { type: 'Room', id: roomID },
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByProject: builder.query<OrgUnit[], number>({
            queryFn: async (projectID) =>
                getRelatedEntities<Project, OrgUnit>(
                    Stores.Projects,
                    projectID,
                    Stores.OrgUnits,
                    'orgUnitIds'
                ),
            providesTags: (result = [], error, projectId) => [
                { type: 'Project', id: projectId },
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnit: builder.query<OrgUnit, number>({
            queryFn: async (orgUnitId) => await getByIdFromIndexedDB(Stores.OrgUnits, orgUnitId),
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
            invalidatesTags: (result, error, arg) => [
                { type: 'OrgUnit', id: arg.id },
                { type: 'Event', id: `${ResourceType.ORGANIZATIONAL_UNIT}-${arg.id}` }
            ]
        }),

        assignItemsToOrgUnit: builder.mutation<Item[], ItemsAssign>({
            query: ({ itemIds, orgUnitId }) => ({
                url: `/org-units/${orgUnitId}/items`,
                method: 'PUT',
                body: itemIds
            }),
            invalidatesTags: (result, error, { itemIds, orgUnitId }) => [
                { type: 'OrgUnit', id: orgUnitId },
                ...itemIds.map((id) => ({ type: 'Item', id } as const)),
                ...itemIds.map((id) => ({ type: 'Event', id: `${ResourceType.ITEM}-${id}` } as const))
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