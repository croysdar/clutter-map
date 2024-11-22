import React from 'react'

import { Card, CircularProgress, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import SubmitButton from '@/components/forms/SubmitButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'
import { useDeleteOrgUnitMutation, useGetOrgUnitQuery, useUpdateOrgUnitMutation } from './orgUnitApi'
import { OrgUnit } from './orgUnitsTypes'
import { ROUTES } from '@/utils/constants'

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
    const redirectUrl = ROUTES.orgUnitDetails(projectId!, roomId!, orgUnitId!);

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
            navigate(redirectUrl)
        }
    }


    return (
        <EditCardWrapper title="Edit Organizer">
            <form onSubmit={handleSubmit}>
                {/* OrgUnit Name */}
                <AppTextField
                    label="OrgUnit Name"

                    id="orgUnitName"
                    name="name"
                    defaultValue={orgUnit.name}

                    required
                />

                {/* OrgUnit Description */}
                <AppTextField
                    label="OrgUnit Description"

                    id="orgUnitDescription"
                    name="description"
                    defaultValue={orgUnit.description}

                    multiline
                    rows={4}
                />

                {/* Submit Button */}
                <SubmitButton
                    disabled={updateLoading}
                    label="Save Changes"
                />
            </form>

            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />

            {/* Delete button with a confirmation dialog */}
            <DeleteOrgUnitButton
                orgUnit={orgUnit}
                isDisabled={updateLoading}
                redirectUrl={redirectUrl}
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    orgUnit: OrgUnit,
    isDisabled: boolean
    redirectUrl: string
}

const DeleteOrgUnitButton: React.FC<DeleteButtonProps> = ({ orgUnit, isDisabled, redirectUrl }) => {
    return (
        <DeleteEntityButtonWithModal
            entity={orgUnit}
            id={orgUnit.id}
            name={orgUnit.name}
            entityType='Organizer'
            extraWarning='This will send all items within the organizer to the Clutter Stash.'
            mutation={useDeleteOrgUnitMutation}
            isDisabled={isDisabled}
            redirectUrl={redirectUrl}
        />
    );
}

export default EditOrgUnit