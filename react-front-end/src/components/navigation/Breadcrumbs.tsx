import React from 'react';

import { Link as RouterLink, useLocation } from 'react-router-dom';

import { Breadcrumbs, Link, Typography } from '@mui/material';

import { useGetItemQuery } from '@/features/items/itemApi';
import { useGetOrgUnitQuery } from '@/features/orgUnits/orgUnitApi';
import { useGetProjectQuery } from '@/features/projects/projectApi';
import { useGetRoomQuery } from '@/features/rooms/roomApi';

import { useEntityHierarchy } from '@/hooks/useEntityHierarchy';

import { ROUTES } from '@/utils/constants';
import { useResolvedParams } from '@/hooks/useResolvedParams';

const AppBreadcrumbs: React.FC = () => {
    const { projectId, roomId, orgUnitId, itemId, projectIdNum, orgUnitIdNum, itemIdNum } = useResolvedParams();
    const { pathname } = useLocation();

    const isAddItemPage = pathname.includes("/items/add");
    const isAddOrgUnitPage = pathname.includes("/org-units/add");
    const isAddRoomPage = pathname.includes("/rooms/add");
    const isAddProjectPage = pathname.includes("/projects/add");
    const isAddPage = isAddItemPage || isAddOrgUnitPage || isAddRoomPage || isAddProjectPage;

    const isEditItemPage = /\/items\/[^/]+\/edit/.test(pathname);
    const isEditOrgUnitPage = /\/org-units\/[^/]+\/edit/.test(pathname);
    const isEditRoomPage = /\/rooms\/[^/]+\/edit/.test(pathname);
    const isEditPage = isEditItemPage || isEditOrgUnitPage || isEditRoomPage;

    // Get hierarchy for items or org units
    const shouldFetchHierarchy = !isAddPage && (itemId || orgUnitId);
    const { hierarchy, loading: hierarchyLoading } = useEntityHierarchy(
        shouldFetchHierarchy
            ? itemId
                ? 'item'
                : 'orgUnit'
            : 'item',
        shouldFetchHierarchy
            ? itemIdNum ?? orgUnitIdNum ?? -1
            : -1
    );

    // Use the orgUnitId from the URL if present, otherwise get it from the hierarchy (if viewing an item)
    const resolvedOrgUnitId = orgUnitId ?? (itemIdNum ? hierarchy.orgUnit?.id.toString() : undefined);

    // Use the roomId from the URL if present,
    // otherwise get it from the hierarchy (if viewing an item or org unit)
    const resolvedRoomId = roomId ?? ((itemIdNum || resolvedOrgUnitId) ? hierarchy.room?.id.toString() : undefined);

    // Convert the resolvedRoomId to a number if it exists, otherwise leave it undefined
    const resolvedRoomIdNum = resolvedRoomId ? Number(resolvedRoomId) : undefined;

    // Convert the resolvedOrgUnitId to a number if it exists, otherwise leave it undefined
    const resolvedOrgUnitIdNum = resolvedOrgUnitId ? Number(resolvedOrgUnitId) : undefined;

    const { data: projectData, isLoading: projectLoading } =
        useGetProjectQuery(projectIdNum ?? -1, { skip: !projectIdNum || isNaN(projectIdNum) });

    const { data: roomData, isLoading: roomLoading } =
        useGetRoomQuery(resolvedRoomIdNum ?? -1, { skip: !resolvedRoomIdNum || isNaN(resolvedRoomIdNum) });

    const { data: orgUnitData, isLoading: orgUnitLoading } =
        useGetOrgUnitQuery(resolvedOrgUnitIdNum ?? -1, { skip: !resolvedOrgUnitIdNum || isNaN(resolvedOrgUnitIdNum) });

    const { data: itemData, isLoading: itemLoading } =
        useGetItemQuery(itemIdNum ?? -1, { skip: !itemIdNum || isNaN(itemIdNum) });

    const breadcrumbs = [
        { label: "Home", to: ROUTES.home },
        pathname.includes(ROUTES.about) && { label: "About", to: ROUTES.about },
        pathname.includes(ROUTES.projects) && { label: "My Projects", to: ROUTES.projects },
        projectId && {
            label: projectLoading ? "Loading..." : projectData?.name || `Project ${projectId}`,
            to: ROUTES.projectDetails(projectId),
        },
        resolvedRoomIdNum && !isNaN(resolvedRoomIdNum) && {
            label: (roomLoading || hierarchyLoading) ? "Loading..." : roomData?.name || `Room ${roomId}`,
            to: ROUTES.roomDetails(projectId!, resolvedRoomIdNum),
        },
        resolvedOrgUnitIdNum && !isNaN(resolvedOrgUnitIdNum) && {
            label: (orgUnitLoading || hierarchyLoading) ? "Loading..." : orgUnitData?.name || `Organizer ${orgUnitId}`,
            to: ROUTES.orgUnitDetails(projectId!, resolvedOrgUnitIdNum),
        },
        itemIdNum && !isNaN(itemIdNum) && {
            label: (itemLoading || hierarchyLoading) ? "Loading..." : itemData?.name || `Item ${itemId}`,
            to: ROUTES.itemDetails(projectId!, itemIdNum),
        },
        isAddPage && {
            label: "Add",
            to: pathname,
        },
        isEditPage && {
            label: "Edit",
            to: pathname,
        }
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


export default AppBreadcrumbs;