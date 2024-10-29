import React  from 'react';

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
import { useGetProjectQuery } from '@/features/api/baseApiSlice';
import RoomMenu from '@/features/rooms/RoomMenu';
import { useParams, useNavigate } from 'react-router-dom';
import { useGetRoomsByProjectQuery } from './roomApi';

const RoomsList: React.FC = () => {
    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(projectId!);

    const {
        data: rooms = [],
        isLoading,
        isError,
        error
    } = useGetRoomsByProjectQuery(projectId!);

    const navigate = useNavigate();

    const handleClick = (e: any, roomId: number) => {
        e.preventDefault();

        navigate(`/projects/${projectId}/rooms/${roomId}/org-units`)
    }

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>{error.toString()}</div>
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
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <Typography variant="h2">
                    {project.name}
                </Typography>
                {rooms.map((room) => (
                    <>
                        <Card key={`room-card-${room.id}`} sx={{ marginTop: 3 }}>
                            <div key={room.id} >
                                <CardHeader
                                    title={<Typography variant='h4'> {room.name}</Typography>}
                                    action={<RoomMenu room={room} />}
                                    onClick={(e) => handleClick(e, room.id)}
                                />
                                <CardContent>
                                    <Typography variant="body2">{room.description}</Typography>
                                </CardContent>
                            </div>
                        </Card>
                    </>
                ))}
                <ButtonLink to={`/projects/${projectId}/rooms/add`} label="Create a new Room" />
            </Paper>
        </Container>
    );
};

export default RoomsList;