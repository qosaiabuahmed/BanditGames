import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router-dom';
import Game from './pages/Game';
import GameSetup from './pages/GameSetup';
import { AuthProvider } from './contexts/AuthContext';

import './App.scss';

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/" element={<Navigate to="/game/setup" replace />} />
                    <Route path="/game/setup" element={<GameSetup/>} />
                    <Route path="/game/play" element={<Game/>} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
