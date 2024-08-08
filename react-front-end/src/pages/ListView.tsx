// src/pages/ListView.tsx
import React, { useEffect, useState } from 'react';

import sampleData from './data.json'
import { HomeData, Location } from '../types/types';
import { Accordion, AccordionDetails, AccordionSummary, Card, CardContent, CardHeader, Divider, Typography } from '@mui/material';

const ListView: React.FC = () => {
    const [data, setData] = useState<HomeData | null>(null);

    useEffect(() => {
        // Simulate fetching data from an API
        const fetchData = async () => {
            // TODO Replace this with an actual API call
            setData(sampleData);
        };

        fetchData();
    }, []);

    if (!data) {
        return <div>Loading...</div>;
    }

    const displayLocation = (location: Location) => {
        return (
            <Accordion>
                <AccordionSummary >
                    <Typography > {location.name} </Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <div key={location.id} className="location">
                        {/* <p>{location.description}</p> */}
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
                            {location.subLocations.map((location) => displayLocation(location))}
                        </ul>
                    </div>
                </AccordionDetails>
            </Accordion>
        )
    }

    return (
        <div>
            <h1>Clutter Map - Room List</h1>
            {data.home.rooms.map((room) => (
                <>
                <Card>

                    <div key={room.id} >
                        <CardHeader title={room.name} />
                        <CardContent>
                            {/* <p>{room.description}</p> */}
                            {room.locations.map((location) => (
                                displayLocation(location)
                            ))}
                        </CardContent>
                    </div>
                </Card>
                <Divider style={{ "marginTop": "10px"}}/>
                </>
            ))}
        </div>
    );
};

export default ListView;