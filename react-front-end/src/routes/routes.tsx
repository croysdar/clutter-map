import React, { ReactElement } from 'react';

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import RoomsList from '@/features/rooms/RoomsList';
import { AddRoom } from '@/features/rooms/AddRoom';
import EditRoom from '@/features/rooms/EditRoom';
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
                                <Route path="/rooms" Component={RoomsList} />
                                <Route path="/rooms/add" Component={AddRoom} />
                                <Route path="/rooms/:roomID/edit" Component={EditRoom} />
                                {/* <Route path="/add-item" component={AddItem} /> */}
                            </Routes>
                        </ProtectedRoute>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
