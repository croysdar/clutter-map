import { Card, CardHeader, Typography } from '@mui/material';
import React, { ReactElement } from 'react';

export type WrapperProps = {
    title: string
    id: number
    onClick?: Function
    elementLeft?: ReactElement
    elementRight?: ReactElement
}

export const TileWrapper: React.FC<WrapperProps> = ({ title, onClick, id, elementLeft, elementRight }) => {
    return (
        <Card key={`tile-card-${id}`} sx={{ width: '100%' }}>
            <CardHeader
                sx={{ padding: 0 }}
                title={<Typography variant='body1'> {title}</Typography>}
                onClick={onClick}
                action={elementLeft}
                avatar={elementRight}
            />
        </Card>
    )
}