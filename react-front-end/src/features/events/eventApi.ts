import { baseApiSlice } from "@/services/baseApiSlice";
import { ResourceType } from "@/types/types";
import { TimelineEvent } from "./eventTypes";

export const eventApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getEntityEvents: builder.query<TimelineEvent[], { entityType: ResourceType, entityId: number }>({
            query: ({ entityType, entityId }) => `/events/${entityType}/${entityId}`,
            transformResponse: (response: { content: TimelineEvent[] }) => response.content,
            providesTags: (result, error, { entityType, entityId }) => [
                { type: 'Event', id: `${entityType}-${entityId}` },
            ],
        }),
    })
})

export const {
    useGetEntityEventsQuery
} = eventApi;