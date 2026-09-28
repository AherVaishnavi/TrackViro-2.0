import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import ExpenseTable from "../components/ExpenseTable";
import ChartCard from "../components/ChartCard";
import LimitRequestModal from "../components/LimitRequestModal";
import employeeApi from "../services/employeeApi";
import { formatCurrency, statusPillClass } from "../utils/formatCurrency";

// Ported from employee_dashboard.html: one page, three internal
// "panels" toggled by the sidebar nav (dashboardView / myExpenses /
// myLimitRequests) exactly like the old showDashboard()/showPanel()
// JS — not separate routes, matching the old single-page structure.
export default function EmployeeDashboard() {
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [error, setError] = useState("");
  const [view, setView] = useState("dashboard");
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [limitError, setLimitError] = useState("");
  const [limitSuccess, setLimitSuccess] = useState("");

  // GET /api/employee/dashboard does NOT include notifications (verified
  // against EmployeeController's actual data.put(...) calls) — fetched
  // as separate calls instead of adding a new field to that endpoint,
  // since only the UI needed changing here, not the backend contract.
  function load() {
    employeeApi
      .getDashboard()
      .then(setData)
      .catch(() => setError("Could not load dashboard."));
    employeeApi.getNotifications().then(setNotifications).catch(() => {});
    employeeApi.getUnreadCount().then(setUnreadCount).catch(() => {});
  }

  useEffect(load, []);

  async function handleLimitSubmit(requestedAmount, reason) {
    setLimitError("");
    setLimitSuccess("");
    try {
      await employeeApi.submitLimitRequest(requestedAmount, reason);
      setShowLimitModal(false);
      setLimitSuccess("Limit request submitted.");
      load();
    } catch (err) {
      setLimitError(err.response?.data?.message || "Could not submit request.");
    }
  }

  async function handleMarkAllRead() {
    await employeeApi.markAllRead();
    load();
  }

  if (error) return <div className="alert-banner alert-err m-4">{error}</div>;
  if (!data) return <div className="p-4">Loading…</div>;

  const remainingBalance = data.remainingBalance;
  const monthlyLimit = data.user.monthlyLimit || 0;

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: view === "dashboard", onClick: () => setView("dashboard") },
    { label: "My Expenses", icon: "📄", active: view === "myExpenses", onClick: () => setView("myExpenses") },
    { label: "Limit Requests", icon: "📋", active: view === "myLimitRequests", onClick: () => setView("myLimitRequests") },
  ];

  return (
    <>
      <Sidebar
        navItems={navItems}
        balance={{ remaining: remainingBalance, limit: monthlyLimit }}
        onLimitRequestClick={() => setShowLimitModal(true)}
      />
      <div className="content">
        <Topbar
          title="Employee Dashboard"
          subtitle={`Welcome back, ${data.user.name}!`}
          notifications={notifications}
          unreadCount={unreadCount}
          onMarkAllRead={handleMarkAllRead}
        />

        {limitSuccess && <div className="alert-banner alert-ok">✅ {limitSuccess}</div>}
        {limitError && <div className="alert-banner alert-warn">⚠️ {limitError}</div>}

        {view === "dashboard" && (
          <div>
            {remainingBalance != null && remainingBalance > 0 && remainingBalance <= monthlyLimit * 0.2 && (
              <div className="alert-banner alert-warn">
                ⚠️ Only <strong>{formatCurrency(remainingBalance)}</strong>&nbsp;remaining — under 20% of your monthly limit.
              </div>
            )}
            {remainingBalance != null && remainingBalance <= 0 && (
              <div className="alert-banner alert-over">
                🚫 <strong>Monthly limit reached.</strong> Request a limit increase to submit new expenses.
              </div>
            )}

            <div className="row g-3 mb-4">
              <StatBlock icon="💰" cls="c1" label="This Month" value={formatCurrency(data.monthlyTotal)} />
              <StatBlock icon="⏳" cls="c2" label="Pending" value={data.pendingCount} />
              <StatBlock icon="✅" cls="c3" label="Approved" value={formatCurrency(data.approvedAmount)} />
              <StatBlock icon="🏦" cls="c4" label="Reimbursed" value={formatCurrency(data.reimbursedAmount)} />
            </div>

            <div className="charts-grid">
              <ChartCard title="Spend by Category" chartData={data.categoryChart} type="doughnut" />
              <ChartCard title="Monthly Expense Trend" chartData={data.monthlyChart} type="bar" />
            </div>
          </div>
        )}

        {view === "myExpenses" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">My Expenses</div>
              <button className="btn-primary-custom" onClick={() => navigate("/employee/submit-expense")}>
                ＋ Submit Expense
              </button>
            </div>
            <div className="panel-body">
              <ExpenseTable expenses={data.expenses} />
            </div>
          </div>
        )}

        {view === "myLimitRequests" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Limit Requests</div>
              <button className="btn-primary-custom" onClick={() => setShowLimitModal(true)}>
                ＋ New Request
              </button>
            </div>
            <div className="panel-body">
              {data.limitRequests.length === 0 ? (
                <div className="no-results">No limit requests yet.</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>Requested</th>
                      <th>Reason</th>
                      <th>Status</th>
                      <th>Approved Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.limitRequests.map((r) => (
                      <tr key={r.id}>
                        <td>{formatCurrency(r.requestedAmount)}</td>
                        <td>{r.reason}</td>
                        <td>
                          <span className={`status-pill ${statusPillClass(r.status)}`}>{r.status}</span>
                          {r.rejectReason && <div className="small text-danger mt-1">{r.rejectReason}</div>}
                        </td>
                        <td>{r.approvedAmount != null ? formatCurrency(r.approvedAmount) : "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}
      </div>

      <LimitRequestModal
        show={showLimitModal}
        onCancel={() => setShowLimitModal(false)}
        onSubmit={handleLimitSubmit}
      />
    </>
  );
}

function StatBlock({ icon, cls, label, value }) {
  return (
    <div className="col-md-3">
      <div className="stat-card">
        <div className={`stat-icon ${cls}`}>{icon}</div>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
      </div>
    </div>
  );
}
