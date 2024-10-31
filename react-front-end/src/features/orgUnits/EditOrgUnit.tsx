import React from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteOrgUnitButton from './DeleteOrgUnitButton'
import { useGetOrgUnitQuery, useUpdateOrgUnitMutation } from './orgUnitApi'

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

    if (orgUnitLoading) {
        return (
            <CircularProgress />
        )
    }

    if (!orgUnit) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <section>
                    <Typography variant='h2'>OrgUnit not found!</Typography>
                </section>
            </Card>
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditOrgUnitFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.orgUnitName.value
        const description = elements.orgUnitDescription.value

        if (orgUnit && name) {
            await updateOrgUnit({ id: orgUnit.id, name: name, description: description })
            // redirect to [this room]/org-units
            navigate(`/projects/${projectId}/rooms/${roomId}/org-units`)
        }
    }

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardHeader
                title={
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Edit Organizational Unit
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
                <DeleteOrgUnitButton orgUnit={orgUnit} isDisabled={updateLoading} />

            </CardContent>
        </Card>
    )
}

export default EditOrgUnit