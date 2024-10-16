import React, { useState } from 'react';

import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Card,
    CardContent,
    CardHeader,
    Container,
    Paper,
    Typography
} from '@mui/material';

import { Location } from '../../types/types';
import ProjectMenu from '@/features/projects/ProjectMenu';
import { useGetProjectsQuery } from '@/features/api/apiSlice';
import ButtonLink from '@/components/common/ButtonLink';
import { Project } from '@/features/projects/projectsTypes';
import { useNavigate } from 'react-router-dom';

const ProjectsList: React.FC = () => {
    const {
        data: projects = [],
        isLoading,
        isError,
        error
    } = useGetProjectsQuery();

    const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
    const open = Boolean(anchorEl)
    const navigate = useNavigate();

    const handleClose = () => {
        setAnchorEl(null);
    }

    const handleClick = (e : any, projectId : number) => {
        e.preventDefault();

        navigate(`/projects/${projectId}/rooms`)
        handleClose();
    }

    if (isLoading) {
        return <div>Loading...</div>;
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    const renderLocation = (location: Location) => {
        return (
            <Accordion>
                <AccordionSummary >
                    <Typography > {location.name} </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <div key={location.id} className="location">
                        <Typography> {location.description} </Typography>
                        <ul>
                            {location.items.map((item) => (
                                <ul key={item.id}>
                                    <Typography>
                                        <strong>{item.name}</strong>: {item.description} (Location: {item.location})
                                    </Typography>
                                </ul>
                            ))}
                        </ul>
                        <ul>
                            {location.subLocations.map((location) => renderLocation(location))}
                        </ul>
                    </div>
                </AccordionDetails>
            </Accordion>
        )
    }

    return (
        <Container maxWidth="md" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
                <Typography variant="h2">
                    Project List
                </Typography>
                {projects.map((project) => (
                    <>
                        <Card key={`project-card-${project.id}`} sx={{marginTop: 3}}>
                            <div key={project.id} >
                                <CardHeader
                                    title={<Typography variant='h4'> {project.name}</Typography>}
                                    action={<ProjectMenu project={project} />}
                                    onClick={(e) => handleClick(e, project.id)}
                                />
                            </div>
                        </Card>
                    </>
                ))}
                <ButtonLink href="/projects/add" label="Create a new Project"/>
            </Paper>
        </Container>
    );
};

export default ProjectsList;