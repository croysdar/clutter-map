import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
// import ListView from '../pages/ListView'; // Import the ListView component

const Pages: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                {/* <Route exact path="/" component={Home} /> */}
                {/* <Route path="/add-item" component={AddItem} /> */}
                {/* <Route path="/list-view" Component={ListView} />  */}
            </Routes>
        </BrowserRouter>
    );
};

export default Pages;
