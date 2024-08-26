// src/pages/BlueprintView.tsx
import React, { useState } from 'react';

import { Stage } from 'react-konva';
import CreateRoomBlueprint from 'components/blueprints/CreateRoomBlueprint';
import { Typography } from '@mui/material';
import FillRoomBlueprint from 'components/blueprints/FillRoomBlueprint';

const BlueprintView: React.FC = () => {
    const [ state, setState ] = useState<string>("create-room");

    const renderCurrentState = () => {
        switch(state) {
            case "create-room": 
                return(
                    <CreateRoomBlueprint/>
                )
            case "fill-room": 
                return(
                    <FillRoomBlueprint/>
                )
            default:
                <>something is wrong</>
        }
    }


    return (
        <div>
            <Typography> Blueprint view in progress... </Typography>

            <Stage width={window.innerWidth * 0.8} height={window.innerHeight * 0.8}>
                {renderCurrentState()}
            </Stage>
        </div>
    )
}

// https://konvajs.org/docs/sandbox/Image_Resize.html

export default BlueprintView;