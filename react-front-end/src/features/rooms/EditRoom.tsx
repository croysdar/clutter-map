import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material'

/* ------------- Components ------------- */
import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import SubmitButton from '@/components/forms/SubmitButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'

/* ------------- Redux ------------- */
import { useDeleteRoomMutation, useGetRoomQuery, useUpdateRoomMutation } from '@/features/rooms/roomApi'

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants'

/* ------------- Types ------------- */
import { Room } from '@/features/rooms/roomsTypes'

interface EditRoomFormFields extends HTMLFormControlsCollection {
    roomName: HTMLInputElement,
    roomDescription: HTMLTextAreaElement
}

interface EditRoomFormElements extends HTMLFormElement {
    readonly elements: EditRoomFormFields
}

const EditRoom = () => {
    const navigate = useNavigate();
    const { roomId, projectId } = useParams();
    const redirectUrl = ROUTES.roomDetails(projectId!, roomId!)

    const { data: room, isLoading: roomLoading, isError, error } = useGetRoomQuery(Number(roomId!));

    const [
        updateRoom,
        { isLoading: updateLoading }
    ] = useUpdateRoomMutation();

    if (roomLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!room) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Room not found</Typography>
            </EditCardWrapper>
        )
    }

    if (isError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </EditCardWrapper>
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditRoomFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.roomName.value
        const description = elements.roomDescription.value

        if (room && name) {
            await updateRoom({ id: room.id, name: name, description: description })
            navigate(redirectUrl)
        }
    }

    return (
        <EditCardWrapper title="Edit Room">
            <form onSubmit={handleSubmit}>
                <AppTextField
                    label="Room Name"

                    id="roomName"
                    name="name"
                    defaultValue={room.name}

                    required
                />

                <AppTextField
                    label="Room Description"

                    id="roomDescription"
                    name="description"
                    defaultValue={room.description}

                    multiline
                    rows={4}
                />

                <SubmitButton
                    disabled={updateLoading}
                    label="Save Changes"
                />
            </form>

            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />

            {/* Delete button with a confirmation dialog */}
            <DeleteRoomButton
                room={room}
                isDisabled={updateLoading}
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    room: Room,
    isDisabled: boolean
}

const DeleteRoomButton: React.FC<DeleteButtonProps> = ({ room, isDisabled }) => {
    const { projectId } = useParams();
    const redirectUrl = ROUTES.projectDetails(projectId!)

    return (
        <DeleteEntityButtonWithModal
            entity={room}
            id={room.id}
            name={room.name}
            entityType='Room'
            mutation={useDeleteRoomMutation}
            isDisabled={isDisabled}
            redirectUrl={redirectUrl}
        />
    );
}

export default EditRoom