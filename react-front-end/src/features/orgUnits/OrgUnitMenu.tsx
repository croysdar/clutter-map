import React from 'react';

import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { OrgUnit } from '@/features/orgUnits/orgUnitsTypes';
import { useParams } from 'react-router-dom';

type OrgUnitMenuProps = {
    orgUnit: OrgUnit
}

const OrgUnitMenu: React.FC<OrgUnitMenuProps> = ({ orgUnit }) => {
    const { roomId, projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Organizer",
            url: `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnit.id}/edit`
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default OrgUnitMenu;