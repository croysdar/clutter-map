import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { OrgUnit } from './orgUnitsSlice';
import { DeleteForever } from '@mui/icons-material';
import { useDeleteOrgUnitMutation } from '../api/apiSlice';
import { useNavigate, useParams } from 'react-router-dom';

type DeleteOrgUnitProps = {
    orgUnit: OrgUnit,
    isDisabled: boolean | undefined
}

const DeleteOrgUnitButton: React.FC<DeleteOrgUnitProps> = ({ orgUnit, isDisabled }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();
    const { roomId } = useParams();
    const { projectId } = useParams();

    const [deleteOrgUnit] = useDeleteOrgUnitMutation();

    const handleDelete = async () => {
        await deleteOrgUnit(orgUnit.id);
        // redirect to [this room]/org-units
        navigate(`/projects/${projectId}/rooms/${roomId}/org-units`)
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
                DELETE ORG UNIT
                <DeleteForever />
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-orgUnit-${orgUnit.id}-dialog`}>
                <DialogTitle>Delete {orgUnit.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this orgUnit? </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete the orgUnit</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteOrgUnitButton