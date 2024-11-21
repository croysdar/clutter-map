import React from 'react';

import { useNavigate, useParams } from 'react-router-dom';

import AppTextField from '@/components/forms/AppTextField';
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { AddNewCardWrapper } from '@/components/pageWrappers/CreatePageWrapper';
import { useGetRoomQuery } from '../rooms/roomApi';
import { useAddNewOrgUnitMutation } from './orgUnitApi';

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
    const { roomId, projectId } = useParams();
    const redirectUrl = `/projects/${projectId}/rooms/${roomId}/org-units`

    const { data: room } = useGetRoomQuery(roomId!);


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
        <AddNewCardWrapper title="Add a New Organizer">
            <form onSubmit={handleSubmit}>
                {/* OrgUnit Name */}
                <AppTextField
                    label="OrgUnit Name"

                    id="orgUnitName"
                    name="name"

                    required
                />

                {/* OrgUnit Description */}
                <AppTextField
                    label="OrgUnit Description"

                    id="orgUnitDescription"
                    name="description"

                    multiline
                    rows={4}
                />

                {/* Submit Button */}
                <SubmitButton
                    disabled={isLoading}
                    label="Create Organizer"
                />
            </form>
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </AddNewCardWrapper>
    )
}