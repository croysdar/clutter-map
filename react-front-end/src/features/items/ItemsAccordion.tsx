import React from 'react';

import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Card,
    CardActions,
    CardContent,
    CardHeader,
    CircularProgress,
    Container,
    Typography
} from '@mui/material';

import CreateNewObjectButton from '@/components/common/CreateNewObjectButton';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import { useParams } from 'react-router-dom';
import { useGetOrgUnitQuery } from '../orgUnits/orgUnitApi';
import { useGetItemsByOrgUnitQuery } from './itemApi';

import { RenderTags } from '@/components/TagField';
import ItemMenu from './ItemMenu';

interface ItemsAccordionProps {
    orgUnitId: string
}

const ItemsAccordion: React.FC<ItemsAccordionProps> = ({ orgUnitId }) => {
    const { projectId, roomId } = useParams();

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
            <CircularProgress />
        );
    }

    if (isError) {
        return <div>{error.toString()}</div>
    }

    if (!orgUnit) {
        return <div>Organizational Unit not found.</div>
    }

    return (
        <Container sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
        }}>
            {
                items.length > 0 &&
                <Accordion sx={{ boxShadow: 'none' }}>
                    <AccordionSummary
                        expandIcon={<ArrowDropDownIcon />}
                        id={`org-unit-${orgUnitId}-items-accordion-header`}
                    >
                        <Typography variant='body1'> Contents </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {items.map((item) => (
                            <Card key={`item-card-${item.id}`} sx={{mt: 1}}>
                                <CardHeader
                                    title={<Typography variant='h6'> {item.name}</Typography>}
                                    action={<ItemMenu item={item} />}
                                    sx={{py: 1}}
                                />
                                <CardContent sx={{py: 1}}>
                                    <Typography variant="body2" gutterBottom align="left">{item.description}</Typography>
                                    <Typography variant="body2" gutterBottom align="left">Quantity: {item.quantity}</Typography>
                                </CardContent>
                                <CardActions>
                                    <RenderTags tags={item.tags} />
                                </CardActions>
                            </Card>
                        ))}
                    </AccordionDetails>
                </Accordion>
            }
            <CreateNewObjectButton
                objectLabel='item'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`}
            />
        </Container>
    );
};

export default ItemsAccordion;