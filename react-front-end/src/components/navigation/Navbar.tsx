import React from 'react';

import { AppBar, Box, Button, Toolbar, Typography, useMediaQuery } from '@mui/material';
import { Link } from 'react-router-dom';

import { logoutUser, selectAuthStatus, selectCurrentUserFirstName, selectCurrentUserName } from '@/features/auth/authSlice';
import { useAppSelector } from '@/hooks/useAppHooks';
import NavMenuDrawer from './NavMenuDrawer';

import Logo from '@/assets/images/logo.svg';
import { ROUTES } from '@/utils/constants';
import OnlineIndicator from '../common/OnlineIndicator';

const Navbar: React.FC = () => {
    const isMobile = useMediaQuery('(max-width: 600px)');

    const userName = useAppSelector(selectCurrentUserName);
    const userFirstName = useAppSelector(selectCurrentUserFirstName);

    const loggedIn = useAppSelector(selectAuthStatus) === 'verified';

    const handleLogout = () => {
        logoutUser();
    }

    return (
        <AppBar position="static" >
            <Toolbar sx={{ justifyContent: 'space-between', height: '50px' }}>

                {/* Drawer and Logo */}
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <NavMenuDrawer />
                    <Link to={ROUTES.home} style={{ textDecoration: 'none', color: 'inherit' }}>
                        <Box sx={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1
                        }}>
                            <Box sx={{ height: '30px', display: 'flex' }} >
                                <img src={Logo} alt="Clutter Map Logo" />
                            </Box>
                            <Typography variant="h6" sx={{ whiteSpace: 'nowrap' }} >
                                Clutter Map
                            </Typography>
                        </Box>
                    </Link>
                </Box>

                {/* Greeting and Logout Button */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <OnlineIndicator />
                    {
                        loggedIn &&
                        <>
                            {!isMobile &&
                                <Typography variant="body1" sx={{ marginRight: 2 }}>
                                    {`Hello, ${userFirstName || userName}`}
                                </Typography>
                            }

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
