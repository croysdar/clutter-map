import React from 'react';

import { Button, Card, CardContent, TextField, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAddNewProjectMutation } from './projectApi';

interface AddProjectFormFields extends HTMLFormControlsCollection {
    projectName: HTMLInputElement,
    projectDescription: HTMLTextAreaElement
}

interface AddProjectFormElements extends HTMLFormElement {
    readonly elements: AddProjectFormFields
}


export const AddProject = () => {
    const [addNewProject, { isLoading }] = useAddNewProjectMutation()
    const navigate = useNavigate()

    const handleSubmit = async (e: React.FormEvent<AddProjectFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.projectName.value

        const form = e.currentTarget

        try {
            await addNewProject({ name }).unwrap()
            form.reset()

            // redirect to /projects
            navigate('/projects')
        } catch (err) {
            console.log("Failed to create the project: ", err)
        }
    }

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardContent>
                <Typography variant="h4" component="h2" gutterBottom align="center">
                    Add a New Project
                </Typography>
                <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
                    {/* Project Name */}
                    <TextField
                        label="Project Name"

                        id="projectName"
                        name="name"

                        required

                        fullWidth
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
                        Create Project
                    </Button>
                </form>

                <Button
                    variant="text"
                    fullWidth
                    sx={{ marginTop: 2 }}
                    onClick={() => navigate('/projects')}

                >
                    Cancel
                </Button>

            </CardContent>
        </Card>
    )
}