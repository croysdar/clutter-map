import React, { useState } from 'react';

import { TagField } from '@/components/TagField';
import { Button, Card, CardContent, TextField, Typography } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useAddNewItemMutation } from './itemApi';

interface AddItemFormFields extends HTMLFormControlsCollection {
    itemName: HTMLInputElement,
    itemDescription: HTMLTextAreaElement
}

interface AddItemFormElements extends HTMLFormElement {
    readonly elements: AddItemFormFields
}

export const AddItem = () => {
    const [addNewItem, { isLoading }] = useAddNewItemMutation()

    const navigate = useNavigate()
    const { projectId, roomId, orgUnitId } = useParams();
    const sourcePageUrl = `/projects/${projectId}/rooms/${roomId}/org-units`

    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);

    // State to manage tags
    const [tags, setTags] = useState<string[]>([]);

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
            await addNewItem({ name, description, tags, orgUnitId }).unwrap()
            form.reset()

            navigate(sourcePageUrl);
        } catch (err) {
            console.error("Failed to create the item: ", err)
        }
    }

    // TODO change this so that the Org Unit can be chosen from a drop down

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardContent>
                <Typography variant="h4" component="h2" gutterBottom align="center">
                    Add a New Item
                </Typography>
                <form onSubmit={handleSubmit} style={{ marginTop: '20px' }}>
                    {/* Item Name */}
                    <TextField
                        label="Item Name"

                        id="itemName"
                        name="name"

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
                        disabled={isLoading}
                    >
                        Create Item
                    </Button>
                </form>
                <Button
                    variant="text"
                    fullWidth
                    sx={{ marginTop: 2 }}
                    onClick={() => navigate(sourcePageUrl)}
                >
                    Cancel
                </Button>

            </CardContent>
        </Card>
    )
}