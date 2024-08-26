// src/pages/BlueprintView.tsx
import React, { useState } from 'react';

import { Layer, Rect } from 'react-konva';

import KonvaImage from 'components/common/KonvaImage';
import { KonvaEventObject } from 'konva/lib/Node';

import WoodenChair from 'assets/images/wooden-chair.svg'

interface BaseShapeProps {
    id: string;
    x: number;
    y: number;
    width: number;
    height: number;
}

interface SvgShape extends BaseShapeProps {
    shapeType: 'svg';
    svg: string;
    fill?: never;
}

interface RectShape extends BaseShapeProps {
    shapeType: 'rect';
    fill: string;
    svg?: never;
}

type ShapeProps = SvgShape | RectShape;


// TODO
/*
*/

const FillRoomBlueprint: React.FC = () => {
    const [shapes, setShapes] = useState<ShapeProps[]>([]);

    // const shapeBank: ShapeProps[] = [
    //     { id: '1', x: 50, y: 50, width: 50, height: 50, fill: 'red' },
    //     { id: '2', x: 50, y: 150, width: 50, height: 50, fill: 'green' },
    //     { id: '3', x: 50, y: 250, width: 50, height: 50, fill: 'blue' },
    // ]

    // const chair = useImage(WoodenChair)

    const chairBank: ShapeProps[] = [
        { id: 'chair-1', x: 50, y: 50, width: 50, height: 50, shapeType: 'svg', svg: WoodenChair },
        { id: 'chair-2', x: 50, y: 150, width: 50, height: 50, shapeType: 'rect', fill: 'red' } ]

    // const shelfBank : ShapeProps[] = [
    //     { id: '1', x: 50, y: 50, width: 50, height: 50, fill: 'red' },
    //     { id: '2', x: 50, y: 150, width: 50, height: 50, fill: 'green' },
    //     { id: '3', x: 50, y: 250, width: 50, height: 50, fill: 'blue' },
    // ]

    // const tableBank : ShapeProps[] = [
    //     { id: '1', x: 50, y: 50, width: 50, height: 50, fill: 'red' },
    //     { id: '2', x: 50, y: 150, width: 50, height: 50, fill: 'green' },
    //     { id: '3', x: 50, y: 250, width: 50, height: 50, fill: 'blue' },
    // ]

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

    const renderShape = (shape: ShapeProps, isBank: Boolean) => {
        const buildShapeProps = (shape: ShapeProps, isBank: Boolean) => {
            return {
                key: shape.id,
                onMouseDown: isBank ? (e: KonvaEventObject<MouseEvent>) => handleCreateShape(e, shape) : undefined,
                draggable: !isBank,
                onDragEnd: isBank ? undefined : (e: KonvaEventObject<MouseEvent>) => handleDragEnd(e, shape.id),
            }
        }

        const shapeProps = buildShapeProps(shape, isBank)

        switch (shape.shapeType) {
            case 'svg':
                return  <KonvaImage {...shapeProps} {...shape} />
            case 'rect':
                return  <Rect {...shapeProps} {...shape} />
            default:
                return <></>;
        }
    }

    return (
        <Layer>
            {/*  shape bank */}
            {chairBank.map((shape) => renderShape(shape, true))}
            {/* Created shapes */}
            {shapes.map((shape) => renderShape(shape, false))}
        </Layer>
    )
}

// https://konvajs.org/docs/sandbox/Image_Resize.html

export default FillRoomBlueprint;