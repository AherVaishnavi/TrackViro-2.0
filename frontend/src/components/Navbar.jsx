import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const HOME_BY_ROLE = {
  EMPLOYEE: "/employee",
  MANAGER: "/manager",
  FINANCE: "/finance",
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
      <div className="container-fluid">
        <Link className="navbar-brand fw-bold" to={user ? HOME_BY_ROLE[user.role] : "/"}>
          TrackViro
        </Link>

        {user && (
          <>
            <div className="d-flex gap-3">
              {user.role === "EMPLOYEE" && (
                <Link className="nav-link text-white" to="/employee/submit-expense">
                  Submit Expense
                </Link>
              )}
              {user.role === "FINANCE" && (
                <>
                  <Link className="nav-link text-white" to="/finance/categories">
                    Categories
                  </Link>
                  <Link className="nav-link text-white" to="/finance/departments">
                    Departments
                  </Link>
                  <Link className="nav-link text-white" to="/finance/users">
                    Users
                  </Link>
                </>
              )}
            </div>

            <div className="d-flex align-items-center ms-auto gap-3">
              <Link className="nav-link text-white" to="/profile">
                Profile
              </Link>
              <span className="text-white-50 small">
                {user.name} · <span className="text-uppercase">{user.role}</span>
              </span>
              <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>
                Log out
              </button>
            </div>
          </>
        )}
      </div>
    </nav>
  );
}