import React from 'react';

import AppTextField from '@/components/common/AppTextField';
import { AddNewCardWrapper } from '@/components/pageWrappers/AddNewPage';
import { Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAddNewProjectMutation } from './projectApi';

interface AddProjectFormFields extends HTMLFormControlsCollection {
    projectName: HTMLInputElement,
    projectDescription: HTMLTextAreaElement
}

interface AddProjectFormElements extends HTMLFormElement {
    readonly elements: AddProjectFormFields
}


export const AddProject = () => {
    const [addNewProject, { isLoading }] = useAddNewProjectMutation()
    const navigate = useNavigate()

    const handleSubmit = async (e: React.FormEvent<AddProjectFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.projectName.value

        const form = e.currentTarget

        try {
            await addNewProject({ name }).unwrap()
            form.reset()

            // redirect to /projects
            navigate('/projects')
        } catch (err) {
            console.log("Failed to create the project: ", err)
        }
    }

    return (
        <AddNewCardWrapper title="Add a New Project">
            <form onSubmit={handleSubmit}>
                {/* Project Name */}
                <AppTextField
                    label="Project Name"

                    id="projectName"
                    name="name"

                    required
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
                    Create Project
                </Button>
            </form>

            <Button
                variant="text"
                fullWidth
                sx={{ marginTop: 2 }}
                onClick={() => navigate('/projects')}

            >
                Cancel
            </Button>
        </AddNewCardWrapper>
    )
}