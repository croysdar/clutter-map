import React from 'react';


import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { Room } from '@/features/rooms/roomsTypes';
import { useParams } from 'react-router-dom';
import { ROUTES } from '@/utils/constants';

type RoomMenuProps = {
    room: Room
}

const RoomMenu: React.FC<RoomMenuProps> = ({ room }) => {

    const { projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Room",
            url: ROUTES.roomEdit(projectId!, room.id)
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default RoomMenu;