import React from 'react';

import { Button, Card, CardContent, Container, TextField, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';
import { useAddNewRoomMutation } from './roomApi';
import { useGetProjectQuery } from '../projects/projectApi';

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
    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(projectId!);

    const handleSubmit = async (e: React.FormEvent<AddRoomFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.roomName.value
        const description = elements.roomDescription.value

        const form = e.currentTarget

        if (!project) {
            console.log("Project not found")
            return
        }

        try {
            await addNewRoom({ name, description, projectId }).unwrap()
            form.reset()

            // redirect to [this project]/rooms
            navigate(`/projects/${projectId}/rooms`)
        } catch (err) {
            console.error("Failed to create the room: ", err)
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
                    <Button
                        variant="text"
                        color="error"
                        fullWidth
                        sx={{ marginTop: 2 }}
                        onClick={() => navigate(`/projects/${projectId}/rooms`)}

                    >
                        Cancel
                    </Button>

                </CardContent>
            </Card>
        </Container>

    )
}