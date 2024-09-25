import React, { useState } from 'react';

import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { Room } from './roomsSlice';
import { DeleteForever } from '@mui/icons-material';
import { useDeleteRoomMutation } from '../api/apiSlice';
import { useNavigate } from 'react-router-dom';

type DeleteRoomProps = {
    room: Room,
    isDisabled: boolean | undefined
}

const DeleteRoomButton: React.FC<DeleteRoomProps> = ({ room, isDisabled }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();

    const [deleteRoom] = useDeleteRoomMutation();
    
    const handleDelete = async () => {
        await deleteRoom(room.id);
        navigate('/rooms');
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
                DELETE ROOM
                <DeleteForever/>
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-room-${room.id}-dialog`}>
                <DialogTitle>Delete {room.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this room? </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete}color="error">Delete the room</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteRoomButton