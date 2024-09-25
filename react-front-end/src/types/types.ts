import { Room } from "@/features/rooms/roomsSlice";

export interface Item {
    id: number;
    name: string;
    description: string;
    location: string;
}

export interface Location {
    id: number;
    name: string;
    description: string;
    items: Item[];
    subLocations: Location[];
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

