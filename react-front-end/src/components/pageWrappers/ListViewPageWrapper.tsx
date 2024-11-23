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
        <Paper sx={{ width: '100%', boxShadow: 3 }}>
            <Box sx={{ position: 'relative', px: 1, py: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', flexDirection: 'column', mx: 1 }}>
                    {/* Title and Menu (Top Row) */}
                    <Box
                        sx={{
                            display: 'flex',
                            alignItems: 'center',
                            width: '100%',
                        }}
                    >
                        {/* Spacer for alignment */}
                        {menu && <Box sx={{ flex: 1, width: '40px' }} />}
                        {/* Title */}
                        <Box sx={{ flex: 10, textAlign: 'center', mx: 1 }}>
                            <Typography
                                variant="h2"
                                key="page-title"
                                sx={{
                                    textAlign: 'center',
                                    wordWrap: 'break-word',
                                    overflowWrap: 'break-word',
                                }}
                            >
                                {title}
                            </Typography>
                        </Box>
                        {/* Menu */}
                        {menu &&
                            <Box sx={{
                                flex: 1,
                                width: '40px'
                            }}>
                                {menu}
                            </Box>
                        }
                    </Box>

                    {/* Subtitle */}
                    {subtitle && (
                        <Typography
                            variant="subtitle1"
                            key="page-subtitle"
                            sx={{ textAlign: 'center', mt: 1 }}
                        >
                            {subtitle}
                        </Typography>
                    )}
                </Box>

                <Box sx={{ pb: 2 }}>
                    {children}
                </Box>
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