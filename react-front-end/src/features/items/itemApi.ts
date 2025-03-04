import { baseApiSlice } from "@/services/baseApiSlice";
import { ResourceType } from "@/types/types";
import { Item, ItemUpdate, NewItem, NewUnassignedItem } from "./itemTypes";

import { Stores } from "../offline/idb";
import { getAllFromIndexedDB, getByIdFromIndexedDB, getRelatedEntities } from "../offline/useIndexedDBQuery";
import { OrgUnit } from "../orgUnits/orgUnitsTypes";
import { Project } from "../projects/projectsTypes";

export const itemApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getItems: builder.query<Item[], void>({
            queryFn: async () => await getAllFromIndexedDB(Stores.Items),
            providesTags: (result = []) => [
                'Item',
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItemsByOrgUnit: builder.query<Item[], number>({
            queryFn: async (orgUnitID) =>
                getRelatedEntities<OrgUnit, Item>(
                    Stores.OrgUnits,
                    orgUnitID,
                    Stores.Items,
                    'itemIds'
                ),
            providesTags: (result = [], error, orgUnitID) => [
                { type: 'OrgUnit', id: orgUnitID },
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItemsByProject: builder.query<Item[], number>({
            queryFn: async (projectID) =>
                getRelatedEntities<Project, Item>(
                    Stores.Projects,
                    projectID,
                    Stores.Items,
                    'itemIds'
                ),
            providesTags: (result = [], error, projectID) => [
                { type: 'Project', id: projectID },
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItem: builder.query<Item, number>({
            queryFn: async (itemId) => await getByIdFromIndexedDB(Stores.Items, itemId),
            providesTags: (result, error, arg) => [{ type: 'Item', id: arg }],
        }),

        /* ------------- POST Operations ------------- */
        addNewItem: builder.mutation<Item, NewItem>(
            ({
                query: newItem => ({
                    url: '/items',
                    method: 'POST',
                    body: newItem
                }),
                invalidatesTags: ['Item']
            })
        ),

        addNewUnassignedItem: builder.mutation<Item, NewUnassignedItem>(
            ({
                query: initialItem => ({
                    url: '/items',
                    method: 'POST',
                    body: initialItem
                }),
                invalidatesTags: ['Item']
            })
        ),

        /* ------------- PUT Operations ------------- */
        updateItem: builder.mutation<Item, ItemUpdate>(
            ({
                query: item => ({
                    url: `/items/${item.id}`,
                    method: 'PUT',
                    body: item
                }),
                invalidatesTags: (result, error, arg) => [
                    { type: 'Item', id: arg.id },
                    { type: 'Event', id: `${ResourceType.ITEM}-${arg.id}` }
                ]
            })
        ),

        unassignItemsFromOrgUnit: builder.mutation<Item[], number[]>(
            ({
                query: itemIds => ({
                    url: '/items/unassign',
                    method: 'PUT',
                    body: itemIds
                }),
                invalidatesTags: (result, error, itemIds) => [
                    ...itemIds.map((id: number) => ({ type: 'Item', id } as const))
                ]
            })
        ),

        /* ------------- DELETE Operations ------------- */
        deleteItem: builder.mutation<{ success: boolean, id: number }, number>(
            ({
                query: itemId => ({
                    url: `/items/${itemId}`,
                    method: 'DELETE',
                }),
                invalidatesTags: (result, error, id) => [{ type: 'Item', id }]
            })
        ),

    })
})

export const {
    useGetItemsQuery,
    useGetItemsByOrgUnitQuery,
    useGetItemsByProjectQuery,
    useGetItemQuery,
    useAddNewItemMutation,
    useAddNewUnassignedItemMutation,
    useUpdateItemMutation,
    useUnassignItemsFromOrgUnitMutation,
    useDeleteItemMutation,
} = itemApi;