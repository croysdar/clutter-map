import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import RoomsList from '@/features/rooms/RoomsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                {/* <Route exact path="/" component={Home} /> */}
                {/* <Route path="/add-item" component={AddItem} /> */}
                {/* <Route path="/rooms" Component={RoomsList} /> 
                <Route path="/rooms/add" Component={AddRoom} /> 
                <Route path="/rooms/:roomID/edit" Component={EditRoom} /> */}
                <Route path="/projects/:projectID/rooms" Component={RoomsList} /> 
                <Route path="/projects/:projectID/rooms/add" Component={AddRoom} /> 
                <Route path="/projects/:projectID/rooms/:roomID/edit" Component={EditRoom} /> 
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
