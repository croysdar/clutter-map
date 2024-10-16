import React, { useEffect } from 'react';
import '../assets/styles/App.css';
import Pages from '../routes/routes';
import { fetchUserInfo, rejectAuthStatus } from '@/features/auth/authSlice';
import { useAppDispatch } from '@/hooks/useAppHooks';

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
      <header className="App-header">
        <Pages />
      </header>
    </div>
  );
}

export default App;
