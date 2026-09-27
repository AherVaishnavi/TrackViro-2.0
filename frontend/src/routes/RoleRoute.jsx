import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Used inside a ProtectedRoute, so `user` is guaranteed non-null here.
 * `role` prop must match UserResponse.role exactly: "EMPLOYEE",
 * "MANAGER", or "FINANCE" (verified against the backend's actual DTO —
 * not guessed).
 */
export default function RoleRoute({ role }) {
  const { user } = useAuth();

  if (user.role !== role) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
