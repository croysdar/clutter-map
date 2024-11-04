import React from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteProjectButton from './DeleteProjectButton'
import { useGetProjectQuery, useUpdateProjectMutation } from './projectApi'

interface EditProjectFormFields extends HTMLFormControlsCollection {
    projectName: HTMLInputElement,
}

interface EditProjectFormElements extends HTMLFormElement {
    readonly elements: EditProjectFormFields
}

const EditProject = () => {
    const navigate = useNavigate();
    const { projectId } = useParams();
    const sourcePageUrl = '/projects';

    const { data: project, isLoading: projectLoading } = useGetProjectQuery(projectId!);

    const [
        updateProject,
        { isLoading: updateLoading }
    ] = useUpdateProjectMutation();

    if (projectLoading) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Card>
        )
    }

    if (!project) {
        return (
            <section>
                <Typography variant='h2'>Project not found!</Typography>
            </section>
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditProjectFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.projectName.value

        if (project && name) {
            await updateProject({ id: project.id, name: name })
            navigate(sourcePageUrl)
        }
    }

    const handleCancelClick = () => {
        navigate(sourcePageUrl)
    }

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardHeader
                title={
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Edit Project
                    </Typography>
                }
            />
            <CardContent>
                <form onSubmit={handleSubmit}>
                    {/* Project Name */}
                    <TextField
                        label="Project Name"

                        id="projectName"
                        name="name"
                        defaultValue={project.name}

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
                <DeleteProjectButton project={project} isDisabled={updateLoading} redirectUrl={sourcePageUrl} />

            </CardContent>
        </Card>
    )
}

export default EditProject