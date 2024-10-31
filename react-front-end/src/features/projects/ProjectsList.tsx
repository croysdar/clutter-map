import React from 'react';

import {
    Card,
    CardHeader,
    Paper,
    Typography
} from '@mui/material';

import ButtonLink from '@/components/common/ButtonLink';
import ProjectMenu from '@/features/projects/ProjectMenu';
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
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    return (
        <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <Typography variant="h1">
                My Projects
            </Typography>
            {projects.map((project) => (
                <>
                    <Card key={`project-card-${project.id}`} sx={{ marginTop: 3 }}>
                        <CardHeader
                            title={<Typography variant='h4'> {project.name}</Typography>}
                            action={<ProjectMenu project={project} />}
                            onClick={(e) => handleClick(e, project.id)}
                        />
                    </Card>
                </>
            ))}
            <ButtonLink to="/projects/add" label="Create a new Project" disabled={projects.length >= PROJECT_LIMIT} />
        </Paper>
    );
};

export default ProjectsList;