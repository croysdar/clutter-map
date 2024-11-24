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
import { useGetItemsByOrgUnitQuery, useUnassignItemsFromOrgUnitMutation } from "@/features/items/itemApi";
import { useGetOrgUnitQuery } from "@/features/orgUnits/orgUnitApi";

/* ------------- Constants ------------- */
import { ROUTES } from "@/utils/constants";

const RemoveOrgUnitItems = () => {
    const { orgUnitId, roomId, projectId } = useParams();
    const navigate = useNavigate();
    const redirectUrl = ROUTES.orgUnitDetails(projectId!, roomId!, orgUnitId!);

    const { data: items, isLoading: itemsLoading, isError, error } = useGetItemsByOrgUnitQuery(orgUnitId!);
    const { data: orgUnit, isLoading: orgUnitLoading } = useGetOrgUnitQuery(orgUnitId!);

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

export default RemoveOrgUnitItems;