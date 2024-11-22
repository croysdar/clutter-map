import React from 'react';

import { useNavigate } from 'react-router-dom';

import AppTextField from '@/components/forms/AppTextField';
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { AddNewCardWrapper } from '@/components/pageWrappers/CreatePageWrapper';

import { useAddNewProjectMutation } from '@/features/projects/projectApi';

import { ROUTES } from '@/utils/constants';

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
    const redirectUrl = ROUTES.projects

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
                <SubmitButton
                    disabled={isLoading}
                    label="Create Project"
                />
            </form>

            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </AddNewCardWrapper>
    )
}