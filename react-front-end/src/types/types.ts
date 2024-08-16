export interface Item {
    id: number;
    name: string;
    description: string;
    location: string;
    comments: Comment[];
}

export interface Location {
    id: number;
    name: string;
    description: string;
    items: Item[];
    subLocations: Location[];
    // coordinate in room
    // shape
    // type? shelf, desk, table, bin, etc
        /*
            - chair
            - sofa
            - dining table
            - end table
            - bed
            - dresser
            - armoire
            - china cabinet
            - desk
            - bookcase
            - shelving unit
            - bin
            - closet rod
            - cabinet
        */
    comments: Comment[];
}

// comment TODO
export interface Comment {
    // How to handle reply structure
    // How to handle picture commments?

}

// coordinate : x, y
export interface Coordinate {
    x: number;
    y: number;
}

export interface Room {
    id: number;
    name: string;
    description: string;
    locations: Location[];
    // blueprint -> list of coordinates of the corners of the room?
    // OR rooms could be from a set number of adjustable shapes
        // https://roomstyler.com/3dplanner
}


export interface HomeData {
    home: {
        rooms: Room[];
    };
}



// How to handle items on countertops and in cabinets? 3D?
// allow overlap? (should this be in all cases or only for some types?)

// what floor is the room on?
