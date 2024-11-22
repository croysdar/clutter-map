import React, { useEffect, useState } from 'react'

import { Card, CircularProgress, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import { QuantityField } from '@/components/forms/QuantityField'
import SubmitButton from '@/components/forms/SubmitButton'
import { TagField } from '@/components/forms/TagField'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'
import { useDeleteItemMutation, useGetItemQuery, useUpdateItemMutation } from './itemApi'
import { Item } from './itemTypes'
import { ROUTES } from '@/utils/constants'

interface EditItemFormFields extends HTMLFormControlsCollection {
    itemName: HTMLInputElement,
    itemDescription: HTMLTextAreaElement
    itemQuantity: HTMLInputElement;
}

interface EditItemFormElements extends HTMLFormElement {
    readonly elements: EditItemFormFields
}

const EditItem = () => {
    const navigate = useNavigate();
    const { projectId, roomId, orgUnitId, itemId } = useParams();
    const redirectUrl = ROUTES.itemDetails(projectId!, roomId!, orgUnitId!, itemId!)

    const { data: item, isLoading: itemLoading } = useGetItemQuery(itemId!);
    const [updateItem, { isLoading: updateLoading }] = useUpdateItemMutation();

    // States to manage special input
    const [tags, setTags] = useState<string[]>(item?.tags || []);
    const [quantity, setQuantity] = useState<number>(item?.quantity || 1);

    useEffect(() => {
        if (item?.tags) {
            setTags(item.tags);
        }
        if (item?.quantity) {
            setQuantity(item.quantity);
        }
    }, [item]);

    if (itemLoading) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Card>
        )
    }

    if (!item) {
        return (
            <section>
                <Typography variant='h2'>Item not found!</Typography>
            </section>
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditItemFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.itemName.value
        const description = elements.itemDescription.value

        if (item && name) {
            await updateItem({ id: item.id, name: name, description: description, tags: tags, quantity: quantity })
            // redirect to ...[this org unit]/items
            navigate(redirectUrl)
        }
    }

    return (
        <EditCardWrapper title="Edit Item">
            <form onSubmit={handleSubmit}>
                {/* Item Name */}
                <AppTextField
                    label="Item Name"
                    id="itemName"
                    name="name"
                    defaultValue={item.name}
                    required
                />

                {/* Item Description */}
                <AppTextField
                    label="Item Description"
                    id="itemDescription"
                    name="description"
                    defaultValue={item.description}
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
                    disabled={updateLoading}
                    label="Save Changes"
                />
            </form>

            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />

            {/* Delete button with a confirmation dialog */}
            <DeleteItemButton
                item={item}
                isDisabled={updateLoading}
                redirectUrl={redirectUrl}
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    item: Item,
    isDisabled: boolean
    redirectUrl: string
}

const DeleteItemButton: React.FC<DeleteButtonProps> = ({ item, isDisabled, redirectUrl }) => {
    return (
        <DeleteEntityButtonWithModal
            entity={item}
            id={item.id}
            name={item.name}
            entityType='Item'
            mutation={useDeleteItemMutation}
            isDisabled={isDisabled}
            redirectUrl={redirectUrl}
        />
    );
}

export default EditItem