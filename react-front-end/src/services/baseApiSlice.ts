import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

import { API_BASE_URL } from '@/utils/constants';
import { getCsrfTokenFromCookies } from '@/utils/utils';

import { syncIDB } from '@/features/offline/idbSlice';

export const baseApiSlice = createApi({
    reducerPath: 'api',

    baseQuery: async (args, api, extraOptions) => {
        const baseQuery = fetchBaseQuery({
            baseUrl: API_BASE_URL,
            prepareHeaders: (headers, { getState }) => {
                const token = localStorage.getItem('jwt');

                if (token) {
                    headers.set('authorization', `Bearer ${token}`);
                }

                headers.set('X-CSRF-Token', getCsrfTokenFromCookies() || '');
                return headers;
            }
        });

        const result = await baseQuery(args, api, extraOptions);

        if (args.method && args.method !== 'GET') {
            console.log("Syncing with server due to mutation...");
            await api.dispatch(syncIDB());

            console.log("Sync complete. Now invalidating redux cache...");
            api.dispatch(
                baseApiSlice.util.invalidateTags(['Project', 'Room', 'OrgUnit', 'Item'])
            )
        }

        return result;
    },

    tagTypes: ['Room', 'Project', 'OrgUnit', 'Item', 'Event'],

    endpoints: builder => ({

    })
})