import React, { useState } from 'react';

import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';

import { Room } from './roomsSlice';
import { DeleteForever } from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useDeleteRoomMutation } from './roomApi';

type DeleteRoomProps = {
    room: Room,
    isDisabled: boolean | undefined
}

const DeleteRoomButton: React.FC<DeleteRoomProps> = ({ room, isDisabled }) => {
    const [open, setOpen] = useState<boolean>(false);

    const handleOpen = () => setOpen(true)
    const handleClose = () => setOpen(false)

    const navigate = useNavigate();
    const { projectId } = useParams();

    const [deleteRoom] = useDeleteRoomMutation();

    const handleDelete = async () => {
        await deleteRoom(room.id);
        // redirect to [this project]/rooms
        navigate(`/projects/${projectId}/rooms`)
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
                <DeleteForever />
            </Button>

            {/* Confirmation dialog */}
            <Dialog open={open} onClose={handleClose} id={`delete-room-${room.id}-dialog`}>
                <DialogTitle>Delete {room.name}</DialogTitle>
                <DialogContent>
                    <Typography > Are you SURE you want to delete this room? This will delete all of its organizational units. </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Cancel</Button>
                    <Button onClick={handleDelete} color="error">Delete the room</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default DeleteRoomButton