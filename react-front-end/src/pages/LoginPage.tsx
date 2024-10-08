import React, { useEffect } from 'react'

import { Container, Typography } from '@mui/material'
import { CredentialResponse, GoogleLogin } from '@react-oauth/google'
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks'
import { useNavigate } from 'react-router-dom'
import { selectAuthStatus, verifyToken } from '@/features/auth/authSlice'


const LoginPage: React.FC = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate()

    const handleSuccess = async (cred: CredentialResponse) => {
        const idToken = cred.credential
        if (idToken) {
            await dispatch(verifyToken(idToken));
            navigate('/home');
        }
    }

    // If the user is logged in and verified
    // reroute to home when attempting to view the login page
    const authStatus = useAppSelector(selectAuthStatus);
    useEffect(() => {
        if (authStatus === 'verified') {
            navigate('/home')
        }
    }, [authStatus, navigate])


    return (
        <Container maxWidth="md" sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh'
        }}>
            <Typography variant='h2'>Welcome to Clutter Map</Typography>
            <Typography variant='h5'>You must be logged in to continue</Typography>

            <GoogleLogin
                onSuccess={handleSuccess}
            />
        </Container>
    )
}

export default LoginPage;