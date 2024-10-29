import React, { useState } from 'react';

import { MoreVert } from '@mui/icons-material';
import { IconButton, Menu, MenuItem, Tooltip } from '@mui/material';

import { OrgUnit } from '@/features/orgUnits/orgUnitsSlice';
import { useNavigate, useParams } from 'react-router-dom';


type OrgUnitMenuProps = {
    orgUnit: OrgUnit
}

const OrgUnitMenu: React.FC<OrgUnitMenuProps> = ({ orgUnit }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)
    const navigate = useNavigate();
    const { roomId } = useParams();
    const { projectId } = useParams();

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    }
    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleEdit = () => {
        navigate(`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnit.id}/edit`)
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
                id={`orgUnit-${orgUnit.id}-menu`}
                onClose={handleClose}
            >
                <MenuItem onClick={handleEdit}>
                    Edit OrgUnit
                </MenuItem>

            </Menu>
        </>
    );
}

export default OrgUnitMenu;