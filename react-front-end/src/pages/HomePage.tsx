import React from 'react';

import { CredentialResponse, GoogleLogin } from '@react-oauth/google';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import ButtonLink from '@/components/common/ButtonLink';
import { StaticPageWrapper } from '@/components/pageWrappers/StaticPageWrapper';

/* ------------- Redux ------------- */
import { fetchUserInfo, selectAuthStatus, verifyToken } from '@/features/auth/authSlice';
import { syncIDB } from '@/features/offline/syncSlice';
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';

const HomePage: React.FC = () => {
    const authStatus = useAppSelector(selectAuthStatus);
    const dispatch = useAppDispatch();

    const handleLoginSuccess = async (cred: CredentialResponse) => {
        const idToken = cred.credential
        if (idToken) {
            await dispatch(verifyToken({ idToken, provider: 'google' }));
            const jwt = localStorage.getItem('jwt');
            if (jwt) {
                const result = await (dispatch(fetchUserInfo(jwt)));

                // Make sure the user is properly logged in before trying to sync
                if (fetchUserInfo.fulfilled.match(result)) {
                    dispatch(syncIDB(jwt));
                }
            }
        }
    }

    return (
        <StaticPageWrapper>
            <Typography variant='h1' sx={{ mb: 2 }}>
                Welcome to Clutter Map
            </Typography>
            <Typography variant='h6' sx={{ mb: 1 }}>
                Clutter Map is the ultimate tool to manage your space and map out your
                clutter with ease. Know exactly where everything belongs, making it simple
                to put things away or find what you need. Ideal for any space—from personal
                homes to shared environments—Clutter Map ensures that every item is in its
                place, creating a more seamless experience for everyone who uses it.
            </Typography>
            <Typography variant='subtitle1' sx={{ fontStyle: 'italic' }} color="textSecondary">
                {/* Know where everything belongs. Make finding and storing easier. */}
                Find what you need, when you need it.
            </Typography>
            {
                authStatus !== 'verified' &&
                <Typography variant='h6'>
                    Sign in to get started
                </Typography>
            }
            {
                (authStatus === 'none' || authStatus === 'idle') &&
                <GoogleLogin
                    onSuccess={handleLoginSuccess}
                />
            }
            {
                !(authStatus === 'none' || authStatus === 'idle') &&

                <ButtonLink to="/projects"
                    element={authStatus === 'pending' ? <CircularProgress /> : "My Projects"}
                    disabled={authStatus !== 'verified'}
                    sx={{
                        mt: 3,
                        px: 4,
                        py: 1.5,
                        backgroundColor: '#2196F3',
                        color: '#FFFFFF',
                        fontSize: '1rem',
                        '&:hover': {
                            backgroundColor: '#1976D2'
                        }
                    }}
                />
            }
        </StaticPageWrapper>
    )
}

export default HomePage;