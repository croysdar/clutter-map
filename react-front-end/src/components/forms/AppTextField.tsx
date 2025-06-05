import React from 'react';

import { TextField, type TextFieldProps } from '@mui/material';

export type AppTextFieldProps = TextFieldProps & {
}

const AppTextField: React.FC<AppTextFieldProps> = ({ ...textFieldProps }) => {

    return (
        <TextField
            fullWidth
            margin="normal"
            variant="outlined"
            InputLabelProps={{ shrink: true }}
            {...textFieldProps}
        />
    )
}

export default AppTextField;

