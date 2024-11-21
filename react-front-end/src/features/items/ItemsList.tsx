import React from 'react';

import {
    CircularProgress,
    Paper
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPage';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useGetItemsByOrgUnitQuery } from './itemApi';
import ItemMenu from './ItemMenu';

const ItemsList: React.FC = () => {
    const { projectId, roomId, orgUnitId } = useParams();
    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);
    const addUrl = `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`

    const {
        data: items = [],
        isLoading,
        isError,
        error
    } = useGetItemsByOrgUnitQuery(orgUnitId!);

    if (isLoading) {
        return (
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Paper>
        );
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    if (!orgUnit) {
        return <div>Organizer not found.</div>
    }

    return (
        <>
            <ListViewTileWrap title={orgUnit.name} subtitle={orgUnit.description} menu={<OrgUnitMenu orgUnit={orgUnit} />}>
                {items.map((item) => (
                    <TileWrapper
                        // TODO make item details page
                        title={item.name}
                        id={item.id}
                        elementLeft={<ItemMenu item={item} />}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='Item'
                to={addUrl}
            />
        </>
    );
};

export default ItemsList;