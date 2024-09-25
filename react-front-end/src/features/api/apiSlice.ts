import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom } from '../rooms/roomsSlice'

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({baseUrl: API_BASE_URL}),

    tagTypes: ['Room'],

    endpoints: builder => ({
        getRooms: builder.query<Room[], void>({
            query: () => '/rooms',
            providesTags: (result = []) => [
                'Room',
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
            query: () => '/rooms'

        }),

        addNewRoom: builder.mutation<Room, NewRoom> ({
            query: initialRoom => ({
                url: '/rooms',
                method: 'POST',
                body: initialRoom
            }),
            invalidatesTags: ['Room']
        })
    })
})

export const {
    useGetRoomsQuery,
    useAddNewRoomMutation
} = apiSlice