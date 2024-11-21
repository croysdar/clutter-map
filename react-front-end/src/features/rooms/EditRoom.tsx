import React from 'react'

import { Button, Card, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButton from '@/components/DeleteEntityButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPage'
import { useDeleteRoomMutation, useGetRoomQuery, useUpdateRoomMutation } from './roomApi'

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
    const sourcePageUrl = `/projects/${projectId}/rooms`;

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
            navigate(sourcePageUrl)
        }
    }

    const handleCancelClick = () => {
        navigate(sourcePageUrl)
    }

    return (
        <EditCardWrapper title="Edit Room">
            <form onSubmit={handleSubmit}>
                {/* Room Name */}
                <TextField
                    label="Room Name"

                    id="roomName"
                    name="name"
                    defaultValue={room.name}

                    required

                    fullWidth
                    margin="normal"
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                />

                {/* Room Description */}
                <TextField
                    label="Room Description"

                    id="roomDescription"
                    name="description"
                    defaultValue={room.description}

                    fullWidth
                    multiline
                    rows={4}
                    margin="normal"
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                />

                {/* Submit Button */}
                <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    fullWidth
                    sx={{ marginTop: 2 }}
                    disabled={updateLoading}
                >
                    Save Changes
                </Button>
            </form>

            <Button
                variant='text'
                fullWidth
                sx={{ marginTop: 2 }}
                onClick={handleCancelClick}
            >
                Cancel
            </Button>

            {/* Delete button with a confirmation dialog */}
            <DeleteEntityButton
                entity={room}
                id={room.id}
                name={room.name}
                entityType='Room'
                mutation={useDeleteRoomMutation}
                extraWarning='This will send all organizers within the room to the Clutter Stash.'
                isDisabled={updateLoading}
                redirectUrl={sourcePageUrl}
            />

        </EditCardWrapper>
    )
}

export default EditRoom