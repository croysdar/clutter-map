import React from 'react';

import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Card,
    CardContent,
    CardHeader,
    CircularProgress,
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
        <>
            {
                items.length > 0 &&
                <Accordion>
                    <AccordionSummary
                        expandIcon={<ArrowDropDownIcon />}
                        id={`org-unit-${orgUnitId}-items-accordion-header`}
                    >
                        <Typography variant='body1'> Items </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {items.map((item) => (
                            <Card key={`item-card-${item.id}`} sx={{ marginTop: 1 }}>
                                <div key={item.id} >
                                    <CardHeader
                                        title={<Typography variant='h6'> {item.name}</Typography>}
                                        action={<ItemMenu item={item} />}
                                    />
                                    <CardContent>
                                        <Typography variant="body2">{item.description}</Typography>
                                        <RenderTags tags={item.tags} />
                                    </CardContent>
                                </div>
                            </Card>
                        ))}

                    </AccordionDetails>
                </Accordion>
            }
            <CreateNewObjectButton
                objectLabel='item'
                to={`/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`}
            />
        </>
    );
};

export default ItemsAccordion;