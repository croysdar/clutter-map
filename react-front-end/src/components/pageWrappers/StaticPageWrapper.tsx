import React from 'react';

import { Box } from '@mui/material';

export type WrapperProps = {
    children: React.ReactNode
}

export const StaticPageWrapper: React.FC<WrapperProps> = ({ children }) => {

    return (
        <Box
            maxWidth="md"
            sx={{
                gap: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
            }}>
            {children}
        </Box>
    )
}