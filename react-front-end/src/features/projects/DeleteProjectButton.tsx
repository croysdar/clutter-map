import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { DeleteForever } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useDeleteProjectMutation } from './projectApi';
import { Project } from './projectsTypes';

type DeleteProjectProps = {
    project: Project
    isDisabled: boolean | undefined
    redirectUrl: string
}

const DeleteProjectButton: React.FC<DeleteProjectProps> = ({ project, isDisabled, redirectUrl }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteProject] = useDeleteProjectMutation();
    
    const handleDelete = async () => {
        await deleteProject(project.id);
        navigate(redirectUrl);
    }

    return (
        <>

            {/* DELETE Button */}
            <Button
                variant="text"
                color="error"
                fullWidth
                sx={{ marginTop: 2 }}
                disabled={isDisabled}
                onClick={handleOpen}

            >
                DELETE PROJECT
                <DeleteForever/>
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-project-${project.id}-dialog`}>
                <DialogTitle>Delete {project.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this project? This will delete all the rooms it contains. </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete}color="error">Delete the project</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteProjectButton