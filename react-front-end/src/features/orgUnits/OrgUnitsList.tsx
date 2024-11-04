import React from 'react';

import {
    Card,
    CardActions,
    CardContent,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { useParams } from 'react-router-dom';
import ItemsAccordion from '../items/ItemsAccordion';
import { useGetOrgUnitsByRoomQuery } from './orgUnitApi';

const OrgUnitsList: React.FC = () => {
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
                        />
                        <CardContent>
                            <Typography variant="body2" sx={{ mb: 1 }}>{orgUnit.description}</Typography>
                        </CardContent>
                        <CardActions>
                            <ItemsAccordion orgUnitId={orgUnit.id.toString()} />
                        </CardActions>
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