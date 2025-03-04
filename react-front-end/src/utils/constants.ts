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

    orgUnitAdd: (projectId: string | number, roomId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/add`,
    orgUnitDetails: (projectId: string | number, roomId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}`,
    orgUnitEdit: (projectId: string | number, roomId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/edit`,
    orgUnitRemoveItems: (projectId: string | number, roomId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/remove`,
    orgUnitAssignItems: (projectId: string | number, roomId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/assign`,

    itemAdd: (projectId: string | number, roomId: string | number, orgUnitId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/add`,
    itemDetails: (projectId: string | number, roomId: string | number, orgUnitId: string | number, itemId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/${itemId}`,
    itemEdit: (projectId: string | number, roomId: string | number, orgUnitId: string | number, itemId: string | number) =>
        `/projects/${projectId}/rooms/${roomId}/org-units/${orgUnitId}/items/${itemId}/edit`,
}

export const IDB_VERSION = 1;

export const IDB_NAME = 'ClutterMapDB';

export const TEST_IDB_NAME = 'ClutterMapDB_Test';