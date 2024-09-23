export interface Item {
    id: string;
    name: string;
    description: string;
    location: string;
}

export interface Location {
    id: string;
    name: string;
    description: string;
    items: Item[];
    subLocations: Location[];
}

export interface Room {
    id: string;
    name: string;
    description: string;
    locations: Location[] | null;
}

export interface HomeData {
    home: {
        rooms: Room[];
    };
}

export const IDLE_STATUS = "idle"
export const PENDING_STATUS = "pending"
export const SUCCEEDED_STATUS = "succeeded"
export const FAILED_STATUS = "failed"

export type QueryStatus = "idle" | "pending" | "succeeded" | "failed"

