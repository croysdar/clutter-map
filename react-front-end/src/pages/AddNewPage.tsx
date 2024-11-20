import { Card, CardContent, CardHeader, Typography } from '@mui/material';
import React from 'react';

export type ListViewTileWrapProps = {
    title: string
    children: React.ReactNode
}

export const AddNewCardWrapper: React.FC<ListViewTileWrapProps> = ({ title, children }) => {

    return (

        <Card sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <CardHeader
                title={
                    <Typography variant="h4" component="h2" gutterBottom align="center">
                        {title}
                    </Typography>
                }
            />
            <CardContent>
                {children}
            </CardContent>
        </Card>
    )
}