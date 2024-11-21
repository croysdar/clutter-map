import React from 'react';

import {
    Card,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPage';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetProjectQuery } from '../projects/projectApi';
import ProjectMenu from '../projects/ProjectMenu';
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
        return (
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Paper>
        );
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    if (!project) {
        return <div>Project not found.</div>
    }

    return (
        <>
            <ListViewTileWrap title={project.name} menu={<ProjectMenu project={project} />} >
                {rooms.map((room) => (
                    <Card key={`room-card-${room.id}`} sx={{ width: '100%' }} onClick={(e) => handleClick(e, room.id)} >
                        <CardHeader
                            title={<Typography variant='h6'> {room.name}</Typography>}
                        />
                    </Card>
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='room'
                to={`/projects/${projectId}/rooms/add`}
            />
        </>
    );
};

export default RoomsList;