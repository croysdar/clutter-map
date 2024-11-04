import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { DeleteForever } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useDeleteOrgUnitMutation } from './orgUnitApi';
import { OrgUnit } from './orgUnitsSlice';

type DeleteOrgUnitProps = {
    orgUnit: OrgUnit
    isDisabled: boolean | undefined
    redirectUrl: string
}

const DeleteOrgUnitButton: React.FC<DeleteOrgUnitProps> = ({ orgUnit, isDisabled, redirectUrl }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteOrgUnit] = useDeleteOrgUnitMutation();

    const handleDelete = async () => {
        await deleteOrgUnit(orgUnit.id);
        navigate(redirectUrl)
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
                DELETE ORGANIZATIONAL UNIT
                <DeleteForever />
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-orgUnit-${orgUnit.id}-dialog`}>
                <DialogTitle>Delete {orgUnit.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this unit? </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete the unit</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteOrgUnitButton