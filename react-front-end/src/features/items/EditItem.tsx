import React, { useEffect, useState } from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButton from '@/components/DeleteEntityButton'
import { QuantityField } from '@/components/QuantityField'
import { TagField } from '@/components/TagField'
import { useDeleteItemMutation, useGetItemQuery, useUpdateItemMutation } from './itemApi'
import { EditCardWrapper } from '@/components/pageWrappers/EditPage'

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
    const { itemId, projectId, roomId, orgUnitId } = useParams();
    const redirectUrl = `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items`

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

    const handleCancelClick = () => {
        navigate(redirectUrl)
    }

    return (
        <EditCardWrapper title="Edit Item">
            <form onSubmit={handleSubmit}>
                {/* Item Name */}
                <TextField
                    label="Item Name"
                    id="itemName"
                    name="name"
                    defaultValue={item.name}
                    required
                    fullWidth
                    margin="normal"
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                />

                {/* Item Description */}
                <TextField
                    label="Item Description"
                    id="itemDescription"
                    name="description"
                    defaultValue={item.description}
                    fullWidth
                    multiline
                    rows={4}
                    margin="normal"
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                />

                {/* Item Quantity */}
                <QuantityField
                    quantity={quantity}
                    onQuantityChange={setQuantity}
                />

                {/* Item Tags */}
                <TagField tags={tags} onTagsChange={setTags} />

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
                entity={item}
                id={item.id}
                name={item.name}
                entityType='Item'
                mutation={useDeleteItemMutation}
                isDisabled={updateLoading}
                redirectUrl={redirectUrl}
            />
        </EditCardWrapper>
    )
}

export default EditItem