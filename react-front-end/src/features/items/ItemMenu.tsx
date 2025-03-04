import React from 'react';


import { useParams } from 'react-router-dom';
import { Item } from './itemTypes';
import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { ROUTES } from '@/utils/constants';

type ItemMenuProps = {
    item: Item
}

const ItemMenu: React.FC<ItemMenuProps> = ({ item }) => {
    const { projectId, roomId, orgUnitId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Item",
            url: ROUTES.itemEdit(projectId!, roomId!, orgUnitId!, item.id),
            requiresOnline: true
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default ItemMenu;