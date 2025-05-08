export const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

export const AUTH_CLIENT_ID = "890412284296-t0hakf6vsvgdubtoulcnp8cmk4i6hon1.apps.googleusercontent.com"

export const PROJECT_LIMIT = 3;

export const ROUTES = {
    home: "/",
    about: "/about",
    projects: "/projects",

    projectAdd:
        "/projects/add",
    projectDetails: (projectId: string | number) =>
        `/projects/${projectId}`,
    projectEdit: (projectId: string | number) =>
        `/projects/${projectId}/edit`,
    projectItems: (projectId: string | number) =>
        `/projects/${projectId}/items`,
    projectOrgUnits: (projectId: string | number) =>
        `/projects/${projectId}/org-units`,


    roomAdd: (projectId: string | number) =>
        `/projects/${projectId}/rooms/add`,
    roomDetails: (projectId: string | number, roomId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}`,
    roomEdit: (projectId: string | number, roomId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/edit`,
    roomRemoveOrgUnits: (projectId: string | number, roomId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/remove`,
    roomAssignOrgUnits: (projectId: string | number, roomId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/assign`,

    orgUnitAdd: (projectId: string | number) =>
        `/projects/${projectId}/org-units/add`,
    orgUnitDetails: (projectId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/org-units/${orgUnitId}`,
    orgUnitEdit: (projectId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/org-units/${orgUnitId}/edit`,
    orgUnitRemoveItems: (projectId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/org-units/${orgUnitId}/items/remove`,
    orgUnitAssignItems: (projectId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/org-units/${orgUnitId}/items/assign`,

    itemAdd: (projectId: string | number) =>
        `/projects/${projectId}/items/add`,
    itemDetails: (projectId: string | number, itemId: string | number) =>
        `/projects/${projectId}/items/${itemId}`,
    itemEdit: (projectId: string | number, itemId: string | number) =>
        `/projects/${projectId}/items/${itemId}/edit`,
}

export const ROUTE_PATTERNS = [
    { pattern: "/projects/:projectId/items/:itemId/edit", keys: ["projectId", "itemId"] },
    { pattern: "/projects/:projectId/items/:itemId", keys: ["projectId", "itemId"] },
    { pattern: "/projects/:projectId/items/add", keys: ["projectId"] },
    { pattern: "/projects/:projectId/items", keys: ["projectId"] },

    { pattern: "/projects/:projectId/org-units/:orgUnitId/edit", keys: ["projectId", "orgUnitId"] },
    { pattern: "/projects/:projectId/org-units/:orgUnitId/items/remove", keys: ["projectId", "orgUnitId"] },
    { pattern: "/projects/:projectId/org-units/:orgUnitId/items/assign", keys: ["projectId", "orgUnitId"] },
    { pattern: "/projects/:projectId/org-units/:orgUnitId", keys: ["projectId", "orgUnitId"] },
    { pattern: "/projects/:projectId/org-units/add", keys: ["projectId"] },
    { pattern: "/projects/:projectId/org-units", keys: ["projectId"] },

    { pattern: "/projects/:projectId/rooms/:roomId/edit", keys: ["projectId", "roomId"] },
    { pattern: "/projects/:projectId/rooms/:roomId/org-units/remove", keys: ["projectId", "roomId"] },
    { pattern: "/projects/:projectId/rooms/:roomId/org-units/assign", keys: ["projectId", "roomId"] },
    { pattern: "/projects/:projectId/rooms/:roomId", keys: ["projectId", "roomId"] },
    { pattern: "/projects/:projectId/rooms/add", keys: ["projectId"] },

    { pattern: "/projects/:projectId/edit", keys: ["projectId"] },
    { pattern: "/projects/:projectId", keys: ["projectId"] },
    { pattern: "/projects/add", keys: [] },
    { pattern: "/projects", keys: [] },

    { pattern: "/about", keys: [] },
    { pattern: "/", keys: [] },
];

export const IDB_VERSION = 3;

export const IDB_NAME = 'ClutterMapDB';

export const TEST_IDB_NAME = 'ClutterMapDB_Test';