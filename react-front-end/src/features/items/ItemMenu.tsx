import React from 'react';


import { useParams } from 'react-router-dom';
import { Item } from './itemTypes';
import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';

type ItemMenuProps = {
    item: Item
}

const ItemMenu: React.FC<ItemMenuProps> = ({ item }) => {
    const { projectId, roomId, orgUnitId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Item",
            url: `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/${item.id}/edit`
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default ItemMenu;