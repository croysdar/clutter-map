import React from 'react';
import { useParams } from 'react-router-dom';
import { type Item } from './itemTypes';
import LinksMenu, { type LinkMenuItem } from '@/components/common/LinksMenu';
import { ROUTES } from '@/utils/constants';

type ItemMenuProps = {
    item: Item
}

const ItemMenu: React.FC<ItemMenuProps> = ({ item }) => {
    const { projectId } = useParams();

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Item",
            url: ROUTES.itemEdit(projectId!, item.id),
            requiresOnline: true
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default ItemMenu;