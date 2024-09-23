// src/pages/ListView.tsx
import React from 'react';

import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Card,
    CardContent,
    CardHeader,
    Divider,
    Typography
} from '@mui/material';

import { Location } from '../types/types';
import RoomMenu from '@/components/RoomMenu';
import { useGetRoomsQuery } from '@/api/apiSlice';
import { API_BASE_URL } from '@/utils/constants';

const ListView: React.FC = () => {
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
        <div>
            <h1>Clutter Map - Room List</h1>
            {rooms.map((room) => (
                <>
                    <Card key={`room-card-${room.id}`}>
                        <div key={room.id} >
                            <CardHeader
                                title={room.name}
                                action={<RoomMenu room={room} />}
                            />
                            <CardContent>
                                {/* <p>{room.description}</p> */}
                                {room.locations?.map((location) => (
                                    renderLocation(location)
                                ))}
                            </CardContent>
                        </div>
                    </Card>
                    <Divider style={{ "marginTop": "10px" }} />
                </>
            ))}
        </div>
    );
};

export default ListView;