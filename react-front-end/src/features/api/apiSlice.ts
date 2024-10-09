import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom, RoomUpdate } from '../rooms/roomsSlice'
import { Project, NewProject, ProjectUpdate } from '../projects/projectsTypes'

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({ baseUrl: API_BASE_URL }),

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

        deleteRoom: builder.mutation<{ success: boolean, id: number }, number>({
            query: roomId => ({
                url: `/rooms/${roomId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'Room', id }]
        }),

        addNewRoom: builder.mutation<Room, NewRoom>({
            query: initialRoom => ({
                url: '/rooms',
                method: 'POST',
                body: initialRoom
            }),
            invalidatesTags: ['Room']
        }),

        getProjects: builder.query<Project[], void>({
            query: () => '/projects',
        }),

        getProject: builder.query<Project, string>({
            query: (projectId) => `/projects/${projectId}`,
        }),

        updateProject: builder.mutation<Project, ProjectUpdate>({
            query: project => ({
                url: `/projects/${project.id}`,
                method: 'PUT',
                body: project
            }),
        }),

        deleteProject: builder.mutation<{ success: boolean, id: number }, number>({
            query: projectId => ({
                url: `/projects/${projectId}`,
                method: 'DELETE',
            }),
        }),

        addNewProject: builder.mutation<Project, NewProject>({
            query: initialProject => ({
                url: '/projects',
                method: 'POST',
                body: initialProject
            }),
        })
        })

    })
})

export const {
    useGetRoomsQuery,
    useGetRoomQuery,
    useUpdateRoomMutation,
    useAddNewRoomMutation,
    useDeleteRoomMutation,
    useGetProjectsQuery,
    useGetProjectQuery,
    useUpdateProjectMutation,
    useAddNewProjectMutation,
    useDeleteProjectMutation
} = apiSlice