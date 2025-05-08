import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material'

/* ------------- Components ------------- */
import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import SubmitButton from '@/components/forms/SubmitButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'

/* ------------- Redux ------------- */
import { useDeleteOrgUnitMutation, useGetOrgUnitQuery, useUpdateOrgUnitMutation } from '@/features/orgUnits/orgUnitApi'

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants'

/* ------------- Types ------------- */
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes'
import { useEntityHierarchy } from '@/hooks/useEntityHierarchy'

interface EditOrgUnitFormFields extends HTMLFormControlsCollection {
    orgUnitName: HTMLInputElement,
    orgUnitDescription: HTMLTextAreaElement
}

interface EditOrgUnitFormElements extends HTMLFormElement {
    readonly elements: EditOrgUnitFormFields
}

const EditOrgUnit = () => {
    const navigate = useNavigate();
    const { orgUnitId, projectId } = useParams();
    const redirectUrl = ROUTES.orgUnitDetails(projectId!, orgUnitId!);

    const { data: orgUnit, isLoading: orgUnitLoading, isError, error } = useGetOrgUnitQuery(Number(orgUnitId!));

    const [
        updateOrgUnit,
        { isLoading: updateLoading }
    ] = useUpdateOrgUnitMutation();

    if (orgUnitLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!orgUnit) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Organizer not found</Typography>
            </EditCardWrapper>
        )
    }

    if (isError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </EditCardWrapper>
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
                <AppTextField
                    label="OrgUnit Name"

                    id="orgUnitName"
                    name="name"
                    defaultValue={orgUnit.name}

                    required
                />

                <AppTextField
                    label="OrgUnit Description"

                    id="orgUnitDescription"
                    name="description"
                    defaultValue={orgUnit.description}

                    multiline
                    rows={4}
                />

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
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    orgUnit: OrgUnit,
    isDisabled: boolean
}

const DeleteOrgUnitButton: React.FC<DeleteButtonProps> = ({ orgUnit, isDisabled }) => {
    const { projectId, orgUnitId } = useParams();

    const { hierarchy, loading: hierarchyLoading } = useEntityHierarchy(
        'orgUnit', Number(orgUnitId)
    );

    const redirectUrl = !hierarchyLoading && hierarchy?.room
        ? ROUTES.roomDetails(projectId!, hierarchy.room.id)
        : ROUTES.projectDetails(projectId!)

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