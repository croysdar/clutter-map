import { Card, CardContent, CardHeader, Typography } from '@mui/material';
import React from 'react';

export type WrapperProps = {
    title: string
    subtitle?: string
    children: React.ReactNode
}

export const EditCardWrapper: React.FC<WrapperProps> = ({ title, subtitle, children }) => {

    return (
        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardHeader
                title={
                    <Typography variant="h4" gutterBottom align="center">
                        {title}
                    </Typography>
                }
                subheader={
                    subtitle &&
                    <Typography variant="subtitle1" gutterBottom align="center">
                        {subtitle}
                    </Typography>
                }
            />
            <CardContent>
                {children}
            </CardContent>
        </Card>
    )
}