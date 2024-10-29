import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
import { Room, NewRoom, RoomUpdate } from '../rooms/roomsSlice'
import { OrgUnit, NewOrgUnit, OrgUnitUpdate } from '../orgUnits/orgUnitsSlice'
import { Project, NewProject, ProjectUpdate } from '../projects/projectsTypes'
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

    tagTypes: ['Room', 'Project', 'OrgUnit'],

    endpoints: builder => ({


        getOrgUnits: builder.query<OrgUnit[], void>({
            query: () => '/org-units',
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnitsByRoom: builder.query<OrgUnit[], string>({
            query: (roomID) => `/rooms/${roomID}/org-units`,
            providesTags: (result = []) => [
                'OrgUnit',
                ...result.map(({ id }) => ({ type: 'OrgUnit', id } as const))
            ]
        }),

        getOrgUnit: builder.query<OrgUnit, string>({
            query: (orgUnitId) => `/org-units/${orgUnitId}`,
            providesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg }]
        }),

        updateOrgUnit: builder.mutation<OrgUnit, OrgUnitUpdate>({
            query: orgUnit => ({
                url: `/org-units/${orgUnit.id}`,
                method: 'PUT',
                body: orgUnit
            }),
            invalidatesTags: (result, error, arg) => [{ type: 'OrgUnit', id: arg.id }]
        }),

        deleteOrgUnit: builder.mutation<{ success: boolean, id: number }, number>({
            query: orgUnitId => ({
                url: `/org-units/${orgUnitId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'OrgUnit', id }]
        }),

        addNewOrgUnit: builder.mutation<OrgUnit, NewOrgUnit>({
            query: initialOrgUnit => ({
                url: '/org-units',
                method: 'POST',
                body: initialOrgUnit
            }),
            invalidatesTags: ['OrgUnit']
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

    useGetOrgUnitsQuery,
    useGetOrgUnitsByRoomQuery,
    useGetOrgUnitQuery,
    useUpdateOrgUnitMutation,
    useAddNewOrgUnitMutation,
    useDeleteOrgUnitMutation,
    useGetProjectsQuery,
    useGetProjectQuery,
    useUpdateProjectMutation,
    useAddNewProjectMutation,
    useDeleteProjectMutation
} = baseApiSlice