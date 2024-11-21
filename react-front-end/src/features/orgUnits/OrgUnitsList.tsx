import React from 'react';

import { CircularProgress, Paper } from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPage';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { useNavigate, useParams } from 'react-router-dom';
import RoomMenu from '../rooms/RoomMenu';
import { useGetOrgUnitsByRoomQuery } from './orgUnitApi';

const OrgUnitsList: React.FC = () => {
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
            <ListViewTileWrap title={room.name} subtitle={room.description} menu={<RoomMenu room={room} />}>
                {orgUnits.map((orgUnit) => (
                    <TileWrapper
                        title={orgUnit.name}
                        id={orgUnit.id}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, orgUnit.id)}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='Organizer'
                to={addUrl}
            />
        </>
    );
};

export default OrgUnitsList;