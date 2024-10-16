import React, { ReactElement } from 'react';

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import RoomsList from '@/features/rooms/RoomsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/LoginPage';
import { useAppSelector } from '@/hooks/useAppHooks';
import { selectAuthStatus } from '@/features/auth/authSlice';
import { CircularProgress } from '@mui/material';


const ProtectedRoute = ({ children }: { children: ReactElement}) => {
    const authStatus = useAppSelector(selectAuthStatus);

    if (authStatus === 'none') {
        return <Navigate to="/login" replace />
    }
    if (authStatus === 'pending' || authStatus === 'idle') {
        return <CircularProgress/>
    }

    // children is a required prop, so we can have assert with !
    return children!
}

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
            <Route path="/login" Component={LoginPage} />
                <Route path="/*"
                    element={
                        <ProtectedRoute>
                            <Routes>
                                <Route path="/home" Component={HomePage} />
                                <Route path="/projects/:projectId/rooms" Component={RoomsList} /> 
                                <Route path="/projects/:projectId/rooms/add" Component={AddRoom} /> 
                                <Route path="/projects/:projectId/rooms/:roomId/edit" Component={EditRoom} /> 
                                <Route path="/projects" Component={ProjectsList} />
                                <Route path="/projects/add" Component={AddProject} /> 
                                <Route path="/projects/:projectId/edit" Component={EditProject} /> 
                            </Routes>
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
