import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom } from '../rooms/roomsSlice'

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({baseUrl: API_BASE_URL}),

    endpoints: builder => ({
        getRooms: builder.query<Room[], void>({
            query: () => '/rooms'
        }),

        addNewRoom: builder.mutation<Room, NewRoom> ({
            query: initialRoom => ({
                url: '/rooms',
                method: 'POST',
                body: initialRoom
            })
        })
    })
})

export const {
    useGetRoomsQuery,
    useAddNewRoomMutation
} = apiSlice