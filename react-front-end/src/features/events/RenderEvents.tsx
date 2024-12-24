import React, { useState } from 'react';

/* ------------- Material UI ------------- */
import {
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Button,
    CircularProgress,
    Typography
} from '@mui/material';

/* ------------- Components ------------- */
import { ResourceType } from '@/types/types';

/* ------------- Redux ------------- */
import { useGetEntityEventsQuery } from './eventApi';

/* ------------- Constants ------------- */
import { TimelineEvent } from "./eventTypes";

type RenderEventsProps = {
    events: TimelineEvent[];
};

type EntityEventsContainerProps = {
    entityId: number;
    entityType: ResourceType;
};

export const EntityEventsContainer: React.FC<EntityEventsContainerProps> = ({ entityId, entityType }) => {
    const [showHistory, setShowHistory] = useState(false);
    const { data: events = [], isLoading, isError } = useGetEntityEventsQuery(
        { entityType, entityId },
        { skip: !showHistory } // Only fetch when `showHistory` is true
    );

    const Content = () => {
        if (isLoading) {
            return <CircularProgress />
        }
        if (isError) {
            return <Typography>Error fetching events.</Typography>
        }
        return (
            events.length > 0 ? (
                <RenderEvents events={events} />
            ) : (
                <Typography>No events available.</Typography>
            )
        )
    }

    return (
        <div>
            <Button variant="outlined" onClick={() => setShowHistory((prev) => !prev)}>
                {showHistory ? 'Hide History' : 'Show History'}
            </Button>
            {showHistory && (
                <div>
                    <Content />
                </div>
            )}
        </div>
    );
};

const RenderEvents: React.FC<RenderEventsProps> = ({ events }) => {
    return (
        <TableContainer component={Paper}>
            <Table>
                <TableHead>
                    <TableRow>
                        {/* <TableCell>Entity Type</TableCell>
                        <TableCell>Entity ID</TableCell> */}
                        {/* <TableCell>Project</TableCell> */}
                        <TableCell>User</TableCell>
                        <TableCell>Action</TableCell>
                        <TableCell>Payload</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {events.map(event => (
                        <TableRow key={event.id}>
                            {/* <TableCell>{event.entityType}</TableCell>
                            <TableCell>{event.entityId}</TableCell> */}
                            {/* <TableCell>{`${event.projectName} (${event.projectId})`}</TableCell> */}
                            <TableCell>{event.userName}</TableCell>
                            <TableCell>{event.action}</TableCell>
                            <TableCell><RenderPayload payload={event.payload} /></TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

type RenderPayloadProps = {
    payload: string
};


const RenderPayload: React.FC<RenderPayloadProps> = ({ payload }) => {
    const parsedPayload = JSON.parse(payload);
    return (
        <>
            {Object.keys(parsedPayload).length > 0 ? (
                <ul>
                    {Object.entries(parsedPayload).map(([field, value]) => (
                        <li key={field}>
                            <strong>{field}:</strong> {String(value)}
                        </li>
                    ))}
                </ul>
            ) : (
                <p>No details available.</p>
            )}
        </>
    );
}