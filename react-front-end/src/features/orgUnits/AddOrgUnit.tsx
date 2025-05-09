import React from 'react';

import { useNavigate } from 'react-router-dom';

import AppTextField from '@/components/forms/AppTextField';
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { AddNewCardWrapper } from '@/components/pageWrappers/CreatePageWrapper';
import { useGetRoomQuery } from '../rooms/roomApi';
import { useAddNewOrgUnitMutation, useAddNewUnassignedOrgUnitMutation } from './orgUnitApi';
import { ROUTES } from '@/utils/constants';
import { useResolvedParams } from '@/hooks/useResolvedParams';

interface AddOrgUnitFormFields extends HTMLFormControlsCollection {
    orgUnitName: HTMLInputElement,
    orgUnitDescription: HTMLTextAreaElement
}

interface AddOrgUnitFormElements extends HTMLFormElement {
    readonly elements: AddOrgUnitFormFields
}

export const AddOrgUnit = () => {
    const [addNewOrgUnit, { isLoading }] = useAddNewOrgUnitMutation()
    const [addNewUnassignedOrgUnit, { isLoading: isLoadingAddNewUnassignedOrgUnit }] = useAddNewUnassignedOrgUnitMutation()

    const navigate = useNavigate()
    const { projectId, roomId } = useResolvedParams();

    let redirectUrl = ROUTES.projectDetails(projectId!)
    if (roomId) {
        redirectUrl = ROUTES.roomDetails(projectId!, roomId!)
    }

    const { data: room } = useGetRoomQuery(Number(roomId!));

    const handleSubmit = async (e: React.FormEvent<AddOrgUnitFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.orgUnitName.value
        const description = elements.orgUnitDescription.value

        const form = e.currentTarget

        try {
            if (roomId) {
                if (!room) {
                    console.log("Room not found")
                    return
                }
                await addNewOrgUnit({ name, description, roomId }).unwrap()
            }
            else {
                await addNewUnassignedOrgUnit({ name, description }).unwrap()
            }
            form.reset()

            navigate(redirectUrl)
        } catch (err) {
            console.error("Failed to create the orgUnit: ", err)
        }
    }

    return (
        <AddNewCardWrapper title="Add a New Organizer">
            <form onSubmit={handleSubmit}>
                <AppTextField
                    label="OrgUnit Name"

                    id="orgUnitName"
                    name="name"

                    required
                />

                <AppTextField
                    label="OrgUnit Description"

                    id="orgUnitDescription"
                    name="description"

                    multiline
                    rows={4}
                />

                <SubmitButton
                    disabled={isLoading || isLoadingAddNewUnassignedOrgUnit}
                    label="Create Organizer"
                />
            </form>
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </AddNewCardWrapper>
    )
}