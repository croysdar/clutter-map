import React from 'react';

import {
    CircularProgress,
    Paper
} from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPageWrapper';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from './orgUnitApi';
import { useGetItemsByOrgUnitQuery } from '../items/itemApi';
import ItemMenu from '../items/ItemMenu';
import { ROUTES } from '@/utils/constants';

const OrgUnitDetails: React.FC = () => {
    const { projectId, roomId, orgUnitId } = useParams();
    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);
    const addUrl = ROUTES.itemAdd(projectId!, roomId!, orgUnitId!)

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
            <ListViewTileWrap
                title={orgUnit.name}
                subtitle={orgUnit.description}
                menu={<OrgUnitMenu orgUnit={orgUnit} />}
                count={items.length}
            >
                {items.map((item) => (
                    <TileWrapper
                        // TODO make item details page
                        key={`tile-wrapper-org-unit-${item.id}`}
                        title={item.name}
                        id={item.id}
                        elementLeft={<ItemMenu item={item} />}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewEntityButton
                objectLabel='Item'
                to={addUrl}
            />
        </>
    );
};

export default OrgUnitDetails;