import React, { useState } from 'react';

import {
    Divider,
    Drawer,
    IconButton,
    List,
    ListItem,
    ListItemText
} from '@mui/material';

import MenuIcon from '@mui/icons-material/Menu';

import { Link } from 'react-router-dom';

const NavMenu = () => {
    const [open, setOpen] = useState<boolean>(false);

    const toggleDrawer = (isOpen: boolean) => (event: React.MouseEvent<HTMLElement>) => {
        setOpen(isOpen);
    };

    return (
        <>
            <IconButton
                edge="start"
                color="inherit"
                aria-label="menu"
                sx={{ mr: 2 }}
                onClick={toggleDrawer(true)}
            >
                <MenuIcon />
            </IconButton>
            <Drawer anchor="left" open={open} onClose={toggleDrawer(false)}>
                <List sx={{ width: 250 }} onClick={toggleDrawer(false)} >
                    <ListItem component={Link} to="/">
                        <ListItemText primary="Home" />
                    </ListItem>
                    <ListItem component={Link} to="/about">
                        <ListItemText primary="About" />
                    </ListItem>
                    <Divider />
                    <ListItem component={Link} to="/projects">
                        <ListItemText primary="My Projects" />
                    </ListItem>
                    {/* <ListItem component={Link} to="/settings">
                        <ListItemText primary="Settings" />
                    </ListItem> */}
                    {/* <ListItem component={Link} to="/account">
                        <ListItemText primary="My Account" />
                    </ListItem> */}
                </List>
            </Drawer>
        </>
    );
};

export default NavMenu;