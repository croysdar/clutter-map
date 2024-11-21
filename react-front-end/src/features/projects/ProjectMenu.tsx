import React from 'react';


import LinksMenu, { LinkMenuItem } from '@/components/common/LinksMenu';
import { Project } from '@/features/projects/projectsTypes';

type ProjectMenuProps = {
    project: Project
}

const ProjectMenu: React.FC<ProjectMenuProps> = ({ project }) => {

    const menuItems: LinkMenuItem[] = [
        {
            label: "Edit Project",
            url: `/projects/${project.id}/edit`
        }
    ]

    return <LinksMenu menuItems={menuItems} />
}

export default ProjectMenu;