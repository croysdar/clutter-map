import { baseApiSlice } from "@/services/baseApiSlice";
import { NewProject, Project, ProjectUpdate } from "./projectsTypes";

export const projectApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
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
            invalidatesTags: (result, error, arg) => [{ type: 'Project', id: arg.id }]
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