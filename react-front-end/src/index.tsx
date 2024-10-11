import React from 'react';
import ReactDOM from 'react-dom/client';
import { Provider } from 'react-redux';

import { GoogleOAuthProvider } from '@react-oauth/google'

import './assets/styles/index.css';

import App from './app/App';
import { store } from './app/store';

import reportWebVitals from './utils/reportWebVitals';
import { AUTH_CLIENT_ID } from './utils/constants';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <Provider store={store}>
      <GoogleOAuthProvider clientId={AUTH_CLIENT_ID}>
        <App />
      </GoogleOAuthProvider>
    </Provider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
