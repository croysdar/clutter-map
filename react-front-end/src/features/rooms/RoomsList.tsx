import React from 'react';

import {
    Card,
    CardContent,
    CardHeader,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
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
        <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <Typography variant="h2" key="project-name">
                {project.name}
            </Typography>
            {rooms.map((room) => (
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
            ))}
            <CreateNewObjectButton
                objectLabel='room'
                to={`/projects/${projectId}/rooms/add`}
            />
        </Paper>
    );
};

export default RoomsList;