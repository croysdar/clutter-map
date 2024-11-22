import React from 'react';

import {
    CircularProgress,
    Typography
} from '@mui/material';

import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import { useParams } from 'react-router-dom';
import { useGetItemQuery } from './itemApi';
import ItemMenu from './ItemMenu';
import { RenderTags } from '@/components/forms/TagField';

const ItemDetails: React.FC = () => {
    const { itemId } = useParams();

    const {
        data: item,
        isLoading,
        isError,
        error
    } = useGetItemQuery(itemId!);

    if (isLoading) {
        return (
            <DetailsPagePaper title=''>
                <CircularProgress />
            </DetailsPagePaper>
        );
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    if (!item) {
        return <div>Item not found.</div>
    }

    return (
        <>
            <DetailsPagePaper
                title={item.name}
                subtitle={item.description}
                menu={<ItemMenu item={item} />}
            >
                <Typography>Quantity: {item.quantity || 1}</Typography>
                <RenderTags tags={item.tags} />
            </DetailsPagePaper>
        </>
    );
};

export default ItemDetails;