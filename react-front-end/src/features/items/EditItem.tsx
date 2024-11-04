import React, { useState } from 'react'

import { Button, Card, CardContent, CardHeader, CircularProgress, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import { TagField } from '@/components/TagField'
import DeleteItemButton from './DeleteItemButton'
import { useGetItemQuery, useUpdateItemMutation } from './itemApi'

interface EditItemFormFields extends HTMLFormControlsCollection {
    itemName: HTMLInputElement,
    itemDescription: HTMLTextAreaElement
}

interface EditItemFormElements extends HTMLFormElement {
    readonly elements: EditItemFormFields
}

const EditItem = () => {
    const navigate = useNavigate();
    const { itemId, projectId, roomId } = useParams();
    const redirectUrl = `/projects/${projectId}/rooms/${roomId}/org-units`

    const { data: item, isLoading: itemLoading } = useGetItemQuery(itemId!);
    const [updateItem, { isLoading: updateLoading }] = useUpdateItemMutation();

    // State to manage tags
    const [tags, setTags] = useState<string[]>(item?.tags || []);

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
            await updateItem({ id: item.id, name: name, description: description, tags: tags })
            // redirect to ...[this org unit]/items
            navigate(redirectUrl)
        }
    }

    const handleCancelClick = () => {
        navigate(redirectUrl)
    }

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardHeader
                title={
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Edit Item
                    </Typography>
                }
            />
            <CardContent>
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
                <DeleteItemButton item={item} isDisabled={updateLoading} redirectUrl={redirectUrl} />

            </CardContent>
        </Card>
    )
}

export default EditItem