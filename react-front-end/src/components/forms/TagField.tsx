import { Box, Chip } from "@mui/material";
import { useState } from "react";
import AppTextField from "./AppTextField";

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
            addTag();
        }
    };

    const removeTag = (tagToRemove: string) => {
        onTagsChange(tags.filter(tag => tag !== tagToRemove));
    };

    const handleBlur = () => {
        if (inputValue.trim() !== "") {
            addTag();
        }
    };

    const addTag = () => {
        onTagsChange([...tags, inputValue.trim()]);
        setInputValue(""); // Clear input field
    };

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <AppTextField
                label="Add a tag"
                placeholder="Press Enter to add tag"

                value={inputValue}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                onBlur={handleBlur}
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