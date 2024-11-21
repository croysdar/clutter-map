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
import OrgUnitMenu from '@/features/orgUnits/OrgUnitMenu';
import { ListViewTileWrap } from '@/components/pageWrappers/ListViewPage';
import { useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useGetItemsByOrgUnitQuery } from './itemApi';
import ItemMenu from './ItemMenu';
import { RenderTags } from '@/components/TagField';

const ItemsList: React.FC = () => {
    const { projectId, roomId, orgUnitId } = useParams();

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
        return <div>Organizer not found.</div>
    }

    return (
        <>
            <ListViewTileWrap title={orgUnit.name} subtitle={orgUnit.description} menu={<OrgUnitMenu orgUnit={orgUnit} />}>
                {items.map((item) => (
                    <Card key={`item-card-${item.id}`} sx={{ width: '100%' }}>
                        <div key={item.id} >
                            <CardHeader
                                title={<Typography variant='h6'> {item.name}</Typography>}
                                action={<ItemMenu item={item} />}
                            />
                            <CardContent>
                                <Typography variant="body2" sx={{ mb: 1 }}>{item.description}</Typography>
                                <RenderTags tags={item.tags} />
                            </CardContent>
                        </div>
                    </Card>
                ))}
            </ListViewTileWrap>
            <CreateNewObjectButton
                objectLabel='Item'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`}
            />
        </>
    );
};

export default ItemsList;