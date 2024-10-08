import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom, RoomUpdate } from '../rooms/roomsSlice'
import { getCsrfTokenFromCookies } from '@/utils/utils';

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({
        baseUrl: API_BASE_URL,
        prepareHeaders: (headers, {getState}) => {
            const token = localStorage.getItem('jwt');

            if (token) {
                headers.set('authorization', `Bearer ${token}`)
            }

            headers.set('X-CSRF-Token', getCsrfTokenFromCookies() || '')

            return headers
        }
    }),

    tagTypes: ['Room'],

    endpoints: builder => ({
        getRooms: builder.query<Room[], void>({
            query: () => '/rooms',
            providesTags: (result = []) => [
                'Room',
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
        }),

        getRoom: builder.query<Room, string>({
            query: (roomId) => `/rooms/${roomId}`,
            providesTags: (result, error, arg) => [{ type: 'Room', id: arg }]
        }),

        updateRoom: builder.mutation<Room, RoomUpdate>({
            query: room => ({
                url: `/rooms/${room.id}`,
                method: 'PUT',
                body: room
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'Room', id: arg.id }]
        }),

        deleteRoom: builder.mutation<{success: boolean, id: number}, number>({
            query: roomId => ({
                url: `/rooms/${roomId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{type: 'Room', id}]
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
    useGetRoomQuery,
    useUpdateRoomMutation,
    useAddNewRoomMutation,
    useDeleteRoomMutation
} = apiSlice