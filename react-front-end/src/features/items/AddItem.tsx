import React, { useState } from 'react';

import { QuantityField } from '@/components/forms/QuantityField';
import { TagField } from '@/components/forms/TagField';
import AppTextField from '@/components/forms/AppTextField';
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { AddNewCardWrapper } from '@/components/pageWrappers/CreatePageWrapper';
import { useNavigate } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useAddNewItemMutation, useAddNewUnassignedItemMutation } from './itemApi';
import { ROUTES } from '@/utils/constants';
import { useResolvedParams } from '@/hooks/useResolvedParams';

interface AddItemFormFields extends HTMLFormControlsCollection {
    itemName: HTMLInputElement,
    itemDescription: HTMLTextAreaElement
    itemQuantity: HTMLInputElement;
}

interface AddItemFormElements extends HTMLFormElement {
    readonly elements: AddItemFormFields
}

export const AddItem = () => {
    const [addNewItem, { isLoading }] = useAddNewItemMutation()
    const [addNewUnassignedItem, { isLoading: isLoadingAddNewUnassignedItem }] = useAddNewUnassignedItemMutation()

    const navigate = useNavigate()
    const { projectId, orgUnitId } = useResolvedParams();

    let redirectUrl = ROUTES.projectDetails(projectId!)
    if (orgUnitId) {
        redirectUrl = ROUTES.orgUnitDetails(projectId!, orgUnitId!)
    }

    const { data: orgUnit } = useGetOrgUnitQuery(Number(orgUnitId!));

    // States to manage special input
    const [tags, setTags] = useState<string[]>([]);
    const [quantity, setQuantity] = useState<number>(1);

    const handleSubmit = async (e: React.FormEvent<AddItemFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.itemName.value
        const description = elements.itemDescription.value

        const form = e.currentTarget


        try {
            if (orgUnitId) {
                if (!orgUnit) {
                    console.log("Organizer not found")
                    return
                }
                await addNewItem({ name, description, tags, orgUnitId, quantity }).unwrap()
            }
            else {
                await addNewUnassignedItem({ name, description, tags, quantity }).unwrap()
            }
            form.reset()

            navigate(redirectUrl);
        } catch (err) {
            console.error("Failed to create the item: ", err)
        }
    }

    // TODO change this so that the Org Unit can be chosen from a drop down

    return (
        <AddNewCardWrapper title="Add a New Item">
            <form onSubmit={handleSubmit}>
                <AppTextField
                    label="Item Name"

                    id="itemName"
                    name="name"

                    required
                />

                <AppTextField
                    label="Item Description"

                    id="itemDescription"
                    name="description"

                    multiline
                    rows={4}
                />

                <QuantityField
                    quantity={quantity}
                    onQuantityChange={setQuantity}
                />

                <TagField tags={tags} onTagsChange={setTags} />

                <SubmitButton
                    disabled={isLoading || isLoadingAddNewUnassignedItem}
                    label="Create Item"
                />
            </form>
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </AddNewCardWrapper>
    )
}