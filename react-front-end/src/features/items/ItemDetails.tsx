import React from 'react';
import { useParams } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import { RenderTags } from '@/components/forms/TagField';
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import { EntityEventsContainer } from '@/features/events/RenderEvents';
import ItemMenu from '@/features/items/ItemMenu';

/* ------------- Redux ------------- */
import { useGetItemQuery } from '@/features/items/itemApi';

/* ------------- Constants ------------- */
import { ResourceType } from '@/types/types';
import { SearchManager } from '../search/SearchManager';

const ItemDetails: React.FC = () => {
    const { itemId } = useParams();

    const {
        data: item,
        isLoading,
        isError,
        error
    } = useGetItemQuery(Number(itemId)!);

    if (isLoading) {
        return (
            <DetailsPagePaper title=''>
                <CircularProgress />
            </DetailsPagePaper>
        )
    }

    if (!item) {
        return (
            <DetailsPagePaper title=''>
                <Typography variant='h2'>Item not found</Typography>
            </DetailsPagePaper>
        )
    }

    if (isError) {
        return (
            <DetailsPagePaper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </DetailsPagePaper>
        )
    }

    return (
        <>
            <DetailsPagePaper
                title={item.name}
                subtitle={item.description}
                menu={<ItemMenu item={item} />}
            >
                <Typography>Quantity: {item.quantity || 1}</Typography>
                {item.tags &&
                    <RenderTags tags={item.tags} />
                }
                {item.orgUnitName && (
                    <Typography>Location: {item.orgUnitName}</Typography>
                )}
                {item.roomName && (
                    <Typography>Room: {item.roomName}</Typography>
                )}
            </DetailsPagePaper>
            <EntityEventsContainer entityId={item.id} entityType={ResourceType.ITEM} />
            <SearchManager />
        </>
    );
};

export default ItemDetails;