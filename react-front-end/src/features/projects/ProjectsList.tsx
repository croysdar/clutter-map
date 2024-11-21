import React from 'react';

import {
    CircularProgress,
    Paper
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { TileWrapper } from '@/components/common/TileWrapper';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPage';
import { PROJECT_LIMIT } from '@/utils/constants';
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

    const addUrl = "/projects/add"

    const handleClick = (e: any, projectId: number) => {
        e.preventDefault();

        navigate(`/projects/${projectId}/rooms`)
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
                        title={project.name}
                        id={project.id}
                        onClick={(e: React.MouseEvent<HTMLDivElement>) => handleClick(e, project.id)}
                    />
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='project'
                to={addUrl}
                disabled={projects.length >= PROJECT_LIMIT}
            />
        </>
    );
};

export default ProjectsList;