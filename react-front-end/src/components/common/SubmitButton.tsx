import React from 'react';

import { Button, ButtonProps } from '@mui/material';

export type SubmitButtonProps = ButtonProps & {
    label?: string
    element?: React.ReactNode
}

const SubmitButton: React.FC<SubmitButtonProps> = ({ label, element, ...buttonProps }) => {

    return (
        <Button
            type="submit"
            variant="contained"
            color="primary"
            fullWidth
            sx={{ marginTop: 2 }}
            {...buttonProps}
        >
            {element ? element : label}
        </Button>
    )
}

export default SubmitButton;