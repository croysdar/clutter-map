import { ReactElement, useEffect, useRef } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';

/* ------------- Material UI ------------- */
import { Box, CircularProgress, Container } from '@mui/material';

/* ------------- Components ------------- */
import AppBreadcrumbs from '@/components/navigation/Breadcrumbs';
import Navbar from '@/components/navigation/Navbar';

/* ------------- Pages ------------- */
import { AddItem } from '@/features/items/AddItem';
import EditItem from '@/features/items/EditItem';
import ItemDetails from '@/features/items/ItemDetails';
import { AddOrgUnit } from '@/features/orgUnits/AddOrgUnit';
import EditOrgUnit from '@/features/orgUnits/EditOrgUnit';
import { AssignItemsToOrgUnit, RemoveOrgUnitItems } from '@/features/orgUnits/EditOrgUnitItems';
import OrgUnitDetails from '@/features/orgUnits/OrgUnitDetails';
import { AddProject } from '@/features/projects/AddProject';
import EditProject from '@/features/projects/EditProject';
import ProjectDetails from '@/features/projects/ProjectDetails';
import ProjectsList from '@/features/projects/ProjectsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
import { AssignOrgUnitsToRoom, RemoveRoomOrgUnits } from '@/features/rooms/EditRoomOrgUnits';
import RoomDetails from '@/features/rooms/RoomDetails';
import AboutPage from '@/pages/AboutPage';
import HomePage from '@/pages/HomePage';

/* ------------- Redux ------------- */
import { fetchUserInfo, logoutUser, selectAuthStatus } from '@/features/auth/authSlice';
import { initIDB } from '@/features/offline/idbSlice';
import { useAppDispatch, useAppSelector } from '@/hooks/useAppHooks';

/* ------------- Constants ------------- */
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
    const isInit = useRef(false);

    useEffect(() => {
        // isInit prevents duplicate running of useEffect
        if (isInit.current) return;
        isInit.current = true;

        const fetchUserInfoIfToken = async () => {
            const token = localStorage.getItem('jwt')
            if (token) {
                await dispatch(fetchUserInfo(token));
            }
            else {
                logoutUser();
            }
        };

        const initializeApp = async () => {
            await dispatch(initIDB());

            await fetchUserInfoIfToken();
        }

        const handleOnline = () => {
            fetchUserInfoIfToken();
        }

        window.addEventListener('online', handleOnline);

        initializeApp();

        return () => {
            window.removeEventListener('online', handleOnline);
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
                                        <Route path={ROUTES.roomRemoveOrgUnits(":projectId", ":roomId")} Component={RemoveRoomOrgUnits} />
                                        <Route path={ROUTES.roomAssignOrgUnits(":projectId", ":roomId")} Component={AssignOrgUnitsToRoom} />

                                        <Route path={ROUTES.orgUnitAdd(":projectId", ":roomId")} Component={AddOrgUnit} />
                                        <Route path={ROUTES.orgUnitDetails(":projectId", ":roomId", ":orgUnitId")} Component={OrgUnitDetails} />
                                        <Route path={ROUTES.orgUnitEdit(":projectId", ":roomId", ":orgUnitId")} Component={EditOrgUnit} />
                                        <Route path={ROUTES.orgUnitRemoveItems(":projectId", ":roomId", ":orgUnitId")} Component={RemoveOrgUnitItems} />
                                        <Route path={ROUTES.orgUnitAssignItems(":projectId", ":roomId", ":orgUnitId")} Component={AssignItemsToOrgUnit} />

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
