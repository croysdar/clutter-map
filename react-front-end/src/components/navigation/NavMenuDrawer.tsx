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

import { selectAuthStatus } from '@/features/auth/authSlice';
import { useAppSelector } from '@/hooks/useAppHooks';
import { Link } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';

const NavMenuDrawer = () => {
    const [open, setOpen] = useState<boolean>(false);

    const loggedIn = useAppSelector(selectAuthStatus) === 'verified';

    const toggleDrawer = (isOpen: boolean) => (event: React.MouseEvent<HTMLElement>) => {
        setOpen(isOpen);
    };

    return (
        <>
            <IconButton
                edge="start"
                color="inherit"
                aria-label="menu"
                sx={{ mr: 1 }}
                onClick={toggleDrawer(true)}
            >
                <MenuIcon />
            </IconButton>
            <Drawer anchor="left" open={open} onClose={toggleDrawer(false)}>
                <List sx={{ width: 250 }} onClick={toggleDrawer(false)} >
                    <NavLink label="Home" to={ROUTES.home} />
                    <NavLink label="About" to="/about" />
                    {
                        loggedIn &&
                        <>
                            <Divider />
                            <NavLink label="My Projects" to="/projects" />
                            {/* <NavLink label="Settings" to = "/settings"/>
                            <NavLink label="My Account" to = "/account"/> */}
                        </>
                    }
                </List>
            </Drawer>
        </>
    );
};

interface NavLinkProps {
    label: string;
    to: string;
}

const NavLink: React.FC<NavLinkProps> = ({ label, to }) => {
    return (
        <ListItem
            component={Link}
            to={to}
            sx={{
                color: 'inherit',
                textDecoration: 'none',
                '&:hover': {
                    bgcolor: 'primary.main',
                    color: 'white',
                },
                '&:active': {
                    bgcolor: 'primary.dark',
                    color: 'white',
                }
            }}
        >
            <ListItemText primary={label} />
        </ListItem>

    )
}

export default NavMenuDrawer;