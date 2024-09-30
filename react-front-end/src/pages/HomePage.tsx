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
            height: '100vh'
        }}>
            <Typography variant='h1'>Welcome to Clutter Map</Typography>
            <ButtonLink href="/rooms" label="Rooms" />

        </Container>
    )
}

export default HomePage;