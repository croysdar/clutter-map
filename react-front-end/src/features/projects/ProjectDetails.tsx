import React from 'react';

import { useNavigate, useParams } from 'react-router-dom';

import {
    CircularProgress
} from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { DetailsPagePaper, TileListWrapper } from '@/components/pageWrappers/ListViewPageWrapper';
import ProjectMenu from '@/features/projects/ProjectMenu';

import { ROUTES } from '@/utils/constants';

import { useGetRoomsByProjectQuery } from '@/features/rooms/roomApi';
import { useGetProjectQuery } from '@/features/projects/projectApi';


const ProjectDetails: React.FC = () => {
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

        navigate(ROUTES.roomDetails(projectId!, roomId))
    }

    if (isLoading) {
        return (
            <DetailsPagePaper title="">
                <CircularProgress />
            </DetailsPagePaper>
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
            <DetailsPagePaper
                title={project.name}
                menu={<ProjectMenu project={project} />}
            >
                <TileListWrapper count={rooms.length} >
                    {rooms.map((room) => (
                        <TileWrapper
                            key={`tile-wrapper-org-unit-${room.id}`}
                            title={room.name}
                            id={room.id}
                            onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, room.id)}
                        />
                    ))}
                </TileListWrapper>
            </DetailsPagePaper>
            <CreateNewEntityButton
                objectLabel='room'
                to={ROUTES.roomAdd(projectId!)}
            />
        </>
    );
};

export default ProjectDetails;