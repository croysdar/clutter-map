import React from 'react'
import ButtonLink from '@/components/common/ButtonLink'
import { Container, Typography } from '@mui/material'

const HomePage: React.FC = () => {
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
                Know where everything belongs. Make finding and storing easier.
            </Typography>

                <ButtonLink to="/projects" 
                    label="My Projects"
                    disabled={authStatus !== 'verified'}
                    sx={{
                        mt: 3,
                        px: 4,
                        py: 1.5,
                        backgroundColor: '#2196F3',
                        color: '#FFFFFF',
                        fontSize: '1rem',
                        fontWeight: 'bold',
                        boxShadow: '0px 4px 12px rgba(33, 150, 243, 0.3)',
                        '&:hover': {
                            backgroundColor: '#1976D2'
                        }
                    }}
                />
        </Container>
    )
}

export default HomePage;