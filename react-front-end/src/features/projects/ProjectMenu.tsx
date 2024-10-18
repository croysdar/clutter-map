import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { Project } from '@/features/projects/projectsTypes';
import { useNavigate } from 'react-router-dom';


type ProjectMenuProps = {
    project: Project
}

const ProjectMenu: React.FC<ProjectMenuProps> = ({ project }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)
    const navigate = useNavigate();

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();
        event.stopPropagation();
        setAnchorEl(event.currentTarget);
    }
    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleEdit = (event: React.MouseEvent<HTMLElement>) => {
        event.preventDefault();
        event.stopPropagation();
        navigate(`/projects/${project.id}/edit`)
        handleClose();
    }

    return (
        <>
            <Tooltip title="Settings">
                <IconButton
                    onClick={handleClick}
                >
                    <MoreVert />
                </IconButton>
            </Tooltip>
            <Menu
                anchorEl={anchorEl}
                open={open}
                id={`project-${project.id}-menu`}
                onClose={handleClose}
            // onClick={handleClose}

            >
                {/* 
                Edit Project - takes you to an edit page?
                Add location - takes you to 'new location' page?
            */}
                <MenuItem onClick={handleEdit}>
                    Edit Project
                </MenuItem>

            </Menu>
        </>
    );
}

export default ProjectMenu;