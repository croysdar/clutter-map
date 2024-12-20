import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { ItemTile } from '@/features/items/RenderItems';
import { TileListWrapper } from '@/components/common/TileWrapper';

/* ------------- Redux ------------- */
import { useGetItemsByOrgUnitQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants';

const OrgUnitDetails: React.FC = () => {
    const { projectId, roomId, orgUnitId } = useParams();
    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);
    const navigate = useNavigate();

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

    if (!orgUnit) {
        return (
            <DetailsPagePaper title="">
                <Typography variant='h2'>Organizer not found</Typography>
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

    const handleClick = (e: React.MouseEvent<HTMLDivElement>, itemId: number) => {
        e.preventDefault();
        navigate(ROUTES.itemDetails(projectId!, roomId!, orgUnitId!, itemId))
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
                        <ItemTile item={item} onClick={handleClick} />
                    ))}
                </TileListWrapper>
            </DetailsPagePaper>
            <CreateNewEntityButton
                objectLabel='Item'
                to={ROUTES.itemAdd(projectId!, roomId!, orgUnitId!)}
            />
        </>
    );
};

export default OrgUnitDetails;