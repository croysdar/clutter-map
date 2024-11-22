import React from 'react';

import { Link as RouterLink, matchPath, useLocation } from 'react-router-dom';

import { Breadcrumbs, Link, Typography } from '@mui/material';

import { useGetItemQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';
import { useGetProjectQuery } from '@/features/projects/projectApi';
import { useGetRoomQuery } from '@/features/rooms/roomApi';

import { ROUTES } from '@/utils/constants';

const AppBreadcrumbs: React.FC = () => {
    const params = useExtractParams();

    const projectId = params["projectId"]
    const roomId = params["roomId"]
    const orgUnitId = params["orgUnitId"]
    const itemId = params["itemId"]

    const { data: projectData, isLoading: projectLoading } = useGetProjectQuery(projectId ?? '', { skip: !projectId });
    const { data: roomData, isLoading: roomLoading } = useGetRoomQuery(roomId ?? '', { skip: !roomId });
    const { data: orgUnitData, isLoading: orgUnitLoading } = useGetOrgUnitQuery(orgUnitId ?? '', { skip: !orgUnitId });
    const { data: itemData, isLoading: itemLoading } = useGetItemQuery(itemId ?? '', { skip: !itemId });

    const { pathname } = useLocation();
    const breadcrumbs = [
        { label: "Home", to: ROUTES.home },
        pathname.includes(ROUTES.about) && { label: "About", to: ROUTES.about },
        pathname.includes(ROUTES.projects) && { label: "My Projects", to: ROUTES.projects },
        projectId && {
            label: projectLoading ? "Loading..." : projectData?.name || `Project ${projectId}`,
            to: ROUTES.projectDetails(projectId),
        },
        roomId && {
            label: roomLoading ? "Loading..." : roomData?.name || `Room ${roomId}`,
            to: ROUTES.roomDetails(projectId!, roomId),
        },
        orgUnitId && {
            label: orgUnitLoading ? "Loading..." : orgUnitData?.name || `Organizer ${orgUnitId}`,
            to: ROUTES.orgUnitDetails(projectId!, roomId!, orgUnitId),
        },
        itemId && {
            label: itemLoading ? "Loading..." : itemData?.name || `Item ${itemId}`,
            to: ROUTES.itemDetails(projectId!, roomId!, orgUnitId!, itemId),
        },
    ].filter(Boolean);

    return (
        <Breadcrumbs aria-label="breadcrumb" separator="›" sx={{ padding: "8px 16px" }}>
            {breadcrumbs.map((crumb, index) => {
                if (!crumb) {
                    return <></>
                }
                const isLast = index === breadcrumbs.length - 1;
                return isLast ? (
                    <Typography key={crumb.to}>{crumb.label}</Typography>
                ) : (
                    <Link
                        key={crumb.to}
                        component={RouterLink}
                        to={crumb.to!}
                        color="inherit"
                        underline="hover"
                    >
                        {crumb.label}
                    </Link>
                );
            })}
        </Breadcrumbs>
    );
}

export const useExtractParams = () => {
    const { pathname } = useLocation();

    // Generate route patterns dynamically from ROUTES
    const routePatterns = Object.entries(ROUTES)
        .filter(([_, value]) => typeof value === "function")
        .map(([_, generateRoute]) =>
            (generateRoute as Function)(
                ":projectId",
                ":roomId",
                ":orgUnitId",
                ":itemId"
            )
        );

    // Attempt to match the current pathname against the generated route patterns
    for (const route of routePatterns) {
        const result = matchPath(route, pathname);
        if (result?.params) {
            return result.params as Record<string, string | undefined>;
        }
    }

    return {}; // Return an empty object if no match is found
};

export default AppBreadcrumbs;