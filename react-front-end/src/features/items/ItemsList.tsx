import React from 'react';

import {
    Card,
    CardContent,
    CardHeader,
    CircularProgress,
    Paper,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import { useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useGetItemsByOrgUnitQuery } from './itemApi';

import ItemMenu from './ItemMenu';

const ItemsList: React.FC = () => {
    const {projectId, roomId, orgUnitId } = useParams();

    // TODO render as an accordion inside Org Unit instead

    // TODO create 'unassigned item pool'

    const { data: orgUnit } = useGetOrgUnitQuery(orgUnitId!);

    const {
        data: items = [],
        isLoading,
        isError,
        error
    } = useGetItemsByOrgUnitQuery(orgUnitId!);

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

    if (!orgUnit) {
        return <div>Organizational Unit not found.</div>
    }

    return (
        <Paper sx={{ width: '100%', padding: 4, boxShadow: 3 }}>
            <Typography variant="h2" key="org-unit-name">
                {orgUnit.name}
            </Typography>
            {items.map((item) => (
                <Card key={`item-card-${item.id}`} sx={{ marginTop: 3 }}>
                    <div key={item.id} >
                        <CardHeader
                            title={<Typography variant='h4'> {item.name}</Typography>}
                            action={<ItemMenu item={item} />}
                        />
                        <CardContent>
                            <Typography variant="body2">{item.description}</Typography>
                        </CardContent>
                    </div>
                </Card>
            ))}
            <CreateNewObjectButton
                objectLabel='item'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`}
            />
        </Paper>
    );
};

export default ItemsList;