import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from '@mui/material';

/* ------------- Components ------------- */
import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileListWrapper, TileWrapper } from '@/components/common/TileWrapper';
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import { EntityEventsContainer } from '@/features/events/RenderEvents';
import ProjectMenu from '@/features/projects/ProjectMenu';

/* ------------- Redux ------------- */
import { useGetProjectQuery } from '@/features/projects/projectApi';
import { useGetRoomsByProjectQuery } from '@/features/rooms/roomApi';

/* ------------- Constants ------------- */
import { ResourceType } from '@/types/types';
import { ROUTES } from '@/utils/constants';

const ProjectDetails: React.FC = () => {
    const { projectId } = useParams();

    const { data: project } = useGetProjectQuery(Number(projectId!));

    const {
        data: rooms = [],
        isLoading,
        isError,
        error
    } = useGetRoomsByProjectQuery(Number(projectId!));

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

    if (!project) {
        return (
            <DetailsPagePaper title=''>
                <Typography variant='h2'>Project not found</Typography>
            </DetailsPagePaper>
        )
    }

    if (isError) {
        return (
            <DetailsPagePaper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </DetailsPagePaper>
        )
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
            <EntityEventsContainer entityId={project.id} entityType={ResourceType.PROJECT} />
        </>
    );
};

export default ProjectDetails;