import { Link, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { HomePage } from "./pages/HomePage";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { UploadPage } from "./pages/UploadPage";
import { VideoPage } from "./pages/VideoPage";
import { ChannelPage } from "./pages/ChannelPage";
import { DashboardPage } from "./pages/DashboardPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { ProtectedRoute } from "./components/ProtectedRoute";

function Layout() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header>
        <h1>Video Streaming</h1>
        <nav>
          <Link to="/">Home</Link>
          {isAuthenticated && <Link to="/dashboard">Dashboard</Link>}
          {isAuthenticated && <Link to="/upload">Upload</Link>}
          {isAuthenticated && user && <Link to={`/users/${user.id}/channel`}>My Channel</Link>}
          {!isAuthenticated && <Link to="/login">Login</Link>}
          {!isAuthenticated && <Link to="/register">Register</Link>}
          {isAuthenticated && (
            <button type="button" onClick={() => void logout()} className="linkish">
              Logout
            </button>
          )}
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/videos/:id" element={<VideoPage />} />
          <Route path="/users/:userId/channel" element={<ChannelPage />} />
          <Route path="/login" element={isAuthenticated ? <Navigate to="/" /> : <LoginPage />} />
          <Route path="/register" element={isAuthenticated ? <Navigate to="/" /> : <RegisterPage />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/upload"
            element={
              <ProtectedRoute requiredRoles={["CREATOR", "ADMIN"]}>
                <UploadPage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Layout />
    </AuthProvider>
  );
}
