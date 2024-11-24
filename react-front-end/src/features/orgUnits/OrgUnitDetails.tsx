import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';

/* ------------- Redux ------------- */
import { useGetItemsByOrgUnitQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants';
import { TileListWrapper } from '@/components/common/TileWrapper';

const OrgUnitDetails: React.FC = () => {
    const { projectId, roomId, orgUnitId } = useParams();
    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);

    const {
        data: items = [],
        isLoading,
        isError,
        error
    } = useGetItemsByOrgUnitQuery(orgUnitId!);

    const navigate = useNavigate();
    const handleClick = (e: React.MouseEvent<HTMLDivElement>, itemId: number) => {
        e.preventDefault();
        navigate(ROUTES.itemDetails(projectId!, roomId!, orgUnitId!, itemId))
    }

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
                            key={`tile-wrapper-org-unit-${item.id}`}
                            title={item.name}
                            id={item.id}
                            onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, item.id)}
                        />
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