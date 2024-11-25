import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material'

/* ------------- Components ------------- */
import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import { QuantityField } from '@/components/forms/QuantityField'
import SubmitButton from '@/components/forms/SubmitButton'
import { TagField } from '@/components/forms/TagField'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'

/* ------------- Redux ------------- */
import { useDeleteItemMutation, useGetItemQuery, useUpdateItemMutation } from '@/features/items/itemApi'

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants'

/* ------------- Types ------------- */
import { Item } from '@/features/items/itemTypes'

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

    const { data: item, isLoading: itemLoading, isError, error } = useGetItemQuery(itemId!);
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
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!item) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Item not found</Typography>
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
                <AppTextField
                    label="Item Name"
                    id="itemName"
                    name="name"
                    defaultValue={item.name}
                    required
                />

                <AppTextField
                    label="Item Description"
                    id="itemDescription"
                    name="description"
                    defaultValue={item.description}
                    multiline
                    rows={4}
                />

                <QuantityField
                    quantity={quantity}
                    onQuantityChange={setQuantity}
                />

                <TagField tags={tags} onTagsChange={setTags} />

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