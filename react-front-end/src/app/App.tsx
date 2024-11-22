import { ReactElement, useEffect } from 'react';

import AppBreadcrumbs from '@/components/navigation/Breadcrumbs';
import Navbar from '@/components/navigation/Navbar';

import { fetchUserInfo, rejectAuthStatus, selectAuthStatus } from '@/features/auth/authSlice';
import { AddItem } from '@/features/items/AddItem';
import EditItem from '@/features/items/EditItem';
import ItemDetails from '@/features/items/ItemDetails';
import { AddOrgUnit } from '@/features/orgUnits/AddOrgUnit';
import EditOrgUnit from '@/features/orgUnits/EditOrgUnit';
import OrgUnitDetails from '@/features/orgUnits/OrgUnitDetails';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';
import ProjectDetails from '@/features/projects/ProjectDetails';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
import RoomDetails from '@/features/rooms/RoomDetails';

import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';
import AboutPage from '@/pages/AboutPage';
import HomePage from '@/pages/HomePage';

import { Box, CircularProgress, Container } from '@mui/material';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

import { ROUTES } from '@/utils/constants';
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
                        <Route path={ROUTES.home} Component={HomePage} />
                        <Route path={ROUTES.about} Component={AboutPage} />
                        <Route path="/*"
                            element={
                                <ProtectedRoute>
                                    <Routes>
                                        <Route path={ROUTES.projects} Component={ProjectsList} />

                                        <Route path={ROUTES.projectAdd} Component={AddProject} />
                                        <Route path={ROUTES.projectDetails(":projectId")} Component={ProjectDetails} />
                                        <Route path={ROUTES.projectEdit(":projectId")} Component={EditProject} />

                                        <Route path={ROUTES.roomAdd(":projectId")} Component={AddRoom} />
                                        <Route path={ROUTES.roomDetails(":projectId", ":roomId")} Component={RoomDetails} />
                                        <Route path={ROUTES.roomEdit(":projectId", ":roomId")} Component={EditRoom} />

                                        <Route path={ROUTES.orgUnitAdd(":projectId", ":roomId")} Component={AddOrgUnit} />
                                        <Route path={ROUTES.orgUnitDetails(":projectId", ":roomId", ":orgUnitId")} Component={OrgUnitDetails} />
                                        <Route path={ROUTES.orgUnitEdit(":projectId", ":roomId", ":orgUnitId")} Component={EditOrgUnit} />

                                        <Route path={ROUTES.itemAdd(":projectId", ":roomId", ":orgUnitId")} Component={AddItem} />
                                        <Route path={ROUTES.itemDetails(":projectId", ":roomId", ":orgUnitId", ":itemId")} Component={ItemDetails} />
                                        <Route path={ROUTES.itemEdit(":projectId", ":roomId", ":orgUnitId", ":itemId")} Component={EditItem} />
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
