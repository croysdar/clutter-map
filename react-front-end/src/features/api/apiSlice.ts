import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom, RoomUpdate } from '../rooms/roomsSlice'
import { Project, NewProject, ProjectUpdate } from '../projects/projectsTypes'

export const apiSlice = createApi({
    reducerPath: 'api',

    baseQuery: fetchBaseQuery({ baseUrl: API_BASE_URL }),

    tagTypes: ['Room', 'Project'],

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
            providesTags: (result = []) => [
                'Project',
                ...result.map(({ id }) => ({ type: 'Project', id } as const))
            ]
        }),

        getProject: builder.query<Project, string>({
            query: (projectId) => `/projects/${projectId}`,
            providesTags: (result, error, arg) => [{ type: 'Project', id: arg }]
        }),

        updateProject: builder.mutation<Project, ProjectUpdate>({
            query: project => ({
                url: `/projects/${project.id}`,
                method: 'PUT',
                body: project
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'Project', id: arg.id }]
        }),

        deleteProject: builder.mutation<{ success: boolean, id: number }, number>({
            query: projectId => ({
                url: `/projects/${projectId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'Project', id }]
        }),

        addNewProject: builder.mutation<Project, NewProject>({
            query: initialProject => ({
                url: '/projects',
                method: 'POST',
                body: initialProject
            }),
            invalidatesTags: ['Project']
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