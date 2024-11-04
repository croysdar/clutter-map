import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { getCsrfTokenFromCookies } from '@/utils/utils';

export const baseApiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({
        baseUrl: API_BASE_URL,
        prepareHeaders: (headers, { getState }) => {
            const token = localStorage.getItem('jwt');

            if (token) {
                headers.set('authorization', `Bearer ${token}`)
            }

            headers.set('X-CSRF-Token', getCsrfTokenFromCookies() || '')

            return headers
        }
    }),

    tagTypes: ['Room', 'Project', 'OrgUnit', 'Item'],

    endpoints: builder => ({

    })
})