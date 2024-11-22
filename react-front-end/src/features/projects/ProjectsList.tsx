import React from 'react';

import {
    CircularProgress,
    Paper
} from '@mui/material';

import CreateNewEntityButton from '@/components/buttons/CreateNewEntityButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPageWrapper';
import { PROJECT_LIMIT, ROUTES } from '@/utils/constants';
import { useNavigate } from 'react-router-dom';
import { useGetProjectsQuery } from './projectApi';

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
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <CircularProgress />
            </Paper>
        );
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    return (
        <>
            <ListViewTileWrap title="My Projects" count={projects.length} >
                {projects.map((project) => (
                    <TileWrapper
                        key={`tile-wrapper-org-unit-${project.id}`}
                        title={project.name}
                        id={project.id}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, project.id)}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewEntityButton
                objectLabel='project'
                to={addUrl}
                disabled={projects.length >= PROJECT_LIMIT}
            />
        </>
    );
};

export default ProjectsList;