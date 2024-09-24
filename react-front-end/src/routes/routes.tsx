import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import RoomsList from '@/features/rooms/RoomsList';

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                {/* <Route exact path="/" component={Home} /> */}
                {/* <Route path="/add-item" component={AddItem} /> */}
                <Route path="/rooms" Component={RoomsList} /> 
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
