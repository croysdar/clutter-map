import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileListWrapper, TileWrapper } from '@/components/common/TileWrapper';
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import RoomMenu from '@/features/rooms/RoomMenu';

/* ------------- Redux ------------- */
import { useGetOrgUnitsByRoomQuery } from '@/features/orgUnits/orgUnitApi';
import { useGetRoomQuery } from '@/features/rooms/roomApi';

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants';

const RoomDetails: React.FC = () => {
    const { projectId, roomId } = useParams();

    const { data: room } = useGetRoomQuery(roomId!);

    const {
        data: orgUnits = [],
        isLoading,
        isError,
        error
    } = useGetOrgUnitsByRoomQuery(roomId!);

    const navigate = useNavigate();
    const handleClick = (e: React.MouseEvent<HTMLDivElement>, orgUnitId: number) => {
        e.preventDefault();
        navigate(ROUTES.orgUnitDetails(projectId!, roomId!, orgUnitId!))
    }

    if (isLoading) {
        return (
            <DetailsPagePaper title="">
                <CircularProgress />
            </DetailsPagePaper>
        );
    }

    if (!room) {
        return (
            <DetailsPagePaper title=''>
                <Typography variant='h2'>Room not found</Typography>
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
                title={room.name}
                subtitle={room.description}
                menu={<RoomMenu room={room} />}
            >
                <TileListWrapper count={orgUnits.length} >
                    {orgUnits.map((orgUnit) => (
                        <TileWrapper
                            key={`tile-wrapper-org-unit-${orgUnit.id}`}
                            title={orgUnit.name}
                            id={orgUnit.id}
                            onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, orgUnit.id)}
                        />
                    ))}
                </TileListWrapper>
            </DetailsPagePaper>
            <CreateNewEntityButton
                objectLabel='Organizer'
                to={ROUTES.orgUnitAdd(projectId!, roomId!)}
            />
        </>
    );
};

export default RoomDetails;