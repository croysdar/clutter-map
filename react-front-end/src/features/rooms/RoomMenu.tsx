import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { Room } from '@/features/rooms/roomsSlice';
import { useNavigate } from 'react-router-dom';


type RoomMenuProps = {
    room: Room
}

const RoomMenu: React.FC<RoomMenuProps> = ({ room }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)
    const navigate = useNavigate();

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    }
    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleEdit = () => {
        navigate(`/rooms/${room.id}/edit`)
        handleClose();
    }

    const handleAddLocation = () => {
        // TODO add location addition functionality
        alert(room.id);
        handleClose();
    }

    return (
        <>
            <Tooltip title="Settings">
                <IconButton
                    onClick={handleClick}
                >
                    <MoreVert />
                </IconButton>
            </Tooltip>
            <Menu
                anchorEl={anchorEl}
                open={open}
                id={`room-${room.id}-menu`}
                onClose={handleClose}
            // onClick={handleClose}

            >
                {/* 
                Edit Room - takes you to an edit page?
                Add location - takes you to 'new location' page?
            */}
                <MenuItem onClick={handleEdit}>
                    Edit Room
                </MenuItem>
                <MenuItem onClick={handleAddLocation}>
                    Add Location
                </MenuItem>

            </Menu>
        </>
    );
}

export default RoomMenu;