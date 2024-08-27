// src/pages/CreateRoomBlueprint.tsx
import { KonvaEventObject } from 'konva/lib/Node';
import React, { useState } from 'react';

import { Layer, Text } from 'react-konva';


// svg imports
import LRoomOne from 'assets/images/L_Room_1.svg'
import SquareRoom from 'assets/images/Square_Room.svg'

import { Coordinate } from 'types/types';
import AdjustRoomShape from './AdjustRoomShape';
import KonvaImage from 'components/common/KonvaImage';

// TODO
/*
    Make shapes that are not rectangles (i.e. the room shapes on roomstyler.com)
    Make shapes adjustable (should be able to click and drag an edge)
    Have clear workspace/bank demarcation
    Figure out how to save the room's shape
*/

/*
    1. choose a room style (click on one of the 11 room shapes)
    2. Adjust the edges and corners as desired
    3. Save the new room
    4. Go to fill room screen to add furniture
*/

const coordinates_Square : Coordinate[] = [
    { x: 250, y: 50 },
    { x: 450, y: 50 },
    { x: 450, y: 250 },
    { x: 250, y: 250 },
    { x: 250, y: 50 },
]

const coordinates_L: Coordinate[] = [
    { x: 250, y: 50 },
    { x: 350, y: 50 },
    { x: 350, y: 150 },
    { x: 450, y: 150 },
    { x: 450, y: 250 },
    { x: 250, y: 250 },
    { x: 250, y: 50 },
]

type RoomOption = {
    svg: string,
    coordinates: Coordinate[]
}

const RoomOptions: RoomOption[] = [
    { svg: LRoomOne, coordinates: coordinates_L },
    { svg: SquareRoom, coordinates: coordinates_Square },
]

const CreateRoomBlueprint: React.FC = () => {
    const [chosenRoom, setChosenRoom] = useState<Coordinate[] | null>(null);

    return (
        <Layer>
            <Text text={'Choose Room Shape'} fontSize={20} fill="white" />
            <>
                {RoomOptions.map((option, index) =>
                    <KonvaImage
                        id={`room-bank-${index}`}
                        key={`room-bank-${index}`}
                        svg={option.svg}
                        x={10}
                        y={50 + 100 * index}
                        width={80}
                        height={80}

                        // Set chosen room style on click
                        onMouseDown={(e: KonvaEventObject<MouseEvent>) => {
                            setChosenRoom(RoomOptions[index].coordinates)
                            console.log(RoomOptions[index].coordinates, index)
                        }}

                        // Set cursor style
                        onMouseEnter={(e: KonvaEventObject<MouseEvent>) => {
                            const container = e.target.getStage()?.container();
                            if (container)
                                container.style.cursor = "pointer";
                        }}
                        onMouseLeave={(e: KonvaEventObject<MouseEvent>) => {
                            const container = e.target.getStage()?.container();
                            if (container)
                                container.style.cursor = "default";
                        }}
                    />
                )}
            </>
            {chosenRoom && <AdjustRoomShape x={250} y={150} coordinates={chosenRoom} />}
        </Layer>
    )
}

// https://konvajs.org/docs/sandbox/Image_Resize.html

export default CreateRoomBlueprint;