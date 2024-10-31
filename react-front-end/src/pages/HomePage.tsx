import React from 'react'
import ButtonLink from '@/components/common/ButtonLink'
import { CircularProgress, Container, Typography } from '@mui/material'
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';
import { selectAuthStatus, verifyToken } from '@/features/auth/authSlice';
import { CredentialResponse, GoogleLogin } from '@react-oauth/google';

const HomePage: React.FC = () => {
    const authStatus = useAppSelector(selectAuthStatus);
    const dispatch = useAppDispatch();

    const handleSuccess = async (cred: CredentialResponse) => {
        const idToken = cred.credential
        if (idToken) {
            await dispatch(verifyToken({ idToken, provider: 'google' }));
        }
    }

    return (
        <Container maxWidth="md" sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh',
            textAlign: 'center',
            gap: 3,
            color: '#E0E0E0'
        }}>
            <Typography variant='h3' sx={{ mb: 2, color: '#FFFFFF' }}>
                Welcome to Clutter Map
            </Typography>
            <Typography variant='h6' sx={{ mb: 1, color: '#B0BEC5', maxWidth: '80%' }}>
                Clutter Map is the ultimate tool to manage your space and map out your
                clutter with ease. Know exactly where everything belongs, making it simple
                to put things away or find what you need. Ideal for any space—from personal
                homes to shared environments—Clutter Map ensures that every item is in its
                place, creating a more seamless experience for everyone who uses it.
            </Typography>
            <Typography variant='subtitle1' sx={{ fontStyle: 'italic', color: '#90A4AE' }}>
                {/* Know where everything belongs. Make finding and storing easier. */}
                Simplify your space. Find what you need, when you need it.
            </Typography>
            {
                authStatus !== 'verified' &&
                <Typography variant='h6' sx={{ color: '#FFFFFF' }}>
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
        </Container>
    )
}

export default HomePage;