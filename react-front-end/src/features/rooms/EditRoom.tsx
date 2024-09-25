import React from 'react'
import { Card, CardContent, Container, Typography } from '@mui/material'

const EditRoom = () => {

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        Edit Room
                    </Typography>

                </CardContent>
            </Card>
        </Container>
    )
}

export default EditRoom