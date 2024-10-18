import React, { useState } from 'react';

import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { Project } from './projectsTypes';
import { DeleteForever } from '@mui/icons-material';
import { useDeleteProjectMutation } from '../api/apiSlice';
import { useNavigate } from 'react-router-dom';

type DeleteProjectProps = {
    project: Project,
    isDisabled: boolean | undefined
}

const DeleteProjectButton: React.FC<DeleteProjectProps> = ({ project, isDisabled }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteProject] = useDeleteProjectMutation();
    
    const handleDelete = async () => {
        await deleteProject(project.id);
        navigate('/projects');
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