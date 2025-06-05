import { baseApiSlice } from "@/services/baseApiSlice";
import { ResourceType } from "@/types/types";
import { Stores } from "../offline/idb";
import { getAllFromIndexedDB, getByIdFromIndexedDB } from "../offline/useIndexedDBQuery";
import { type NewProject, type Project, type ProjectUpdate } from "./projectsTypes";

export const projectApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getProjects: builder.query<Project[], void>({
            queryFn: async () => await getAllFromIndexedDB(Stores.Projects),
            providesTags: (result = []) => [
                'Project',
                ...result.map(({ id }) => ({ type: 'Project', id } as const))
            ]
        }),

        getProject: builder.query<Project, number>({
            queryFn: async (projectId) => await getByIdFromIndexedDB(Stores.Projects, projectId),
            providesTags: (result, error, arg) => [{ type: 'Project', id: arg }]
        }),

        /* ------------- POST Operations ------------- */
        addNewProject: builder.mutation<Project, NewProject>({
            query: initialProject => ({
                url: '/projects',
                method: 'POST',
                body: initialProject
            }),
            invalidatesTags: ['Project']
        }),

        /* ------------- PUT Operations ------------- */
        updateProject: builder.mutation<Project, ProjectUpdate>({
            query: project => ({
                url: `/projects/${project.id}`,
                method: 'PUT',
                body: project
            }),
            invalidatesTags: (result, error, arg) => [
                { type: 'Project', id: arg.id },
                { type: 'Event', id: `${ResourceType.PROJECT}-${arg.id}` }
            ]
        }),

        /* ------------- DELETE Operations ------------- */
        deleteProject: builder.mutation<{ success: boolean, id: number }, number>({
            query: projectId => ({
                url: `/projects/${projectId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'Project', id }]
        }),


    })
})

export const {
    useGetProjectsQuery,
    useGetProjectQuery,
    useUpdateProjectMutation,
    useAddNewProjectMutation,
    useDeleteProjectMutation
} = projectApi;