import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { Item } from '@/features/items/itemTypes';
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes';
import { Project } from '@/features/projects/projectsTypes';
import { Room } from '@/features/rooms/roomsTypes';
import { DeleteForever } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

type DeleteEntityProps = {
    entity: Project | Room | OrgUnit | Item
    id: number,
    isDisabled: boolean | undefined
    redirectUrl: string
    name: string
    entityType: string
    extraWarning?: string
    mutation: Function
}

const DeleteEntityButtonWithModal: React.FC<DeleteEntityProps> = ({ entity, isDisabled, redirectUrl, name, entityType, mutation, id, extraWarning }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteEntity] = mutation();

    const handleDelete = async () => {
        await deleteEntity(id);
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
                DELETE {entityType.toUpperCase()}
                <DeleteForever />
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-${entityType}-${entity.id}-dialog`}>
                <DialogTitle>Delete {name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this {entityType.toLowerCase()}? {extraWarning} </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteEntityButtonWithModal