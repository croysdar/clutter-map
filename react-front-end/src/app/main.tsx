import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import '../assets/styles/index.css'

import { Provider } from 'react-redux';
import { GoogleOAuthProvider } from '@react-oauth/google';

import App from './App';
import { store } from './store';

import { ThemeProvider } from '@emotion/react';
import { CssBaseline } from '@mui/material';
import theme from '../contexts/theme';
import { AUTH_CLIENT_ID } from '../utils/constants';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <Provider store={store}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                <GoogleOAuthProvider clientId={AUTH_CLIENT_ID}>
                    <App />
                </GoogleOAuthProvider>
            </ThemeProvider>
        </Provider>
    </StrictMode>,
)
