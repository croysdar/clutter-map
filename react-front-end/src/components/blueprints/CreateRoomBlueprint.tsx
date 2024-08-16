// src/pages/BlueprintView.tsx
import React, { useState } from 'react';

import { Layer, Rect } from 'react-konva';

interface ShapeProps {
    id: string;
    x: number;
    y: number;
    width: number;
    height: number;
    fill: string;
}

const CreateRoomBlueprint: React.FC = () => {
    const [shapes, setShapes] = useState<ShapeProps[]>([]);

    const shapeBank: ShapeProps[] = [
        { id: '1', x: 50, y: 50, width: 50, height: 50, fill: 'red' },
        { id: '2', x: 50, y: 150, width: 50, height: 50, fill: 'green' },
        { id: '3', x: 50, y: 250, width: 50, height: 50, fill: 'blue' },
    ]

    const handleCreateShape = (e: any, shape: ShapeProps) => {

        const stage = e.target.getStage();
        const pointerPosition = stage.getPointerPosition();

        if (pointerPosition) {
            const newShape = {
                ...shape,
                id: `${shape.id}-${shapes.length + 1}`,
                x: shape.x + 100,
                y: shape.y,
            };

            setShapes([...shapes, newShape]);
        }
    };

    const handleDragEnd = (e: any, id: string) => {
        const updatedShapes = shapes.map((shape) =>
            shape.id === id
                ? {
                    ...shape,
                    x: e.target.x(),
                    y: e.target.y(),
                }
                : shape
        );
        setShapes(updatedShapes);
    };

    return (
        <Layer>
            {/*  shape bank */}
            {shapeBank.map((shape) => (
                <Rect
                    key={shape.id}
                    {...shape}
                    onMouseDown={(e) => handleCreateShape(e, shape)}
                />
            ))}
            {/* Created shapes */}
            {shapes.map((shape) => (
                <Rect
                    key={shape.id}
                    {...shape}
                    draggable
                    onDragEnd={(e) => handleDragEnd(e, shape.id)}
                />
            ))}

        </Layer>
    )
}

// https://konvajs.org/docs/sandbox/Image_Resize.html

export default CreateRoomBlueprint;