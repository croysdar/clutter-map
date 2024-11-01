import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { Room } from '@/features/rooms/roomsSlice';
import { useNavigate, useParams } from 'react-router-dom';

type RoomMenuProps = {
    room: Room
}

const RoomMenu: React.FC<RoomMenuProps> = ({ room }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)
    const navigate = useNavigate();
    const { projectId } = useParams();

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();
        event.stopPropagation();
        setAnchorEl(event.currentTarget);
    }

    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleEdit = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();
        event.stopPropagation();
        navigate(`/projects/${projectId}/rooms/${room.id}/edit`)
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
            >
                <MenuItem onClick={handleEdit}>
                    Edit Room
                </MenuItem>
            </Menu>
        </>
    );
}

export default RoomMenu;