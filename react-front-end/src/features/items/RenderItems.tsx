import React from 'react';

/* ------------- Material UI ------------- */
import { Checkbox, Divider, List, ListItem, ListItemIcon, ListItemText, Typography, useMediaQuery } from '@mui/material';

/* ------------- Components ------------- */
import { TileWrapper } from '@/components/common/TileWrapper';
import { RenderTags } from '@/components/forms/TagField';

/* ------------- Types ------------- */
import { Item } from '@/features/items/itemTypes';

/* ------------- Utils ------------- */
import { truncateText } from '@/utils/utils';

type ItemTileProps = {
    item: Item
    onClick: Function
}

export const ItemTile: React.FC<ItemTileProps> = ({ item, onClick }) => {
    return (
        <TileWrapper
            key={`tile-wrapper-item-${item.id}`}
            title={item.name}
            id={item.id}
            onClick={(e: React.MouseEvent<HTMLDivElement>) => onClick(e, item.id)}
        />
    )
}

type ItemListItemProps = {
    item: Item
    onClick: Function
    checked: boolean
    showCurrentOrgUnit?: boolean
}
export const ItemListItem: React.FC<ItemListItemProps> = ({
    item,
    onClick,
    checked,
    showCurrentOrgUnit
}) => {
    const isMobile = useMediaQuery('(max-width: 600px)');

    const Content = () => {
        if (isMobile) {
            return (
                <ListItemText
                    primary={
                        <Typography variant='body1' >
                            {truncateText(item.name, 50)}
                        </Typography>
                    }
                    secondary={
                        <Typography variant='body2'>
                            {truncateText(item.description, 50)}
                        </Typography>
                    }
                />
            )
        } else {
            return (
                <>
                    <ListItemText sx={{ flex: 1 }}>
                        <Typography variant='body1' >
                            {truncateText(item.name, 50)}
                        </Typography>
                    </ListItemText>
                    {
                        showCurrentOrgUnit &&
                        <ListItemText sx={{ flex: 1 }}>
                            <Typography variant='body1' >
                                {
                                    item.orgUnitName ?
                                        truncateText(item.orgUnitName, 50)
                                        :
                                        "Item is stashed"
                                }
                            </Typography>
                        </ListItemText>
                    }
                    <ListItemText sx={{ flex: 2 }}>
                        <Typography variant='body1'>
                            {truncateText(item.description, 100)}
                        </Typography>
                    </ListItemText>
                    <ListItemText sx={{ flex: 1 }}>
                        <RenderTags tags={item.tags} />
                    </ListItemText>
                </>
            )
        }

    }

    return (
        <ListItem
            key={`list-item-${item.id}`}
            sx={{ alignItems: 'center' }}
        >
            <ListItemIcon>
                <Checkbox
                    checked={checked}
                    onChange={() => onClick(item.id)}
                />
            </ListItemIcon>
            <Content />

        </ListItem>
    )
}

type ItemListWithCheckBoxesProps = {
    items: Item[]
    checkedItems: number[]
    setCheckedItems: Function
    showCurrentOrgUnit?: boolean
}
export const ItemListWithCheckBoxes: React.FC<ItemListWithCheckBoxesProps> = ({
    items,
    checkedItems,
    setCheckedItems,
    showCurrentOrgUnit
}) => {
    const handleCheckItem = (e: React.MouseEvent<HTMLDivElement>, itemId: number) => {
        setCheckedItems((prev: number[]) =>
            prev.includes(itemId) ? prev.filter((id) => id !== itemId) : [...prev, itemId])
    }

    return (
        <List>
            {items.map((item) =>
                <>
                    <ItemListItem
                        item={item}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleCheckItem(e, item.id)}
                        checked={checkedItems.includes(item.id)}
                        showCurrentOrgUnit={showCurrentOrgUnit}
                    />
                    <Divider />
                </>
            )}
        </List>
    )
}