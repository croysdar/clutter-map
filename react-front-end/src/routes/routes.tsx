import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ListView, BlueprintView } from 'pages'

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                {/* <Route exact path="/" component={Home} /> */}
                {/* <Route path="/add-item" component={AddItem} /> */}
                <Route path="/rooms" Component={ListView} /> 
                <Route path="/blueprint" Component={BlueprintView} /> 
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
