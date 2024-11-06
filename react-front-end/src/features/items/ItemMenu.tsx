import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { useNavigate, useParams } from 'react-router-dom';
import { Item } from './itemsSlice';

type ItemMenuProps = {
    item: Item
}

const ItemMenu: React.FC<ItemMenuProps> = ({ item }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)

    const navigate = useNavigate();
    const { projectId, roomId, orgUnitId } = useParams();

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
        navigate(`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/${item.id}/edit`)
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
                id={`item-${item.id}-menu`}
                onClose={handleClose}
            >
                <MenuItem onClick={handleEdit}>
                    Edit Item
                </MenuItem>
            </Menu>
        </>
    );
}

export default ItemMenu;