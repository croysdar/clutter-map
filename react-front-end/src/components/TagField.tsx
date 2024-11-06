import { Box, TextField, Chip } from "@mui/material";
import { useState } from "react";

interface TagFieldProps {
    tags: string[];
    onTagsChange: (tags: string[]) => void;
}

export const TagField: React.FC<TagFieldProps> = ({ tags, onTagsChange }) => {
    const [inputValue, setInputValue] = useState("");

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setInputValue(e.target.value);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter" && inputValue.trim() !== "") {
            e.preventDefault();
            onTagsChange([...tags, inputValue.trim()]);
            setInputValue(""); // Clear input field
        }
    };

    const removeTag = (tagToRemove: string) => {
        onTagsChange(tags.filter(tag => tag !== tagToRemove));
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: 300 }}>
            <TextField
                variant="outlined"
                label="Add a tag"
                value={inputValue}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                placeholder="Press Enter to add tag"
            />

            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {tags.map((tag, index) => (
                    <Chip
                        key={index}
                        label={tag}
                        onDelete={() => removeTag(tag)}
                        color="primary"
                    />
                ))}
            </Box>
        </Box>
    );
}

interface RenderTagsProps {
    tags: string[];
}

export const RenderTags: React.FC<RenderTagsProps> = ({ tags }) => {
    return (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {tags.map((tag, index) => (
                <Chip
                    key={index}
                    label={tag}
                    color="primary"
                />
            ))}
        </Box>
    )
}