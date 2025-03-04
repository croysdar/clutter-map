import React, { useEffect, useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { Box, IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { useNavigate } from 'react-router-dom';

type MenuProps = {
    menuItems: LinkMenuItem[]
}

export interface LinkMenuItem {
    label: string
    // icon?:
    url: string
    requiresOnline?: boolean
}

const LinksMenu: React.FC<MenuProps> = ({ menuItems }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const [isOnline, setIsOnline] = useState(navigator.onLine);

    const open = Boolean(anchorEl)
    const navigate = useNavigate();

    useEffect(() => {
        const handleOnline = () => setIsOnline(true);
        const handleOffline = () => setIsOnline(false);
        window.addEventListener('online', handleOnline);
        window.addEventListener('offline', handleOffline);

        return () => {
            window.removeEventListener('online', handleOnline);
            window.removeEventListener('offline', handleOffline);
        }
    })

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
        <Box sx={{ display: 'flex', justifyContent: 'right' }}>
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
                {menuItems.map((item, index) => {
                    const isDisabled = item.requiresOnline && !isOnline

                    return (
                        <Tooltip key={`menu-item-${index}`} title={isDisabled ? "This feature requires an internet connection" : ""} >
                            <span>
                                <MenuItem
                                    key={`menu-item-${index}`}
                                    onClick={(e) => handleNavigationClick(e, item.url)}
                                    disabled={isDisabled}
                                >
                                    {item.label}
                                </MenuItem>
                            </span>
                        </Tooltip>
                    )
                })}
            </Menu>
        </Box>
    );
}

export default LinksMenu;