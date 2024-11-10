import { fetchUserInfo, logoutUser } from '@/features/auth/authSlice';
import { useAppDispatch } from '@/hooks/useAppHooks';
import { useEffect } from 'react';
import '../assets/styles/App.css';
import Pages from '../routes/routes';

function App() {
    const dispatch = useAppDispatch();

    useEffect(() => {
        const token = localStorage.getItem('jwt')
        const cachedUserInfo = localStorage.getItem('userInfo');
        const isOffline = !navigator.onLine;

        // Check if user has previously logged in
        if (token) {
            if (isOffline && cachedUserInfo) {
                // Offline: Use cached user info if available
                const userInfo = JSON.parse(cachedUserInfo);
                dispatch({
                    type: 'auth/fetchUserInfo/fulfilled',
                    payload: userInfo,
                });
            }
            else {
                // Online: Send token to backend to fetch user data
                dispatch(fetchUserInfo(token));
            }
        }
        else {
            // Set auth status to 'none' because there is no jwt
            logoutUser();
        }

        // Listen for online/offline status changes to handle reconnecting logic
        const handleOnline = () => {
            if (token) {
                dispatch(fetchUserInfo(token));
            }
        };

        window.addEventListener('online', handleOnline);

        return () => {
            window.removeEventListener('online', handleOnline);
        };
    }, [dispatch]);


    return (
        <div className="App">
            <Pages />
        </div>
    );
}

export default App;
