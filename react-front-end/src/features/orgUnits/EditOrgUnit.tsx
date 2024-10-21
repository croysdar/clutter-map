import React from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, Container, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import { useGetOrgUnitQuery, useUpdateOrgUnitMutation } from '../api/apiSlice'
import DeleteOrgUnitButton from './DeleteOrgUnitButton'

interface EditOrgUnitFormFields extends HTMLFormControlsCollection {
    orgUnitName: HTMLInputElement,
    orgUnitDescription: HTMLTextAreaElement
}

interface EditOrgUnitFormElements extends HTMLFormElement {
    readonly elements: EditOrgUnitFormFields
}

const EditOrgUnit = () => {
    const { orgUnitId } = useParams();
    const navigate = useNavigate();
    const { roomId } = useParams();
    const { projectId } = useParams();

    const { data: orgUnit, isLoading: orgUnitLoading } = useGetOrgUnitQuery(orgUnitId!);

    const [
        updateOrgUnit,
        { isLoading: updateLoading }
    ] = useUpdateOrgUnitMutation();

    if (!orgUnit) {
        return (
            <section>
                <Typography variant='h2'>OrgUnit not found!</Typography>
            </section>
        )
    }

    if (orgUnitLoading) {
        return (
            <CircularProgress />
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditOrgUnitFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.orgUnitName.value
        const description = elements.orgUnitDescription.value

        if (orgUnit && name) {
            await updateOrgUnit({ id: orgUnit.id, name: name, description: description })
            // redirect to [this room]/orgUnits
            navigate(`/projects/${projectId}/rooms/${roomId}/orgUnits`)
        }
    }

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CardHeader
                    title={
                        <Typography variant="h4" component="h2" gutterBottom align="center">
                            Edit OrgUnit
                        </Typography>
                    }
                />
                <CardContent>
                    <form onSubmit={handleSubmit}>
                        {/* OrgUnit Name */}
                        <TextField
                            label="OrgUnit Name"

                            id="orgUnitName"
                            name="name"
                            defaultValue={orgUnit.name}

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
                            defaultValue={orgUnit.description}

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

                    {/* Delete button with a confirmation dialog */}
                    <DeleteOrgUnitButton orgUnit={orgUnit} isDisabled={updateLoading}/>

                </CardContent>
            </Card>
        </Container>
    )
}

export default EditOrgUnit