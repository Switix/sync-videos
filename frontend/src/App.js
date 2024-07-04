import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import RoomPage from './page/RoomPage';
import LandingPage from './page/LandingPage';
import ErrorPage from './page/ErrorPage';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/room/:roomId" element={<RoomPage />} />

                <Route path="/404" element={<ErrorPage />} />
                <Route path="*" element={<ErrorPage />} />
            </Routes>
        </Router>
    );
}

export default App;