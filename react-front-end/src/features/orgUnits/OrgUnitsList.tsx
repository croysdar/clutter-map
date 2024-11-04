import React from 'react';

import {
    Card,
    CardContent,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetOrgUnitsByRoomQuery } from './orgUnitApi';

const OrgUnitsList: React.FC = () => {
    const navigate = useNavigate();

    const { projectId, roomId } = useParams();

    const { data: room } = useGetRoomQuery(roomId!);

    const {
        data: orgUnits = [],
        isLoading,
        isError,
        error
    } = useGetOrgUnitsByRoomQuery(roomId!);

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

    const handleClick = (e: any, orgUnitId: number) => {
        e.preventDefault();
        navigate(`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items`)
    }

    return (
        <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <Typography variant="h2" key="room-name">
                {room.name}
            </Typography>
            {orgUnits.map((orgUnit) => (
                <Card key={`orgUnit-card-${orgUnit.id}`} sx={{ marginTop: 3 }}>
                    <div key={orgUnit.id} >
                        <CardHeader
                            title={<Typography variant='h4'> {orgUnit.name}</Typography>}
                            action={<OrgUnitMenu orgUnit={orgUnit} />}
                            onClick={(e) => handleClick(e, orgUnit.id)}
                        />
                        <CardContent>
                            <Typography variant="body2">{orgUnit.description}</Typography>
                        </CardContent>
                    </div>
                </Card>
            ))}
            <CreateNewObjectButton
                objectLabel='Organizational Unit'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/add`}
            />
        </Paper>
    );
};

export default OrgUnitsList;