import React from 'react';

import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

import { rejectAuthStatus, selectAuthStatus, selectCurrentUserFirstName, selectCurrentUserName } from '@/features/auth/authSlice';
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';

const Navbar: React.FC = () => {
    const userName = useAppSelector(selectCurrentUserName);
    const userFirstName = useAppSelector(selectCurrentUserFirstName);
    const dispatch = useAppDispatch();

    const loggedIn = useAppSelector(selectAuthStatus) === 'verified';

    const handleLogout = () => {
        dispatch(rejectAuthStatus());
    }

    return (
        <AppBar position="static" >
            <Toolbar sx={{ justifyContent: 'space-between' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>

                    {/* 
                    // TODO add pages like About and Settings so that this button
                    // can lead somewhere
                    <IconButton
                    edge="start"
                    color="inherit"
                    aria-label="menu"
                    sx={{ mr: 2 }}
                >
                    <MenuIcon />
                </IconButton> */}

                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
                            Clutter Map
                        </Link>
                    </Typography>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>

                    {
                        loggedIn &&
                        <>
                            {/*  TODO make this use first name - requires changing backend to store first name as well as full name */}
                            <Typography variant="body1" sx={{ marginRight: 2 }}>
                                {`Hello, ${userFirstName || userName}`}
                            </Typography>

                            <Button color="inherit" onClick={handleLogout}>
                                Logout
                            </Button>
                        </>
                    }
                </Box>
            </Toolbar>
        </AppBar>
    );
};

export default Navbar;
