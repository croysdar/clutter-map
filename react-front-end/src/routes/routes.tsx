import React, { ReactElement } from 'react';

import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

import { selectAuthStatus } from '@/features/auth/authSlice';
import { AddOrgUnit } from '@/features/orgUnits/AddOrgUnit';
import EditOrgUnit from '@/features/orgUnits/EditOrgUnit';
import OrgUnitsList from '@/features/orgUnits/OrgUnitsList';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
import RoomsList from '@/features/rooms/RoomsList';
import { useAppSelector } from '@/hooks/useAppHooks';
import HomePage from '@/pages/HomePage';
import { CircularProgress, Container } from '@mui/material';


const ProtectedRoute = ({ children }: { children: ReactElement }) => {
    const authStatus = useAppSelector(selectAuthStatus);

    if (authStatus === 'none') {
        return <Navigate to="/" replace />
    }
    if (authStatus === 'pending' || authStatus === 'idle') {
        return <CircularProgress />
    }

    // children is a required prop, so we can have assert with !
    return children!
}

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Container maxWidth="md" sx={{
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                height: '100vh',
                textAlign: 'center',
                gap: 3,
            }}>
            <Routes>
            <Route path="/" Component={HomePage} />
                <Route path="/*"
                    element={
                        <ProtectedRoute>
                            <Routes>
                                <Route path="/projects/:projectId/rooms" Component={RoomsList} /> 
                                <Route path="/projects/:projectId/rooms/add" Component={AddRoom} /> 
                                <Route path="/projects/:projectId/rooms/:roomId/edit" Component={EditRoom} />
                                <Route path="/projects/:projectId/rooms/:roomId/org-units" Component={OrgUnitsList} /> 
                                <Route path="/projects/:projectId/rooms/:roomId/org-units/add" Component={AddOrgUnit} /> 
                                <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/edit" Component={EditOrgUnit} /> 
                                <Route path="/projects" Component={ProjectsList} />
                                <Route path="/projects/add" Component={AddProject} /> 
                                <Route path="/projects/:projectId/edit" Component={EditProject} /> 
                            </Routes>
                        </ProtectedRoute>
                    }
                />
            </Routes>
            </Container>
        </BrowserRouter>
    );
};

export default Pages;
