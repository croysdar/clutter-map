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

export interface Room {
    id: number;
    name: string;
    description: string;
    locations: Location[];
}

export interface HomeData {
    home: {
        rooms: Room[];
    };
}