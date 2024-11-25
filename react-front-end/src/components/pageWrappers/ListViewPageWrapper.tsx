import React from 'react';

import { Box, Paper, Typography } from '@mui/material';

export type DetailsPagePaperProps = {
    title: string
    subtitle?: string
    children?: React.ReactNode
    menu?: React.ReactElement
}

export const DetailsPagePaper: React.FC<DetailsPagePaperProps> = ({ title, subtitle, children, menu }) => {

    return (
        <Paper sx={{ width: '100%', boxShadow: 3, padding: 4 }}>
            <Box sx={{ position: 'relative', px: 1, pb: 2 }}>
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

                <Box>
                    {children}
                </Box>
            </Box>
        </Paper>
    )
}
