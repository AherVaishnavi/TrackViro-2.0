import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";
import RoleRoute from "./routes/RoleRoute";

import Login from "./pages/Login";
import EmployeeDashboard from "./pages/EmployeeDashboard";
import SubmitExpense from "./pages/SubmitExpense";
import ManagerDashboard from "./pages/ManagerDashboard";
import FinanceDashboard from "./pages/FinanceDashboard";
import Categories from "./pages/Categories";
import Departments from "./pages/Departments";
import Users from "./pages/Users";
import Profile from "./pages/Profile";

const HOME_BY_ROLE = {
  EMPLOYEE: "/employee",
  MANAGER: "/manager",
  FINANCE: "/finance",
};

function RoleHome() {
  const { user } = useAuth();
  return <Navigate to={HOME_BY_ROLE[user.role] || "/login"} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<RoleHome />} />

        {/* Shared by all three roles */}
        <Route path="/profile" element={<Profile />} />

        <Route element={<RoleRoute role="EMPLOYEE" />}>
          <Route path="/employee" element={<EmployeeDashboard />} />
          <Route path="/employee/submit-expense" element={<SubmitExpense />} />
        </Route>

        <Route element={<RoleRoute role="MANAGER" />}>
          <Route path="/manager" element={<ManagerDashboard />} />
        </Route>

        <Route element={<RoleRoute role="FINANCE" />}>
          <Route path="/finance" element={<FinanceDashboard />} />
          <Route path="/finance/categories" element={<Categories />} />
          <Route path="/finance/departments" element={<Departments />} />
          <Route path="/finance/users" element={<Users />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}