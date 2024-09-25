import React from 'react';

import { Button } from '@mui/material';
import { Link } from 'react-router-dom';

type ButtonLinkProps = {
    href:  string,
    label: string
}

const ButtonLink: React.FC<ButtonLinkProps> = ({ href, label }) => {

    return (
        <Button component={Link} to={href} variant="contained">
            {label}
        </Button>
    )


}

export default ButtonLink;
