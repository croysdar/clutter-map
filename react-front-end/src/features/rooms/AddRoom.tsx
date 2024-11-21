import React from 'react';

import AppTextField from '@/components/common/AppTextField';
import { AddNewCardWrapper } from '@/components/pageWrappers/AddNewPage';
import { Button } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetProjectQuery } from '../projects/projectApi';
import { useAddNewRoomMutation } from './roomApi';

interface AddRoomFormFields extends HTMLFormControlsCollection {
    roomName: HTMLInputElement,
    roomDescription: HTMLTextAreaElement
}

interface AddRoomFormElements extends HTMLFormElement {
    readonly elements: AddRoomFormFields
}

export const AddRoom = () => {
    const [addNewRoom, { isLoading }] = useAddNewRoomMutation()
    const navigate = useNavigate()
    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(projectId!);

    const handleSubmit = async (e: React.FormEvent<AddRoomFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.roomName.value
        const description = elements.roomDescription.value

        const form = e.currentTarget

        if (!project) {
            console.log("Project not found")
            return
        }

        try {
            await addNewRoom({ name, description, projectId }).unwrap()
            form.reset()

            // redirect to [this project]/rooms
            navigate(`/projects/${projectId}/rooms`)
        } catch (err) {
            console.error("Failed to create the room: ", err)
        }
    }

    return (

        <AddNewCardWrapper title="Add a New Room">
            <form onSubmit={handleSubmit}>
                {/* Room Name */}
                <AppTextField
                    label="Room Name"

                    id="roomName"
                    name="name"

                    required
                />

                {/* Room Description */}
                <AppTextField
                    label="Room Description"

                    id="roomDescription"
                    name="description"

                    multiline
                    rows={4}
                />

                {/* Submit Button */}
                <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    fullWidth
                    sx={{ marginTop: 2 }}
                    disabled={isLoading}
                >
                    Create Room
                </Button>
            </form>
            <Button
                variant="text"
                fullWidth
                sx={{ marginTop: 2 }}
                onClick={() => navigate(`/projects/${projectId}/rooms`)}

            >
                Cancel
            </Button>
        </AddNewCardWrapper>
    )
}