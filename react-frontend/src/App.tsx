import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import HomePage from './components/pages/HomePage';
import LoginPage from './components/pages/LoginPage';
import RegisterPage from './components/pages/RegisterPage';
import DashboardPage from './components/pages/DashboardPage';
import AdminDashboardPage from './components/pages/AdminDashboardPage';
import ProfilePage from './components/pages/ProfilePage';
import EditProfilePage from './components/pages/EditProfilePage';
import FindFriendsPage from './components/pages/FindFriendsPage';
import FriendsPage from './components/pages/FriendsPage';
import GameCatalogPage from './components/pages/GameCatalogPage';
import GameDetailsPage from './components/pages/GameDetailsPage';
import RegisterGamePage from './components/pages/RegisterGamePage';
import AchievementsPage from './components/pages/AchievementsPage';
import ProtectedRoute from './components/common/ProtectedRoute';
import AdminProtectedRoute from './components/common/AdminProtectedRoute';
import ChatWidget from './components/chatbot/ChatWidget';
import NotificationsPage from './components/pages/NotificationsPage';
import {NotificationProvider} from "./components/notifications/NotificationContent.tsx";
import NotificationToast from './components/notifications/NotificationToast';
import PendingInvitesPanel from './components/features/PendingInvitesPanel';
import LobbyInviteToast from './components/features/LobbyInviteToast';
import './App.css';

function App() {
    return (
        <Router>
            <NotificationProvider>
                <Routes>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route path="/register" element={<RegisterPage/>}/>
                    <Route
                        path="/dashboard"
                        element={
                            <ProtectedRoute>
                                <DashboardPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin"
                        element={
                            <AdminProtectedRoute>
                                <AdminDashboardPage/>
                            </AdminProtectedRoute>
                        }
                    />
                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/edit-profile"
                        element={
                            <ProtectedRoute>
                                <EditProfilePage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/find-friends"
                        element={
                            <ProtectedRoute>
                                <FindFriendsPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/friends"
                        element={
                            <ProtectedRoute>
                                <FriendsPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route path="/games" element={<GameCatalogPage/>}/>
                    <Route path="/games/:id" element={<GameDetailsPage/>}/>
                    <Route
                        path="/games/new"
                        element={
                            <ProtectedRoute>
                                <RegisterGamePage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/notifications"
                        element={
                            <ProtectedRoute>
                                <NotificationsPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/invites"
                        element={
                            <ProtectedRoute>
                                <PendingInvitesPanel/>
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/achievements"
                        element={
                            <ProtectedRoute>
                                <AchievementsPage/>
                            </ProtectedRoute>
                        }
                    />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>

                {/* Global Components */}
                <ChatWidget/>
                <NotificationToast/>
                <LobbyInviteToast/>
            </NotificationProvider>
        </Router>
    );
}

export default App;
