import React from 'react';
import { useParams } from 'react-router-dom';

/* ------------- Components ------------- */
import LinksMenu, { type LinkMenuItem } from '@/components/common/LinksMenu';

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants';

/* ------------- Types ------------- */
import { type Room } from '@/features/rooms/roomsTypes';

type RoomMenuProps = {
    room: Room
}

const RoomMenu: React.FC<RoomMenuProps> = ({ room }) => {

    const { projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Room",
            url: ROUTES.roomEdit(projectId!, room.id),
            requiresOnline: true
        },
        {
            label: "Remove Organizers From Room",
            url: ROUTES.roomRemoveOrgUnits(projectId!, room.id),
            requiresOnline: true
        },
        {
            label: "Move Organizers to Room",
            url: ROUTES.roomAssignOrgUnits(projectId!, room.id),
            requiresOnline: true
        },
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default RoomMenu;