import React from 'react';
import ButtonLink, { type ButtonLinkProps } from '../common/ButtonLink';

import AddIcon from '@mui/icons-material/Add';

type CreateNewEntityButtonProps = ButtonLinkProps & {
    objectLabel: string;
}

const CreateNewEntityButton: React.FC<CreateNewEntityButtonProps> = ({ objectLabel, ...props }) => {
    return (
        <ButtonLink
            element={
                <span style={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                    <AddIcon fontSize='small' /> {`Create a new ${objectLabel}`}
                </span>
            }
            sx={{ marginTop: 3 }}
            variant="text"
            {...props}
        />
    )
}

export default CreateNewEntityButton;