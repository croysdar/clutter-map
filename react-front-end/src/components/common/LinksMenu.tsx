import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { useNavigate } from 'react-router-dom';

type MenuProps = {
    menuItems: LinkMenuItem[]
}

export interface LinkMenuItem {
    label: string
    // icon?:
    url: string
}

const LinksMenu: React.FC<MenuProps> = ({ menuItems }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

    const open = Boolean(anchorEl)
    const navigate = useNavigate();

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();
        event.stopPropagation();
        setAnchorEl(event.currentTarget);
    }

    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleNavigationClick = (event: React.MouseEvent<HTMLElement>, url: string) => {
        event.preventDefault();
        event.stopPropagation();
        navigate(url);
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
                id={`menu`}
                onClose={handleClose}
            >
                {menuItems.map((item, index) =>
                    <MenuItem key={`menu-item-${index}`} onClick={(e) => handleNavigationClick(e, item.url)}>
                        {item.label}
                    </MenuItem>
                )}
            </Menu>
        </>
    );
}

export default LinksMenu;