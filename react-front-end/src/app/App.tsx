import { ReactElement, useEffect } from 'react';

import AppBreadcrumbs from '@/components/navigation/Breadcrumbs';
import Navbar from '@/components/navigation/Navbar';

import { fetchUserInfo, rejectAuthStatus, selectAuthStatus } from '@/features/auth/authSlice';
import { AddItem } from '@/features/items/AddItem';
import EditItem from '@/features/items/EditItem';
import ItemsList from '@/features/items/ItemsList';
import { AddOrgUnit } from '@/features/orgUnits/AddOrgUnit';
import EditOrgUnit from '@/features/orgUnits/EditOrgUnit';
import OrgUnitsList from '@/features/orgUnits/OrgUnitsList';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';
import ProjectDetails from '@/features/projects/ProjectDetails';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';

import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';
import AboutPage from '@/pages/AboutPage';
import HomePage from '@/pages/HomePage';

import { Box, CircularProgress, Container } from '@mui/material';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

import '../assets/styles/App.css';

const ProtectedRoute = ({ children }: { children: ReactElement }) => {
    const authStatus = useAppSelector(selectAuthStatus);

    if (authStatus === 'none') {
        return <Navigate to={"/"} replace />
    }
    if (authStatus === 'pending' || authStatus === 'idle') {
        return <CircularProgress />
    }

    // children is a required prop, so we can have assert with !
    return children!
}

function App() {
    const dispatch = useAppDispatch();

    useEffect(() => {
        const token = localStorage.getItem('jwt')

        // Check if user has previously logged in
        if (token) {
            // send token to backend to fetch user data
            dispatch(fetchUserInfo(token));
        }
        else {
            // Set auth status to 'none' because there is no jwt
            dispatch(rejectAuthStatus());
        }
    }, [dispatch]);


    return (
        <div className="App">
            <BrowserRouter>
                <Box sx={{ minHeight: '10vh' }}>
                    <Navbar />
                    <AppBreadcrumbs />
                </Box>
                <Container
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'center',
                        alignItems: 'center',
                        minHeight: '90vh',
                        textAlign: 'center',
                    }}>
                    <Routes>
                        <Route path="/" Component={HomePage} />
                        <Route path="/about" Component={AboutPage} />
                        <Route path="/*"
                            element={
                                <ProtectedRoute>
                                    <Routes>
                                        <Route path="/projects" Component={ProjectsList} />
                                        <Route path="/projects/:projectId" Component={ProjectDetails} />
                                        <Route path="/projects/:projectId/edit" Component={EditProject} />
                                        <Route path="/projects/add" Component={AddProject} />

                                        <Route path="/projects/:projectId/rooms/add" Component={AddRoom} />
                                        <Route path="/projects/:projectId/rooms/:roomId/edit" Component={EditRoom} />

                                        <Route path="/projects/:projectId/rooms/:roomId/org-units" Component={OrgUnitsList} />
                                        <Route path="/projects/:projectId/rooms/:roomId/org-units/add" Component={AddOrgUnit} />
                                        <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/edit" Component={EditOrgUnit} />

                                        <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/items" Component={ItemsList} />
                                        <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/items/add" Component={AddItem} />
                                        <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/items/:itemId/edit" Component={EditItem} />
                                        {/* <Route path="/projects/:projectId/rooms/:roomId/org-units/:orgUnitId/items/:itemId" Component={ItemDetails} /> */}
                                    </Routes>
                                </ProtectedRoute>
                            }
                        />
                    </Routes>
                </Container>
            </BrowserRouter>
        </div>
    );
}

export default App;
