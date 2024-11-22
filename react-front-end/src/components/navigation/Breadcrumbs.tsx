import React from 'react';

import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';
import { useGetProjectQuery } from '@/features/projects/projectApi';
import { useGetRoomQuery } from '@/features/rooms/roomApi';

import { Breadcrumbs, Link, Typography } from '@mui/material';

import { Link as RouterLink, matchPath, useLocation } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';

const AppBreadcrumbs: React.FC = () => {
    const location = useLocation();

    // Split the pathname into an array
    const pathnames = location.pathname.split('/').filter(x => x);

    const getObjectIdFromPath = (idIndex: number, objectName: string) => {
        let objectId = (pathnames[idIndex - 1] === objectName) ? pathnames[idIndex] : null;

        if (Number.isNaN(Number(objectId))) {
            objectId = null;
        }

        return objectId;
    }

    // Extract project and room IDs from the URL if they exist
    const projectId = getObjectIdFromPath(1, 'projects');
    const roomId = getObjectIdFromPath(3, 'rooms');
    const orgUnitId = getObjectIdFromPath(5, 'org-units');

    const { data: projectData, isLoading: projectLoading } = useGetProjectQuery(projectId ?? '', { skip: !projectId });
    const { data: roomData, isLoading: roomLoading } = useGetRoomQuery(roomId ?? '', { skip: !roomId });
    const { data: orgUnitData, isLoading: orgUnitLoading } = useGetOrgUnitQuery(orgUnitId ?? '', { skip: !orgUnitId });

    const getBreadcrumbName = (path: string) => {
        if (matchPath(ROUTES.projects, path)) {
            return 'Projects';
        }

        if (projectId && path === ROUTES.projectDetails(projectId)) return null;
        if (matchPath("/projects/:projectId/rooms", path)) {
            if (projectLoading)
                return '...'
            return projectData ? projectData.name : `Project ${projectId}`;
        }

        if (roomId && projectId && path === ROUTES.roomDetails(projectId, roomId)) return null;
        if (matchPath("/projects/:projectId/rooms/:roomId/org-units", path)) {
            if (roomLoading)
                return '...'
            return roomData ? roomData.name : `Room ${roomId}`;
        }

        if (orgUnitId && roomId && projectId && path === ROUTES.orgUnitDetails(projectId, roomId, orgUnitId)) return null;
        if (matchPath("/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/items", path)) {
            if (orgUnitLoading)
                return '...'
            return orgUnitData ? orgUnitData.name : `Organizer ${orgUnitId}`;
        }

        // Default fallback: return the last segment of the path
        return path.split('/').pop() || '';
    };

    return (
        <Breadcrumbs aria-label="breadcrumb" separator="›" sx={{ padding: '8px 16px' }} >
            <Link component={RouterLink} to={ROUTES.home} color="inherit" underline='hover'>Home</Link>
            {pathnames.map((value, index) => {
                // Build the URL path up to the current level
                const to = `/${pathnames.slice(0, index + 1).join('/')}`;

                const breadcrumbName = getBreadcrumbName(to);

                if (!breadcrumbName) return null

                const isLast = index === pathnames.length - 1;
                return isLast ? (
                    <Typography key={to}> {breadcrumbName}</Typography>
                ) : (
                    <Typography key={to} color="inherit">
                        <Link component={RouterLink} to={to} underline="hover">{breadcrumbName}</Link>
                    </Typography>
                );
            })}
        </Breadcrumbs>
    )
}

export default AppBreadcrumbs;