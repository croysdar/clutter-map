import React from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButton from '@/components/DeleteEntityButton'
import { useDeleteOrgUnitMutation, useGetOrgUnitQuery, useUpdateOrgUnitMutation } from './orgUnitApi'
import { EditCardWrapper } from '@/components/pageWrappers/EditPage'

interface EditOrgUnitFormFields extends HTMLFormControlsCollection {
    orgUnitName: HTMLInputElement,
    orgUnitDescription: HTMLTextAreaElement
}

interface EditOrgUnitFormElements extends HTMLFormElement {
    readonly elements: EditOrgUnitFormFields
}

const EditOrgUnit = () => {
    const navigate = useNavigate();
    const { orgUnitId, roomId, projectId } = useParams();
    const sourcePageUrl = `/projects/${projectId}/rooms/${roomId}/org-units`;

    const { data: orgUnit, isLoading: orgUnitLoading } = useGetOrgUnitQuery(orgUnitId!);

    const [
        updateOrgUnit,
        { isLoading: updateLoading }
    ] = useUpdateOrgUnitMutation();

    if (orgUnitLoading) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Card>
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
            navigate(sourcePageUrl)
        }
    }

    const handleCancelClick = () => {
        navigate(sourcePageUrl)
    }

    return (
        <EditCardWrapper title="Edit Organizational Unit">
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
                entity={orgUnit}
                id={orgUnit.id}
                name={orgUnit.name}
                entityType='Organizational Unit'
                mutation={useDeleteOrgUnitMutation}
                extraWarning='This will send all items within the organizer to the Clutter Stash.'
                isDisabled={updateLoading}
                redirectUrl={sourcePageUrl}
            />
        </EditCardWrapper>
    )
}

export default EditOrgUnit