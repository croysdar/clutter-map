import React from 'react';

import { Button, ButtonProps } from '@mui/material';
import { Link } from 'react-router-dom';

type ButtonLinkProps = Omit<ButtonProps, 'href'> & {
    to:  string,
    label: string
}

const ButtonLink: React.FC<ButtonLinkProps> = ({ to, label, ...buttonProps }) => {

    return (
        <Button component={Link} to={to} variant="contained" {...buttonProps}>
            {label}
        </Button>
    )
}

export default ButtonLink;
