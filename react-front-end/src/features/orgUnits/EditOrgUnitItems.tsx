import React, { useState } from 'react';
import { useNavigate, useParams } from "react-router-dom";

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from "@mui/material";

/* ------------- Components ------------- */
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { EditCardWrapper } from "@/components/pageWrappers/EditPageWrapper";
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import { ItemListWithCheckBoxes } from '@/features/items/RenderItems';

/* ------------- Redux ------------- */
import { useGetItemsByOrgUnitQuery, useGetItemsByProjectQuery, useUnassignItemsFromOrgUnitMutation } from "@/features/items/itemApi";
import { useAssignItemsToOrgUnitMutation, useGetOrgUnitQuery } from "@/features/orgUnits/orgUnitApi";

/* ------------- Constants ------------- */
import { ROUTES } from "@/utils/constants";

export const RemoveOrgUnitItems = () => {
    const { orgUnitId, projectId } = useParams();
    const navigate = useNavigate();
    const redirectUrl = ROUTES.orgUnitDetails(projectId!, orgUnitId!);

    const { data: items, isLoading: itemsLoading, isError, error } = useGetItemsByOrgUnitQuery(Number(orgUnitId)!);
    const { data: orgUnit, isLoading: orgUnitLoading } = useGetOrgUnitQuery(Number(orgUnitId!));

    const [itemsToRemove, setItemsToRemove] = useState<number[]>([]);

    const [
        unassignItems,
        { isLoading: unassignLoading }
    ] = useUnassignItemsFromOrgUnitMutation();

    if (itemsLoading || orgUnitLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!items || !orgUnit) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Organizer not found</Typography>
            </EditCardWrapper>
        )
    }

    if (isError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </EditCardWrapper>
        )
    }

    const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        if (itemsToRemove.length) {
            await unassignItems(itemsToRemove);
        }

        navigate(redirectUrl);
    }

    return (
        <DetailsPagePaper title="Remove items" subtitle={orgUnit.name}>
            <ItemListWithCheckBoxes items={items} checkedItems={itemsToRemove} setCheckedItems={setItemsToRemove} />
            <SubmitButton
                onClick={(e) => handleSubmit(e)}
                disabled={unassignLoading}
                label="Remove Items"
            />
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </DetailsPagePaper>
    )
}

export const AssignItemsToOrgUnit = () => {
    const { orgUnitId, projectId } = useParams();
    const navigate = useNavigate();
    const redirectUrl = ROUTES.orgUnitDetails(projectId!, orgUnitId!);

    const { data: orgUnit, isLoading: orgUnitLoading, isError, error } = useGetOrgUnitQuery(Number(orgUnitId!));
    const { data: projectItems, isLoading: itemsLoading } = useGetItemsByProjectQuery(Number(projectId)!);

    const itemBank = projectItems?.filter((item) => String(item.orgUnitId) !== orgUnitId) || [];

    const [itemsToAdd, setItemsToAdd] = useState<number[]>([]);

    const [
        assignItems,
        { isLoading: assignLoading }
    ] = useAssignItemsToOrgUnitMutation();

    if (itemsLoading || orgUnitLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!projectItems || !orgUnit) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Organizer not found</Typography>
            </EditCardWrapper>
        )
    }

    if (isError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {error.toString()} </Typography>
            </EditCardWrapper>
        )
    }

    const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        if (itemsToAdd.length) {
            await assignItems({ orgUnitId: orgUnit.id, itemIds: itemsToAdd });
        }

        navigate(redirectUrl);
    }

    return (
        <DetailsPagePaper title="Move Items" subtitle={orgUnit.name}>
            <ItemListWithCheckBoxes items={itemBank} checkedItems={itemsToAdd} setCheckedItems={setItemsToAdd} showCurrentOrgUnit />
            <SubmitButton
                onClick={(e) => handleSubmit(e)}
                disabled={assignLoading}
                label="Move Items"
            />
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </DetailsPagePaper>
    )
}