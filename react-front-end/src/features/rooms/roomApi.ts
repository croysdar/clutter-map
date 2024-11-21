import { baseApiSlice } from "@/services/baseApiSlice";
import { NewRoom, Room, RoomUpdate } from "./roomsTypes";
import { OrgUnit, OrgUnitsAssign } from "../orgUnits/orgUnitsTypes";

export const roomsApi = baseApiSlice.injectEndpoints({
    endpoints: (builder) => ({
        /* ------------- GET Operations ------------- */
        getRooms: builder.query<Room[], void>({
            query: () => '/rooms',
            providesTags: (result = []) => [
                'Room',
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
        }),

        getRoomsByProject: builder.query<Room[], string>({
            query: (projectID) => `/projects/${projectID}/rooms`,
            providesTags: (result = []) => [
                'Room',
                ...result.map(({ id }) => ({ type: 'Room', id } as const))
            ]
        }),

        getRoom: builder.query<Room, string>({
            query: (roomId) => `/rooms/${roomId}`,
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
            invalidatesTags: (result, error, arg) => [{ type: 'Room', id: arg.id }]
        }),

        assignOrgUnitsToRoom: builder.mutation<OrgUnit[], OrgUnitsAssign>({
            query: ({ orgUnitIds, roomId }) => ({
                url: `/rooms/${roomId}/org-units`,
                method: 'PUT',
                body: orgUnitIds
            })
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
    useAddNewRoomMutation,
    useDeleteRoomMutation,
} = roomsApi;