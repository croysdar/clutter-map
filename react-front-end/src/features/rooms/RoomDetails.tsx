import React from 'react';

import { CircularProgress, Paper } from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPageWrapper';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { useNavigate, useParams } from 'react-router-dom';
import RoomMenu from './RoomMenu';
import { useGetOrgUnitsByRoomQuery } from '../orgUnits/orgUnitApi';

const RoomDetails: React.FC = () => {
    const { projectId, roomId } = useParams();
    const addUrl = `/projects/${projectId}/rooms/${roomId}/org-units/add`

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
        navigate(`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items`)
    }

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

    if (!room) {
        return <div>Room not found.</div>
    }

    return (
        <>
            <ListViewTileWrap
                title={room.name}
                subtitle={room.description}
                menu={<RoomMenu room={room} />}
                count={orgUnits.length}
            >
                {orgUnits.map((orgUnit) => (
                    <TileWrapper
                        key={`tile-wrapper-org-unit-${orgUnit.id}`}
                        title={orgUnit.name}
                        id={orgUnit.id}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, orgUnit.id)}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewEntityButton
                objectLabel='Organizer'
                to={addUrl}
            />
        </>
    );
};

export default RoomDetails;