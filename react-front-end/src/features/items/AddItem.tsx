import React, { useState } from 'react';

import { QuantityField } from '@/components/QuantityField';
import { TagField } from '@/components/TagField';
import AppTextField from '@/components/common/AppTextField';
import CancelButton from '@/components/common/CancelButton';
import SubmitButton from '@/components/common/SubmitButton';
import { AddNewCardWrapper } from '@/components/pageWrappers/AddNewPage';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useAddNewItemMutation } from './itemApi';

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

    const navigate = useNavigate()
    const { projectId, roomId, orgUnitId } = useParams();
    const redirectUrl = `/projects/${projectId}/rooms/${roomId}/org-units`

    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);

    // States to manage special input
    const [tags, setTags] = useState<string[]>([]);
    const [quantity, setQuantity] = useState<number>(1);

    const handleSubmit = async (e: React.FormEvent<AddItemFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.itemName.value
        const description = elements.itemDescription.value

        const form = e.currentTarget

        if (!orgUnit) {
            console.log("Organizational Unit not found")
            return
        }

        try {
            await addNewItem({ name, description, tags, orgUnitId, quantity }).unwrap()
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
                {/* Item Name */}
                <AppTextField
                    label="Item Name"

                    id="itemName"
                    name="name"

                    required
                />

                {/* Item Description */}
                <AppTextField
                    label="Item Description"

                    id="itemDescription"
                    name="description"

                    multiline
                    rows={4}
                />

                {/* Item Quantity */}
                <QuantityField
                    quantity={quantity}
                    onQuantityChange={setQuantity}
                />

                {/* Item Tags */}
                <TagField tags={tags} onTagsChange={setTags} />

                {/* Submit Button */}
                <SubmitButton
                    disabled={isLoading}
                    label="Create Item"
                />
            </form>
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </AddNewCardWrapper>
    )
}