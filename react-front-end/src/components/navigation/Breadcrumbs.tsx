import React from 'react';

import { Link as RouterLink, matchPath, useLocation } from 'react-router-dom';

import { Breadcrumbs, Link, Typography } from '@mui/material';

import { useGetItemQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';
import { useGetProjectQuery } from '@/features/projects/projectApi';
import { useGetRoomQuery } from '@/features/rooms/roomApi';

import { useEntityHierarchy } from '@/hooks/useEntityHierarchy';

import { ROUTE_PATTERNS, ROUTES } from '@/utils/constants';

const AppBreadcrumbs: React.FC = () => {
    const params = useExtractParams();

    const projectId = params["projectId"]
    const roomParamId = params["roomId"]
    const orgUnitParamId = params["orgUnitId"]
    const itemId = params["itemId"]

    // Get hierarchy for items or org units
    const { hierarchy, loading: hierarchyLoading } = useEntityHierarchy(
        itemId ? 'item' : orgUnitParamId ? 'orgUnit' : 'item',
        Number(itemId || orgUnitParamId || 0)
    );

    // Only enrich if current page is item or orgUnit
    const orgUnitId = orgUnitParamId || (itemId ? hierarchy.orgUnit?.id.toString() : undefined);
    const roomId = roomParamId || ((itemId || orgUnitId) ? hierarchy.room?.id.toString() : undefined);

    const { data: projectData, isLoading: projectLoading } = useGetProjectQuery(Number(projectId) ?? '', { skip: !projectId });
    const { data: roomData, isLoading: roomLoading } = useGetRoomQuery(Number(roomId) ?? '', { skip: !roomId });
    const { data: orgUnitData, isLoading: orgUnitLoading } = useGetOrgUnitQuery(Number(orgUnitId) ?? '', { skip: !orgUnitId });
    const { data: itemData, isLoading: itemLoading } = useGetItemQuery(Number(itemId) ?? '', { skip: !itemId });

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
            label: (roomLoading || hierarchyLoading) ? "Loading..." : roomData?.name || `Room ${roomId}`,
            to: ROUTES.roomDetails(projectId!, roomId),
        },
        orgUnitId && {
            label: (orgUnitLoading || hierarchyLoading) ? "Loading..." : orgUnitData?.name || `Organizer ${orgUnitId}`,
            to: ROUTES.orgUnitDetails(projectId!, orgUnitId),
        },
        itemId && {
            label: (itemLoading || hierarchyLoading) ? "Loading..." : itemData?.name || `Item ${itemId}`,
            to: ROUTES.itemDetails(projectId!, itemId),
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


    // Attempt to match the current pathname against the route patterns
    for (const { pattern } of ROUTE_PATTERNS) {
        const match = matchPath(pattern, pathname);
        if (match?.params) {
            return match.params as Record<string, string | undefined>;
        }
    }

    return {}; // Return an empty object if no match is found
};

export default AppBreadcrumbs;