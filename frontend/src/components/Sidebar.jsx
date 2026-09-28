import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { formatCurrency } from "../utils/formatCurrency";

/**
 * Ported from the old templates' .sidebar structure (identical markup
 * across employee_dashboard.html/manager_dashboard.html/finance_
 * dashboard.html/submit_expense.html, just different nav items).
 *
 * navItems: [{ label, icon, active, onClick, badge? }]
 * setupLinks (Finance only): [{ label, icon, to }]
 * balance (Employee only): { remaining, limit } — renders the
 * balance-ok/warn/over strip exactly as the old app's th:classappend
 * logic decided it (remaining <= 0 -> over, <= 20% of limit -> warn).
 */
export default function Sidebar({ navItems = [], setupLinks = [], balance, onLimitRequestClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  let balanceClass = "balance-ok";
  if (balance) {
    if (balance.remaining == null || balance.remaining <= 0) balanceClass = "balance-over";
    else if (balance.remaining <= balance.limit * 0.2) balanceClass = "balance-warn";
  }

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <div className="sidebar-logo-mark">💼</div>
        <span className="sidebar-brand-name">TrackViro</span>
      </div>

      <div className="sidebar-profile">
        <div className="profile-avatar-default">👤</div>
        <div className="profile-info">
          <div className="profile-name">{user.name}</div>
          <div className="profile-dept">
            {user.departmentName ? `🏢 ${user.departmentName}` : "No Department"}
          </div>
          <span className="profile-role-badge">{user.role}</span>
        </div>
      </div>

      {balance && (
        <>
          <div className={`balance-strip ${balanceClass}`}>
            <div className="balance-label">Remaining Balance</div>
            <div className="balance-amount">{formatCurrency(balance.remaining)}</div>
            <div className="balance-sub">Limit: {formatCurrency(balance.limit)}</div>
          </div>
          <button className="btn-limit" onClick={onLimitRequestClick}>
            ＋ Request Limit Increase
          </button>
        </>
      )}

      <nav className="sidebar-nav">
        {navItems.map((item, i) =>
          item.section ? (
            <div className="nav-section-label" key={`s${i}`}>
              {item.section}
            </div>
          ) : (
            <button
              key={item.label}
              className={`nav-link-item ${item.active ? "active" : ""}`}
              onClick={item.onClick}
            >
              <span className="nav-icon">{item.icon}</span> {item.label}
              {item.badge > 0 && <span className="nav-badge">{item.badge}</span>}
            </button>
          )
        )}

        {setupLinks.length > 0 && (
          <>
            <div className="nav-section-label">Setup</div>
            {setupLinks.map((link) => (
              <a key={link.to} className="nav-link-item" href={link.to} onClick={(e) => { e.preventDefault(); navigate(link.to); }}>
                <span className="nav-icon">{link.icon}</span> {link.label}
              </a>
            ))}
          </>
        )}
      </nav>

      <div className="sidebar-bottom">
        {user.role === "EMPLOYEE" && (
          <a
            className="btn-submit-expense"
            href="/employee/submit-expense"
            onClick={(e) => {
              e.preventDefault();
              navigate("/employee/submit-expense");
            }}
          >
            ＋ Submit Expense
          </a>
        )}
        <button className="btn-logout" onClick={handleLogout}>
          → Logout
        </button>
      </div>
    </div>
  );
}
