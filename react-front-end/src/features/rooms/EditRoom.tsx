import React from 'react'

import { Button, Card, CardContent, CircularProgress, Container, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
import { useGetRoomQuery, useUpdateRoomMutation } from '../api/apiSlice'

interface EditRoomFormFields extends HTMLFormControlsCollection {
    roomName: HTMLInputElement,
    roomDescription: HTMLTextAreaElement
}

interface EditRoomFormElements extends HTMLFormElement {
    readonly elements: EditRoomFormFields
}

const EditRoom = () => {
    const { roomID } = useParams();
    const navigate = useNavigate();

    const { data: room, isLoading: roomLoading } = useGetRoomQuery(roomID!);

    const [
        updateRoom,
        { isLoading: updateLoading }
    ] = useUpdateRoomMutation();

    if (!room) {
        return (
            <section>
                <Typography variant='h2'>Room not found!</Typography>
            </section>
        )
    }

    if (roomLoading) {
        return (
            <CircularProgress />
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditRoomFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.roomName.value
        const description = elements.roomDescription.value

        if (room && name) {
            await updateRoom({id: room.id, name: name, description: description})
            navigate(`/rooms`)
        }
    }

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Edit Room
                    </Typography>
                    <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
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

                </CardContent>
            </Card>
        </Container>
    )
}

export default EditRoom