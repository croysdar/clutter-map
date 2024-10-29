import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

import { API_BASE_URL } from '@/utils/constants'
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
    useGetProjectsQuery,
    useGetProjectQuery,
    useUpdateProjectMutation,
    useAddNewProjectMutation,
    useDeleteProjectMutation
} = baseApiSlice