import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { Item } from './itemsSlice';
import { DeleteForever } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useDeleteItemMutation } from './itemApi';

type DeleteItemProps = {
    item: Item,
    isDisabled: boolean | undefined
    redirectUrl: string
}

const DeleteItemButton: React.FC<DeleteItemProps> = ({ item, isDisabled, redirectUrl }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteItem] = useDeleteItemMutation();

    const handleDelete = async () => {
        await deleteItem(item.id);
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
                DELETE ITEM
                <DeleteForever />
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-room-${item.id}-dialog`}>
                <DialogTitle>Delete {item.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this item? </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete the item</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteItemButton