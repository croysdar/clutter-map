import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react'

import { Room } from '@/types/types'
import { API_BASE_URL } from '@/utils/constants'

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({baseUrl: API_BASE_URL}),

    endpoints: builder => ({
        getRooms: builder.query<Room[], void>({
            query: () => '/rooms'
        })
    })
})

export const {
    useGetRoomsQuery
} = apiSlice