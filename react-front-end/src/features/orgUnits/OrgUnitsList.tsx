import React from 'react';

import {
    Button,
    Card,
    CardContent,
    CardHeader,
    Container,
    Paper,
    Typography
} from '@mui/material';

import ButtonLink from '@/components/common/ButtonLink';
import { useGetOrgUnitsByRoomQuery, useGetProjectQuery, useGetRoomQuery } from '@/features/api/apiSlice';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useParams } from 'react-router-dom';

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
        //Container previous properties: , justifyContent: 'center', alignItems: 'center',
        <Container maxWidth="md" sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', height: '100vh' }}>
            <Button
                href={`/projects`}
                variant="text"
                sx={{ marginBottom: 2, fontSize: '0.875rem' }}
            >
                Projects List
            </Button>
            <Button
                href={`/projects/${projectId}/rooms`}
                variant="text"
                sx={{ marginBottom: 2, fontSize: '0.875rem' }}
            >
                Rooms in this project
            </Button>
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
        </Container>
    );
};

export default OrgUnitsList;