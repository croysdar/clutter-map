import React from 'react';

import { Button, ButtonProps } from '@mui/material';
import { Link } from 'react-router-dom';

type ButtonLinkProps = Omit<ButtonProps, 'href'> & {
    to:  string,
    label?: string
    element?: React.ReactNode
}

const ButtonLink: React.FC<ButtonLinkProps> = ({ to, label, element, ...buttonProps }) => {

    return (
        <Button component={Link} to={to} variant="contained" {...buttonProps}>
            {element ? element : label}
        </Button>
    )
}

export default ButtonLink;
