import React from "react";

import AppTextField from "./common/AppTextField";

interface QuantityFieldProps {
    quantity: number;
    onQuantityChange: (quantity: number) => void;
}

export const QuantityField: React.FC<QuantityFieldProps> = ({ quantity, onQuantityChange }) => {
    const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;

        // Validate input
        const isValid = (/^\d+$/.test(value) && parseInt(value, 10) > 0);

        // Update state only if valid
        if (isValid) {
            onQuantityChange(parseInt(value));
        }
    };

    return (
        <AppTextField
            label="Quantity"

            id="itemQuantity"
            name="quantity"

            value={quantity}
            onChange={handleQuantityChange}
            required

            inputProps={{
                inputMode: "numeric", // for mobile keyboard
            }}
        />
    );
}
