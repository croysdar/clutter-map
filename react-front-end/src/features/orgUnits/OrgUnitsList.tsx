import React from 'react';

import {
    Button,
    Card,
    CardContent,
    CardHeader,
    Paper,
    Typography
} from '@mui/material';

import ButtonLink from '@/components/common/ButtonLink';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { useParams } from 'react-router-dom';
import { useGetProjectQuery } from '../projects/projectApi';
import { useGetOrgUnitsByRoomQuery } from './orgUnitApi';

const OrgUnitsList: React.FC = () => {
    const { roomId } = useParams();

    const { data: room } = useGetRoomQuery(roomId!);

    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(projectId!);

    const {
        data: orgUnits = [],
        isLoading,
        isError,
        error
    } = useGetOrgUnitsByRoomQuery(roomId!);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    if (!room) {
        return <div>Room not found.</div>
    }

    if (!project) {
        return <div>Project not found.</div>
    }


    return (
        <>
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <Typography variant="h2">
                    {room.name}
                </Typography>
                {orgUnits.map((orgUnit) => (
                    <>
                        <Card key={`orgUnit-card-${orgUnit.id}`} sx={{ marginTop: 3 }}>
                            <div key={orgUnit.id} >
                                <CardHeader
                                    title={<Typography variant='h4'> {orgUnit.name}</Typography>}
                                    action={<OrgUnitMenu orgUnit={orgUnit} />}
                                />
                                <CardContent>
                                    <Typography variant="body2">{orgUnit.description}</Typography>
                                </CardContent>
                            </div>
                        </Card>
                    </>
                ))}
                <ButtonLink to={`/projects/${projectId}/rooms/${roomId}/org-units/add`} label="Create a new Organizational Unit" />
            </Paper>
        </>
    );
};

export default OrgUnitsList;