import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import RoomsList from '@/features/rooms/RoomsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                {/* <Route exact path="/" component={Home} /> */}
                {/* <Route path="/add-item" component={AddItem} /> */}
                {/* <Route path="/rooms" Component={RoomsList} /> 
                <Route path="/rooms/add" Component={AddRoom} /> 
                <Route path="/rooms/:roomId/edit" Component={EditRoom} /> */}
                <Route path="/projects/:projectId/rooms" Component={RoomsList} /> 
                <Route path="/projects/:projectId/rooms/add" Component={AddRoom} /> 
                <Route path="/projects/:projectId/rooms/:roomId/edit" Component={EditRoom} /> 
                <Route path="/projects" Component={ProjectsList} />
                <Route path="/projects/add" Component={AddProject} /> 
                <Route path="/projects/:projectId/edit" Component={EditProject} /> 
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
