import React from 'react';

import { Button, Card, CardContent, Container, TextField, Typography } from '@mui/material';
import { useAddNewRoomMutation } from '../api/apiSlice';
import { useNavigate } from 'react-router-dom';

interface AddRoomFormFields extends HTMLFormControlsCollection {
    roomName: HTMLInputElement,
    roomDescription: HTMLTextAreaElement
}

interface AddRoomFormElements extends HTMLFormElement {
    readonly elements: AddRoomFormFields
}


export const AddRoom = () => {
    const [addNewRoom, { isLoading }] = useAddNewRoomMutation()
    const navigate = useNavigate()

    const handleSubmit = async (e: React.FormEvent<AddRoomFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.roomName.value
        const description = elements.roomDescription.value

        const form = e.currentTarget

        try {
            await addNewRoom({ name, description }).unwrap()
            form.reset()

            // redirect to /rooms
            navigate('/rooms')
        } catch (err) {
            console.log("Failed to create the room: ", err)
        }
    }

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Add a New Room
                    </Typography>
                    <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
                        {/* Room Name */}
                        <TextField
                            label="Room Name"

                            id="roomName"
                            name="name"

                            required

                            fullWidth
                            margin="normal"
                            variant="outlined"
                            InputLabelProps={{shrink: true}}
                        />

                        {/* Room Description */}
                        <TextField
                            label="Room Description"

                            id="roomDescription"
                            name="description"

                            fullWidth
                            multiline
                            rows={4}
                            margin="normal"
                            variant="outlined"
                            InputLabelProps={{shrink: true}}
                        />

                        {/* Submit Button */}
                        <Button
                            type="submit"
                            variant="contained"
                            color="primary"
                            fullWidth
                            sx={{ marginTop: 2 }}
                            disabled={isLoading}
                        >
                            Create Room
                        </Button>
                    </form>

                </CardContent>
            </Card>
        </Container>

    )
}