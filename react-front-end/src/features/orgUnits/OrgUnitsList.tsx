import React from 'react';

import {
    Card,
    CardActions,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { useGetRoomQuery } from '@/features/rooms/roomApi';
import { ListViewTileWrap } from '@/pages/ListViewPage';
import { useParams } from 'react-router-dom';
import ItemsAccordion from '../items/ItemsAccordion';
import RoomMenu from '../rooms/RoomMenu';
import { useGetOrgUnitsByRoomQuery } from './orgUnitApi';

const OrgUnitsList: React.FC = () => {
    const { projectId, roomId } = useParams();

    const { data: room } = useGetRoomQuery(roomId!);

    const {
        data: orgUnits = [],
        isLoading,
        isError,
        error
    } = useGetOrgUnitsByRoomQuery(roomId!);

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

    if (!room) {
        return <div>Room not found.</div>
    }

    return (
        <>
            <ListViewTileWrap title={room.name} subtitle={room.description} menu={<RoomMenu room={room} />}>
                {orgUnits.map((orgUnit) => (
                    <Card key={`orgUnit-card-${orgUnit.id}`} sx={{ width: '100%' }}>
                        <div key={orgUnit.id} >
                            <CardHeader
                                title={<Typography variant='h6'> {orgUnit.name}</Typography>}
                                action={<OrgUnitMenu orgUnit={orgUnit} />}
                            />
                            {/* <CardContent>
                                <Typography variant="body2" sx={{ mb: 1 }}>{orgUnit.description}</Typography>
                            </CardContent> */}
                            <CardActions>
                                <ItemsAccordion orgUnitId={orgUnit.id.toString()} />
                            </CardActions>
                        </div>
                    </Card>
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='Organizational Unit'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/add`}
            />
        </>
    );
};

export default OrgUnitsList;