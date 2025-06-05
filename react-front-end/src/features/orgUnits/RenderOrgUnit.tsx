import React from 'react';

/* ------------- Material UI ------------- */
import { Checkbox, Divider, List, ListItem, ListItemIcon, ListItemText, Typography, useMediaQuery } from '@mui/material';

/* ------------- Components ------------- */
import { TileWrapper } from '@/components/common/TileWrapper';

/* ------------- Types ------------- */
import { type OrgUnit } from './orgUnitsTypes';

/* ------------- Utils ------------- */
import { truncateText } from '@/utils/utils';

type OrgUnitTileProps = {
    orgUnit: OrgUnit
    onClick: Function
}
export const OrgUnitTile: React.FC<OrgUnitTileProps> = ({ orgUnit, onClick }) => {
    return (
        <TileWrapper
            key={`tile-wrapper-org-unit-${orgUnit.id}`}
            title={orgUnit.name}
            id={orgUnit.id}
            onClick={(e: React.MouseEvent<HTMLDivElement>) => onClick(e, orgUnit.id)}
        />
    )
}

type OrgUnitListItemProps = {
    orgUnit: OrgUnit
    onClick: Function
    checked: boolean
    showCurrentRoom?: boolean
}
export const OrgUnitListItem: React.FC<OrgUnitListItemProps> = ({
    orgUnit,
    onClick,
    checked,
    showCurrentRoom
}) => {
    const isMobile = useMediaQuery('(max-width: 600px)');

    const Content = () => {
        if (isMobile) {
            return (
                <ListItemText
                    primary={
                        <Typography variant='body1' >
                            {truncateText(orgUnit.name, 50)}
                        </Typography>
                    }
                    secondary={
                        <Typography variant='body2'>
                            {truncateText(orgUnit.description, 50)}
                        </Typography>
                    }
                />
            )
        } else {
            return (
                <>
                    <ListItemText sx={{ flex: 1 }}>
                        <Typography variant='body1' >
                            {truncateText(orgUnit.name, 50)}
                        </Typography>
                    </ListItemText>
                    {
                        showCurrentRoom &&
                        <ListItemText sx={{ flex: 1 }}>
                            <Typography variant='body1' >
                                {
                                    orgUnit.roomName ?
                                        truncateText(orgUnit.roomName, 50)
                                        :
                                        "Organizer is stashed"
                                }
                            </Typography>
                        </ListItemText>
                    }
                    <ListItemText sx={{ flex: 2 }}>
                        <Typography variant='body1'>
                            {truncateText(orgUnit.description, 100)}
                        </Typography>
                    </ListItemText>
                </>
            )
        }

    }

    return (
        <ListItem
            key={`list-orgUnit-${orgUnit.id}`}
            sx={{ alignItems: 'center' }}
        >
            <ListItemIcon>
                <Checkbox
                    checked={checked}
                    onChange={() => onClick(orgUnit.id)}
                />
            </ListItemIcon>
            <Content />
        </ListItem>
    )
}

type OrgUnitListWithCheckBoxesProps = {
    orgUnits: OrgUnit[]
    checkedOrgUnits: number[]
    setCheckedOrgUnits: Function
    showCurrentRoom?: boolean
}
export const OrgUnitListWithCheckBoxes: React.FC<OrgUnitListWithCheckBoxesProps> = ({
    orgUnits,
    checkedOrgUnits,
    setCheckedOrgUnits,
    showCurrentRoom
}) => {

    const handleCheckOrgUnit = (e: React.MouseEvent<HTMLDivElement>, orgUnitId: number) => {
        setCheckedOrgUnits((prev: number[]) =>
            prev.includes(orgUnitId) ? prev.filter((id) => id !== orgUnitId) : [...prev, orgUnitId])
    }

    return (
        <List>
            {orgUnits.map((orgUnit) =>
                <>
                    <OrgUnitListItem
                        orgUnit={orgUnit}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleCheckOrgUnit(e, orgUnit.id)}
                        checked={checkedOrgUnits.includes(orgUnit.id)}
                        showCurrentRoom={showCurrentRoom}
                    />
                    <Divider />
                </>
            )}
        </List>
    )
}
