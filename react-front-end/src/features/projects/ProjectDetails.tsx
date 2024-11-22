import React from 'react';

import {
    CircularProgress,
    Paper
} from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPageWrapper';
import { useNavigate, useParams } from 'react-router-dom';
import { useGetProjectQuery } from './projectApi';
import ProjectMenu from './ProjectMenu';
import { useGetRoomsByProjectQuery } from '../rooms/roomApi';

const ProjectDetails: React.FC = () => {
    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(projectId!);

    const addUrl = `/projects/${projectId}/rooms/add`

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
            <ListViewTileWrap
                title={project.name}
                menu={<ProjectMenu project={project} />}
                count={rooms.length}
            >
                {rooms.map((room) => (
                    <TileWrapper
                        key={`tile-wrapper-org-unit-${room.id}`}
                        title={room.name}
                        id={room.id}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, room.id)}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewEntityButton
                objectLabel='room'
                to={addUrl}
            />
        </>
    );
};

export default ProjectDetails;