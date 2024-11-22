import React from 'react';

import { useNavigate } from 'react-router-dom';

import {
    CircularProgress,
} from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { DetailsPagePaper, TileListWrapper } from '@/components/pageWrappers/ListViewPageWrapper';

import { PROJECT_LIMIT, ROUTES } from '@/utils/constants';

import { useGetProjectsQuery } from '@/features/projects/projectApi';

const ProjectsList: React.FC = () => {
    const {
        data: projects = [],
        isLoading,
        isError,
        error
    } = useGetProjectsQuery();

    const navigate = useNavigate();

    const addUrl = ROUTES.projectAdd

    const handleClick = (e: any, projectId: number) => {
        e.preventDefault();

        navigate(ROUTES.projectDetails(projectId))
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

    return (
        <>
            <DetailsPagePaper title="My Projects" >
                <TileListWrapper count={projects.length}>
                    {projects.map((project) => (
                        <TileWrapper
                            key={`tile-wrapper-org-unit-${project.id}`}
                            title={project.name}
                            id={project.id}
                            onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, project.id)}
                        />
                    ))}
                </TileListWrapper>
            </DetailsPagePaper>
            <CreateNewEntityButton
                objectLabel='project'
                to={addUrl}
                disabled={projects.length >= PROJECT_LIMIT}
            />
        </>
    );
};

export default ProjectsList;