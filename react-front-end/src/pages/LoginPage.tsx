import React from 'react'
import { Container, Typography } from '@mui/material'
import { GoogleLogin } from '@react-oauth/google'

const LoginPage: React.FC = () => {

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
                onSuccess={(cred) => {
                    const idToken = cred.credential

                    console.log(cred)

                    // Send the token to your backend
                    fetch("http://localhost:8080/auth/verify-token", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",

                        },
                        body: idToken
                    })
                        .then((response) => response.json()) // Parse the JSON response
                        .then((data) => {
                            console.log("Token verification success:", data);
                        })
                        .catch((error) => {
                            console.error("Token verification failed:", error);
                        });
                }}
            />
        </Container>
    )
}

export default LoginPage;