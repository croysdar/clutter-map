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
import RoomMenu from '@/features/rooms/RoomMenu';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetProjectQuery } from '../projects/projectApi';
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
        <>
            <Button
                href={`/projects`}
                variant="text"
                sx={{ marginBottom: 2, fontSize: '0.875rem' }}
            >
                Projects List
            </Button>
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <Typography variant="h1">
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
        </>
    );
};

export default RoomsList;