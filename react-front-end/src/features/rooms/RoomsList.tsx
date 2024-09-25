import React from 'react';

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
import RoomMenu from '@/features/rooms/RoomMenu';
import { useGetRoomsQuery } from '@/features/api/apiSlice';
import ButtonLink from '@/components/common/ButtonLink';

const RoomsList: React.FC = () => {
    const {
        data: rooms = [],
        isLoading,
        isError,
        error
    } = useGetRoomsQuery();

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
                    Room List
                </Typography>
                {rooms.map((room) => (
                    <>
                        <Card key={`room-card-${room.id}`} sx={{marginTop: 3}}>
                            <div key={room.id} >
                                <CardHeader
                                    title={<Typography variant='h4'> {room.name}</Typography>}
                                    action={<RoomMenu room={room} />}
                                />
                                <CardContent>
                                    <Typography variant="body2">{room.description}</Typography>
                                    {/* {room.locations?.map((location) => (
                                    renderLocation(location)
                                ))} */}
                                </CardContent>
                            </div>
                        </Card>
                    </>
                ))}
                <ButtonLink href="/rooms/add" label="Create a new Room"/>
            </Paper>
        </Container>
    );
};

export default RoomsList;