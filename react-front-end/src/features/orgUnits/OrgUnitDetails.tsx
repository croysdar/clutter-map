import React from 'react';

import {
    CircularProgress
} from '@mui/material';

import { useParams } from 'react-router-dom';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { DetailsPagePaper, TileListWrapper } from '@/components/pageWrappers/ListViewPageWrapper';
import ItemMenu from '@/features/items/ItemMenu';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';

import { ROUTES } from '@/utils/constants';

import { useGetItemsByOrgUnitQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';


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
            <DetailsPagePaper title="">
                <CircularProgress />
            </DetailsPagePaper>
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
            <DetailsPagePaper
                title={orgUnit.name}
                subtitle={orgUnit.description}
                menu={<OrgUnitMenu orgUnit={orgUnit} />}
            >
                <TileListWrapper count={items.length} >
                    {items.map((item) => (
                        <TileWrapper
                            // TODO make item details page
                            key={`tile-wrapper-org-unit-${item.id}`}
                            title={item.name}
                            id={item.id}
                            elementLeft={<ItemMenu item={item} />}
                        />
                    ))}
                </TileListWrapper>
            </DetailsPagePaper>
            <CreateNewEntityButton
                objectLabel='Item'
                to={addUrl}
            />
        </>
    );
};

export default OrgUnitDetails;