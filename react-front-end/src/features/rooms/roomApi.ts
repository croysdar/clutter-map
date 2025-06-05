import { baseApiSlice } from "@/services/baseApiSlice";
import { ResourceType } from "@/types/types";
import { Stores } from "../offline/idb";
import { getAllFromIndexedDB, getByIdFromIndexedDB, getRelatedEntities } from "../offline/useIndexedDBQuery";
import { type OrgUnit, type OrgUnitsAssign } from "../orgUnits/orgUnitsTypes";
import { type Project } from "../projects/projectsTypes";
import { type NewRoom, type Room, type RoomUpdate } from "./roomsTypes";

export const roomsApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getRooms: builder.query<Room[], void>({
            queryFn: async () => await getAllFromIndexedDB(Stores.Rooms),
            providesTags: (result = []) => [
                'Room',
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
        }),

        getRoomsByProject: builder.query<Room[], number>({
            queryFn: async (projectID) =>
                getRelatedEntities<Project, Room>(
                    Stores.Projects,
                    projectID,
                    Stores.Rooms,
                    'roomIds'
                ),
            providesTags: (result = [], error, projectID) => [
                'Room',
                { type: 'Project', id: projectID },
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
        }),

        getRoom: builder.query<Room, number>({
            queryFn: async (roomId) => await getByIdFromIndexedDB(Stores.Rooms, roomId),
            providesTags: (result, error, arg) => [{ type: 'Room', id: arg }]
        }),

        /* ------------- POST Operations ------------- */
        addNewRoom: builder.mutation<Room, NewRoom>({
            query: initialRoom => ({
                url: '/rooms',
                method: 'POST',
                body: initialRoom
            }),
            invalidatesTags: ['Room']
        }),

        /* ------------- PUT Operations ------------- */
        updateRoom: builder.mutation<Room, RoomUpdate>({
            query: room => ({
                url: `/rooms/${room.id}`,
                method: 'PUT',
                body: room
            }),
            invalidatesTags: (result, error, arg) => [
                { type: 'Room', id: arg.id },
                { type: 'Event', id: `${ResourceType.ROOM}-${arg.id}` }
            ]
        }),

        assignOrgUnitsToRoom: builder.mutation<OrgUnit[], OrgUnitsAssign>({
            query: ({ orgUnitIds, roomId }) => ({
                url: `/rooms/${roomId}/org-units`,
                method: 'PUT',
                body: orgUnitIds
            }),
            invalidatesTags: (result, error, { orgUnitIds, roomId }) => [
                { type: "Room", id: roomId },
                ...orgUnitIds.map((id) => ({ type: 'OrgUnit', id } as const)),
                ...orgUnitIds.map((id) => ({ type: 'Event', id: `${ResourceType.ORGANIZATIONAL_UNIT}-${id}` } as const))
            ]
        }),

        /* ------------- DELETE Operations ------------- */
        deleteRoom: builder.mutation<{ success: boolean, id: number }, number>({
            query: roomId => ({
                url: `/rooms/${roomId}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [{ type: 'Room', id }]
        }),

    })
})

export const {
    useGetRoomsQuery,
    useGetRoomsByProjectQuery,
    useGetRoomQuery,
    useUpdateRoomMutation,
    useAssignOrgUnitsToRoomMutation,
    useAddNewRoomMutation,
    useDeleteRoomMutation,
} = roomsApi;