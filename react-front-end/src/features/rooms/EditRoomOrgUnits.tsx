import React, { useState } from 'react';
import { useNavigate, useParams } from "react-router-dom";

/* ------------- Material UI ------------- */
import { CircularProgress, Typography } from "@mui/material";

/* ------------- Components ------------- */
import CancelButton from '@/components/forms/CancelButton';
import SubmitButton from '@/components/forms/SubmitButton';
import { EditCardWrapper } from "@/components/pageWrappers/EditPageWrapper";
import { DetailsPagePaper } from '@/components/pageWrappers/ListViewPageWrapper';
import { OrgUnitListWithCheckBoxes } from '../orgUnits/RenderOrgUnit';

/* ------------- Redux ------------- */
import { useGetOrgUnitsByProjectQuery, useGetOrgUnitsByRoomQuery, useUnassignOrgUnitsFromRoomMutation } from "@/features/orgUnits/orgUnitApi";
import { useAssignOrgUnitsToRoomMutation, useGetRoomQuery } from './roomApi';

/* ------------- Constants ------------- */
import { ROUTES } from "@/utils/constants";

export const RemoveRoomOrgUnits = () => {
    const { roomId, projectId } = useParams();
    const navigate = useNavigate();
    const redirectUrl = ROUTES.roomDetails(projectId!, roomId!);

    const { data: orgUnits, isLoading: orgUnitsLoading, isError, error } = useGetOrgUnitsByRoomQuery(roomId!);
    const { data: room, isLoading: roomLoading } = useGetRoomQuery(roomId!);

    const [orgUnitsToRemove, setOrgUnitsToRemove] = useState<number[]>([]);

    const [
        unassignOrgUnits,
        { isLoading: unassignLoading }
    ] = useUnassignOrgUnitsFromRoomMutation();

    if (orgUnitsLoading || roomLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!orgUnits || !room) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Room not found</Typography>
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

        if (orgUnitsToRemove.length) {
            await unassignOrgUnits(orgUnitsToRemove);
        }

        navigate(redirectUrl);
    }

    return (
        <DetailsPagePaper title="Remove Organizers" subtitle={room.name}>
            <OrgUnitListWithCheckBoxes orgUnits={orgUnits} checkedOrgUnits={orgUnitsToRemove} setCheckedOrgUnits={setOrgUnitsToRemove} />
            <SubmitButton
                onClick={(e) => handleSubmit(e)}
                disabled={unassignLoading}
                label="Remove Organizers"
            />
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </DetailsPagePaper>
    )
}

export const AssignOrgUnitsToRoom = () => {
    const { roomId, projectId } = useParams();
    const navigate = useNavigate();
    const redirectUrl = ROUTES.roomDetails(projectId!, roomId!);

    const { data: room, isLoading: roomLoading, isError: isRoomError, error: roomError } = useGetRoomQuery(roomId!);

    const { data: projectOrgUnits, isLoading: orgUnitsLoading, isError: isProjectError, error: projectError } = useGetOrgUnitsByProjectQuery(projectId!);
    const orgUnitBank = projectOrgUnits?.filter((orgUnit) => String(orgUnit.roomId) !== roomId) || [];

    const [orgUnitsToAdd, setOrgUnitsToAdd] = useState<number[]>([]);

    const [
        assignOrgUnits,
        { isLoading: assignLoading }
    ] = useAssignOrgUnitsToRoomMutation();

    if (orgUnitsLoading || roomLoading) {
        return (
            <EditCardWrapper title=''>
                <CircularProgress />
            </EditCardWrapper>
        )
    }

    if (!room) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'>Room not found</Typography>
            </EditCardWrapper>
        )
    }

    if (isRoomError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {roomError.toString()} </Typography>
            </EditCardWrapper>
        )
    }

    if (isProjectError) {
        return (
            <EditCardWrapper title=''>
                <Typography variant='h2'> {projectError.toString()} </Typography>
            </EditCardWrapper>
        )
    }

    const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();

        if (orgUnitsToAdd.length) {
            await assignOrgUnits({ roomId: room.id, orgUnitIds: orgUnitsToAdd });
        }

        navigate(redirectUrl);
    }

    return (
        <DetailsPagePaper title="Move Organizers" subtitle={room.name}>
            <OrgUnitListWithCheckBoxes orgUnits={orgUnitBank} checkedOrgUnits={orgUnitsToAdd} setCheckedOrgUnits={setOrgUnitsToAdd} showCurrentRoom />
            <SubmitButton
                onClick={(e) => handleSubmit(e)}
                disabled={assignLoading}
                label="Move Organizers"
            />
            <CancelButton
                onClick={() => navigate(redirectUrl)}
            />
        </DetailsPagePaper>
    )
}