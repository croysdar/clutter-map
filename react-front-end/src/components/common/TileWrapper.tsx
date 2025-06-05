import { Card, CardHeader, Container, Typography } from '@mui/material';
import React, { type ReactElement } from 'react';

export type WrapperProps = {
    title: string
    id: number
    onClick?: Function
    elementLeft?: ReactElement
    elementRight?: ReactElement
    isSelected?: boolean
}

export const TileWrapper: React.FC<WrapperProps> = ({
    title,
    onClick,
    id,
    elementLeft,
    elementRight,
    isSelected
}) => {

    const cursor = onClick ? 'pointer' : 'auto';


    return (
        <Card
            key={`tile-card-${id}`}
            sx={{
                width: '100%',
                cursor: cursor,
                backgroundColor: isSelected ? 'rgba(0, 123, 255, 0.1)' : 'inherit', // Highlight selected tiles
                border: isSelected ? '2px solid #007bff' : 'inherit', // Add a border for selected tiles
            }}
        >
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

export type TileWrapperProps = {
    children: React.ReactNode
    count?: number
}

export const TileListWrapper: React.FC<TileWrapperProps> = ({ children, count }) => {

    const getNumColumns = (screenSize: 'xs' | 'sm' | 'md' | 'lg' | 'xl') => {

        const maxColumns = {
            'xs': 1,
            'sm': 2,
            'md': 3,
            'lg': 4,
            'xl': 4
        };

        const columns = Math.min(count ?? maxColumns[screenSize], maxColumns[screenSize]);
        return `repeat(${columns}, 1fr)`;
    }

    return (
        <Container sx={{
            display: 'grid',
            gridTemplateColumns: {
                xs: getNumColumns('xs'),
                sm: getNumColumns('sm'),
                md: getNumColumns('md'),
                lg: getNumColumns('lg'),
                xl: getNumColumns('xl'),
            },
            gap: 3,
            marginTop: 3
        }}>
            {children}
        </Container>
    )
}