import React from 'react';

import { Box, Container, Paper, Typography } from '@mui/material';

export type DetailsPagePaperProps = {
    title: string
    subtitle?: string
    children?: React.ReactNode
    menu?: React.ReactElement
}

export const DetailsPagePaper: React.FC<DetailsPagePaperProps> = ({ title, subtitle, children, menu }) => {

    return (
        <Paper sx={{ width: '100%', boxShadow: 3, pb: 6 }}>
            <Box sx={{ position: 'relative', padding: 4 }}>
                {/* Menu positioned in the top right corner */}
                {menu && (
                    <Box sx={{ position: 'absolute', top: 50, right: 40 }}>
                        {menu}
                    </Box>
                )}

                {/* Centered title and subtitle */}
                <Box sx={{ textAlign: 'center', pt: 2, pb: 4 }}>
                    <Typography variant="h2" key="page-title" sx={{ pr: 4 }}>
                        {title}
                    </Typography>
                    {subtitle && (
                        <Typography variant="subtitle1" key="page-subtitle">
                            {subtitle}
                        </Typography>
                    )}
                </Box>
                {children}
            </Box>
        </Paper>
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

    {/* Children displayed in a responsive grid */ }
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