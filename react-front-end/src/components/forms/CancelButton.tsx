import React from 'react';

import { Button, ButtonProps } from '@mui/material';

export type CancelButtonProps = ButtonProps & {
    label?: string
    element?: React.ReactNode
}

const CancelButton: React.FC<CancelButtonProps> = ({ label, element, ...buttonProps }) => {

    return (
        <Button
            variant="text"
            fullWidth
            sx={{ marginTop: 2 }}
            {...buttonProps}
        >
            {element ? element : label ? label : "Cancel"}
        </Button>
    )
}

export default CancelButton;