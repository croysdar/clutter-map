import React from 'react';
import ButtonLink, { ButtonLinkProps } from './ButtonLink';

import AddIcon from '@mui/icons-material/Add'

type CreateNewObjectButtonProps = ButtonLinkProps & {
    objectLabel: string;
}

const CreateNewObjectButton: React.FC<CreateNewObjectButtonProps> = ({ objectLabel, ...props }) => {
    return (
        <ButtonLink
            element={
                <span style={{display: 'flex', gap: 1, alignItems: 'center'}}>
                    <AddIcon fontSize='small' /> {`Create a new ${objectLabel}`}
                </span>
        }
            sx={{ marginTop: 3 }}
            variant="text"
            {...props}
        />
    )
}

export default CreateNewObjectButton;