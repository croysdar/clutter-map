import { baseApiSlice } from "@/services/baseApiSlice";
import { NewItem, Item, ItemUpdate, NewUnassignedItem } from "./itemTypes";

export const itemApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getItems: builder.query<Item[], void>({
            query: () => '/items',
            providesTags: (result = []) => [
                'Item',
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItemsByOrgUnit: builder.query<Item[], string>({
            query: (orgUnitID) => `/org-units/${orgUnitID}/items`,
            providesTags: (result = [], error, orgUnitID) => [
                { type: 'OrgUnit', id: orgUnitID },
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItemsByProject: builder.query<Item[], string>({
            query: (projectId) => `/projects/${projectId}/items`,
            providesTags: (result = [], error, projectID) => [
                { type: 'Project', id: projectID },
                ...result.map(({ id }) => ({ type: 'Item', id } as const))
            ]
        }),

        getItem: builder.query<Item, string>({
            query: (itemId) => `/items/${itemId}`,
            providesTags: (result, error, arg) => [{ type: 'Item', id: arg }]
        }),

        /* ------------- POST Operations ------------- */
        addNewItem: builder.mutation<Item, NewItem>({
            query: initialItem => ({
                url: '/items',
                method: 'POST',
                body: initialItem
            }),
            invalidatesTags: ['Item']
        }),

        addNewUnassignedItem: builder.mutation<Item, NewUnassignedItem>({
            query: initialItem => ({
                url: '/items',
                method: 'POST',
                body: initialItem
            }),
            invalidatesTags: ['Item']
        }),

        /* ------------- PUT Operations ------------- */
        updateItem: builder.mutation<Item, ItemUpdate>({
            query: item => ({
                url: `/items/${item.id}`,
                method: 'PUT',
                body: item
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'Item', id: arg.id }]
        }),

        unassignItemsFromOrgUnit: builder.mutation<Item[], number[]>({
            query: itemIds => ({
                url: '/items/unassign',
                method: 'PUT',
                body: itemIds
            }),
            invalidatesTags: (result, error, itemIds) => [
                ...itemIds.map((id) => ({ type: 'Item', id } as const))
            ]
        }),

        /* ------------- DELETE Operations ------------- */
        deleteItem: builder.mutation<{ success: boolean, id: number }, number>({
            query: itemId => ({
                url: `/items/${itemId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'Item', id }]
        }),

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