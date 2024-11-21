import React from 'react'

import { Card, CircularProgress, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import DeleteEntityButtonWithModal from '@/components/buttons/DeleteEntityButtonWithModal'
import AppTextField from '@/components/forms/AppTextField'
import CancelButton from '@/components/forms/CancelButton'
import SubmitButton from '@/components/forms/SubmitButton'
import { EditCardWrapper } from '@/components/pageWrappers/EditPageWrapper'
import { useDeleteProjectMutation, useGetProjectQuery, useUpdateProjectMutation } from './projectApi'
import { Project } from './projectsTypes'

interface EditProjectFormFields extends HTMLFormControlsCollection {
    projectName: HTMLInputElement,
}

interface EditProjectFormElements extends HTMLFormElement {
    readonly elements: EditProjectFormFields
}

const EditProject = () => {
    const navigate = useNavigate();
    const { projectId } = useParams();
    const redirectUrl = '/projects';

    const { data: project, isLoading: projectLoading } = useGetProjectQuery(projectId!);

    const [
        updateProject,
        { isLoading: updateLoading }
    ] = useUpdateProjectMutation();

    if (projectLoading) {
        return (
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Card>
        )
    }

    if (!project) {
        return (
            <section>
                <Typography variant='h2'>Project not found!</Typography>
            </section>
        )
    }

    const handleSubmit = async (e: React.FormEvent<EditProjectFormElements>) => {
        e.preventDefault()

        const { elements } = e.currentTarget
        const name = elements.projectName.value

        if (project && name) {
            await updateProject({ id: project.id, name: name })
            navigate(redirectUrl)
        }
    }

    return (
        <EditCardWrapper title="Edit Project">
            <form onSubmit={handleSubmit}>
                {/* Project Name */}
                <AppTextField
                    label="Project Name"

                    id="projectName"
                    name="name"
                    defaultValue={project.name}

                    required
                />

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
            <DeleteButton
                project={project}
                isDisabled={updateLoading}
                redirectUrl={redirectUrl}
            />
        </EditCardWrapper>
    )
}

type DeleteButtonProps = {
    project: Project,
    isDisabled: boolean
    redirectUrl: string
}

const DeleteButton: React.FC<DeleteButtonProps> = ({ project, isDisabled, redirectUrl }) => {
    return (
        <DeleteEntityButtonWithModal
            entity={project}
            id={project.id}
            name={project.name}
            entityType='Project'
            extraWarning='This will delete all rooms, organizers and items within.'
            mutation={useDeleteProjectMutation}
            isDisabled={isDisabled}
            redirectUrl={redirectUrl}
        />
    );
}

export default EditProject