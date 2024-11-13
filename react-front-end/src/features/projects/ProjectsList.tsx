import React from 'react';

import {
    Card,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { ListViewTileWrap } from '@/pages/ListViewPage';
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
                    <Card key={`project-card-${project.id}`} sx={{ width: '100%' }}>
                        <CardHeader
                            title={<Typography variant='h6'> {project.name}</Typography>}
                            onClick={(e) => handleClick(e, project.id)}
                        />
                    </Card>
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='project'
                to="/projects/add"
                disabled={projects.length >= PROJECT_LIMIT}
            />
        </>
    );
};

export default ProjectsList;