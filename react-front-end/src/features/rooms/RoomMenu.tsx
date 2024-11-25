import React from 'react';
import { useParams } from 'react-router-dom';

/* ------------- Components ------------- */
import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';

/* ------------- Constants ------------- */
import { ROUTES } from '@/utils/constants';

/* ------------- Types ------------- */
import { Room } from '@/features/rooms/roomsTypes';

type RoomMenuProps = {
    room: Room
}

const RoomMenu: React.FC<RoomMenuProps> = ({ room }) => {

    const { projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Room",
            url: ROUTES.roomEdit(projectId!, room.id)
        },
        {
            label: "Remove Organizers From Room",
            url: ROUTES.roomRemoveOrgUnits(projectId!, room.id)
        },
        {
            label: "Move Organizers to Room",
            url: ROUTES.roomAssignOrgUnits(projectId!, room.id)
        },
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default RoomMenu;