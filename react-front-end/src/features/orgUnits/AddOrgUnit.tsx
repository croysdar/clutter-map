import React from 'react';

import { Button, Card, CardContent, Container, TextField, Typography } from '@mui/material';
import { useAddNewOrgUnitMutation, useGetRoomQuery } from '../api/apiSlice';
import { useNavigate } from 'react-router-dom';
import { useParams } from 'react-router-dom';

interface AddOrgUnitFormFields extends HTMLFormControlsCollection {
    orgUnitName: HTMLInputElement,
    orgUnitDescription: HTMLTextAreaElement
}

interface AddOrgUnitFormElements extends HTMLFormElement {
    readonly elements: AddOrgUnitFormFields
}

export const AddOrgUnit = () => {
    const [addNewOrgUnit, { isLoading }] = useAddNewOrgUnitMutation()
    const navigate = useNavigate()
    const { roomId } = useParams();

    const { data: room } = useGetRoomQuery(roomId!);

    const { projectId } = useParams();

    const handleSubmit = async (e: React.FormEvent<AddOrgUnitFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.orgUnitName.value
        const description = elements.orgUnitDescription.value

        const form = e.currentTarget

        if (!room) {
            console.log("Room not found")
            return
        }

        try {
            await addNewOrgUnit({ name, description, roomId }).unwrap()
            form.reset()

            // redirect to [this room]/org-units
            navigate(`/projects/${projectId}/rooms/${roomId}/org-units`)
        } catch (err) {
            console.error("Failed to create the orgUnit: ", err)
        }
    }

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Add a New OrgUnit
                    </Typography>
                    <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
                        {/* OrgUnit Name */}
                        <TextField
                            label="OrgUnit Name"

                            id="orgUnitName"
                            name="name"

                            required

                            fullWidth
                            margin="normal"
                            variant="outlined"
                            InputLabelProps={{ shrink: true }}
                        />

                        {/* OrgUnit Description */}
                        <TextField
                            label="OrgUnit Description"

                            id="orgUnitDescription"
                            name="description"

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
                            disabled={isLoading}
                        >
                            Create OrgUnit
                        </Button>
                    </form>
                    <Button
                        variant="text"
                        color="error"
                        fullWidth
                        sx={{ marginTop: 2 }}
                        onClick={() => navigate(`/projects/${projectId}/rooms/${roomId}/org-units`)}
                    >
                        Cancel
                    </Button>

                </CardContent>
            </Card>
        </Container>
    )
}