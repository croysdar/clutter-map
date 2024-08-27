// src/pages/AdjustRoomShape.tsx
import React, { useEffect, useState } from 'react';

// konva imports
import { KonvaEventObject } from 'konva/lib/Node';
import { Circle, Rect } from 'react-konva';

import { Coordinate } from 'types/types';

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

type ComponentProps = {
    coordinates: Coordinate[],
    x: number,
    y: number
}

const translatedCoordinates = (coordinates: Coordinate[], offsetX: number, offsetY: number) => {
    return coordinates.map(coordinate => ({
        x: coordinate.x + offsetX,
        y: coordinate.y + offsetY
    }));
}

const AdjustRoomShape: React.FC<ComponentProps> = ({ coordinates, x, y }) => {
    const [corners, setCorners] = useState<Coordinate[]>(translatedCoordinates(coordinates, x, y));

    useEffect(() => {
        setCorners(translatedCoordinates(coordinates, x, y))
    }, [x, y, coordinates])

    const handleDragMove = (e: KonvaEventObject<MouseEvent>, index: number) => {
        const newCorners = [...corners];

        const nextIndex = (index + 1) % (corners.length - 1);

        const isHorizontal = corners[index].y === corners[nextIndex].y


        if (isHorizontal) {
            e.target.x(corners[index].x);

            newCorners[index].y = e.target.y();
            newCorners[nextIndex].y = e.target.y();
        }
        else {
            e.target.y(corners[index].y);

            newCorners[index].x = e.target.x();
            newCorners[nextIndex].x = e.target.x();
        }

        setCorners(newCorners);
    }

    return (
        <>
            {corners.map((coord, index) => {
                if (index < corners.length - 1) {
                    return (
                        <Circle
                            key={`circle-${index}`}
                            x={coord.x}
                            y={coord.y}
                            radius={10}
                            fill="white"
                            stroke="black"
                        />
                    )
                }
                return <></>
            })}
            {
                corners.map((coord, index) => {
                    if (index < corners.length - 1) {
                        const nextIndex = (index + 1) % (corners.length - 1);
                        const next = corners[nextIndex];

                        const isHorizontal = Math.abs(coord.y - next.y) < 3

                        const thickness = 10

                        return (
                            <Rect
                                key={`rect-${index}`}
                                x={isHorizontal ? coord.x : coord.x - thickness / 2}
                                y={isHorizontal ? coord.y - thickness / 2 : coord.y}
                                width={isHorizontal ? next.x - coord.x : thickness}
                                height={isHorizontal ? thickness : next.y - coord.y}
                                fill={"white"}
                                stroke="black"

                                draggable

                                onDragMove={(e) => handleDragMove(e, index)}
                            />
                        )
                    }
                    return <></>
                })
            }
        </>
    )
}

// https://konvajs.org/docs/sandbox/Image_Resize.html

export default AdjustRoomShape;