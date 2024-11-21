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
            providesTags: (result = []) => [
                'Item',
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

        unassignItemsFromOrgUnit: builder.mutation<Item[], Number[]>({
            query: itemIds => ({
                url: '/items/unassign',
                method: 'PUT',
                body: itemIds
            }),
            invalidatesTags: ['Item', 'OrgUnit']
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
    useGetItemQuery,
    useUpdateItemMutation,
    useAddNewItemMutation,
    useDeleteItemMutation,
} = itemApi;