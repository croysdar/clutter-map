// src/pages/KonvaImage.tsx
import React from 'react';

import { Image, Text } from 'react-konva';
import useImage from 'use-image'

import { ShapeConfig } from 'konva/lib/Shape';

interface KonvaImageProps extends ShapeConfig {
    svg: string;
    id: string;
    x: number;
    y: number;
    width: number;
    height: number;
}

const KonvaImage: React.FC<KonvaImageProps> = (props) => {
    const { svg, ...rest } = props;

    const [image, status] = useImage(svg);


    if (status === "loading")
        return <Text text="loading" fontSize={15} fill='white' {...rest}/>
    
    if (status === "failed")
        return <Text text="failed" fontSize={15} fill='white' {...rest}/>

    return (
        <Image image={image} {...rest}/>
    )
}
export default KonvaImage;