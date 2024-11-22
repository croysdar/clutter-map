import React from 'react'

import { Card, CircularProgress, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import SubmitButton from '@/components/forms/SubmitButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'
import { useDeleteRoomMutation, useGetRoomQuery, useUpdateRoomMutation } from './roomApi'
import { Room } from './roomsTypes'
import { ROUTES } from '@/utils/constants'

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

    const { data: room, isLoading: roomLoading } = useGetRoomQuery(roomId!);

    const [
        updateRoom,
        { isLoading: updateLoading }
    ] = useUpdateRoomMutation();

    if (roomLoading) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Card>
        )
    }

    if (!room) {
        return (
            <section>
                <Typography variant='h2'>Room not found!</Typography>
            </section>
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
                {/* Room Name */}
                <AppTextField
                    label="Room Name"

                    id="roomName"
                    name="name"
                    defaultValue={room.name}

                    required
                />

                {/* Room Description */}
                <AppTextField
                    label="Room Description"

                    id="roomDescription"
                    name="description"
                    defaultValue={room.description}

                    multiline
                    rows={4}
                />

                {/* Submit Button */}
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
                redirectUrl={redirectUrl}
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    room: Room,
    isDisabled: boolean
    redirectUrl: string
}

const DeleteRoomButton: React.FC<DeleteButtonProps> = ({ room, isDisabled, redirectUrl }) => {
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