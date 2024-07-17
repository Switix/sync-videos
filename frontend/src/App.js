import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import RoomPage from './page/RoomPage';
import LandingPage from './page/LandingPage';
import ErrorPage from './page/ErrorPage';
import Header from './component/Header';

import { persistor, store } from './redux/store';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';

function App() {
    return (
        <Provider store={store}>
            <PersistGate loading={null} persistor={persistor}>
                <Router>
                    <div class="flex flex-col h-screen bg-neutral-900">
                        <Header />
                        <Routes>
                            <Route path="/" element={<LandingPage />} />
                            <Route path="/room/:roomId" element={<RoomPage />} />
                            <Route path="/404" element={<ErrorPage />} />
                            <Route path="*" element={<ErrorPage />} />
                        </Routes>
                    </div>
                </Router>
            </PersistGate>
        </Provider>
    );
}

export default App;