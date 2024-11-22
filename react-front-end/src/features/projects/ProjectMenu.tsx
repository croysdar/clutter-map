import React from 'react';


import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { Project } from '@/features/projects/projectsTypes';
import { ROUTES } from '@/utils/constants';

type ProjectMenuProps = {
    project: Project
}

const ProjectMenu: React.FC<ProjectMenuProps> = ({ project }) => {

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Project",
            url: ROUTES.projectEdit(project.id)
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default ProjectMenu;