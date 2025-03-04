import React from 'react';

import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes';
import { useParams } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';

type OrgUnitMenuProps = {
    orgUnit: OrgUnit
}

const OrgUnitMenu: React.FC<OrgUnitMenuProps> = ({ orgUnit }) => {
    const { roomId, projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Organizer",
            url: ROUTES.orgUnitEdit(projectId!, roomId!, orgUnit.id),
            requiresOnline: true
        },
        {
            label: "Remove Items From Organizer",
            url: ROUTES.orgUnitRemoveItems(projectId!, roomId!, orgUnit.id),
            requiresOnline: true
        },
        {
            label: "Move Items to Organizer",
            url: ROUTES.orgUnitAssignItems(projectId!, roomId!, orgUnit.id),
            requiresOnline: true
        },
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default OrgUnitMenu;