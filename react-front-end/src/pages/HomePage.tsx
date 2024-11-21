import React from 'react';

import ButtonLink from '@/components/common/ButtonLink';
import { StaticPageWrapper } from '@/components/pageWrappers/StaticPageWrapper';
import { fetchUserInfo, selectAuthStatus, verifyToken } from '@/features/auth/authSlice';
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';
import { CircularProgress, Typography } from '@mui/material';
import { CredentialResponse, GoogleLogin } from '@react-oauth/google';

const HomePage: React.FC = () => {
    const authStatus = useAppSelector(selectAuthStatus);
    const dispatch = useAppDispatch();

    const handleSuccess = async (cred: CredentialResponse) => {
        const idToken = cred.credential
        if (idToken) {
            await dispatch(verifyToken({ idToken, provider: 'google' }));
            const jwt = localStorage.getItem('jwt');
            if (jwt)
                await (dispatch(fetchUserInfo(jwt)));
        }
    }

    return (
        <StaticPageWrapper>
            <Typography variant='h3' sx={{ mb: 2 }}>
                Welcome to Clutter Map
            </Typography>
            <Typography variant='h6' sx={{ mb: 1, maxWidth: '80%' }}>
                Clutter Map is the ultimate tool to manage your space and map out your
                clutter with ease. Know exactly where everything belongs, making it simple
                to put things away or find what you need. Ideal for any space—from personal
                homes to shared environments—Clutter Map ensures that every item is in its
                place, creating a more seamless experience for everyone who uses it.
            </Typography>
            <Typography variant='subtitle1' sx={{ fontStyle: 'italic' }} color="textSecondary">
                {/* Know where everything belongs. Make finding and storing easier. */}
                Simplify your space. Find what you need, when you need it.
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
                    onSuccess={handleSuccess}
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