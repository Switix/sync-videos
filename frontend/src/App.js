import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import RoomPage from './page/RoomPage';
import LandingPage from './page/LandingPage';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/room/:roomId" element={<RoomPage />} />
            </Routes>
        </Router>
    );
}

export default App;