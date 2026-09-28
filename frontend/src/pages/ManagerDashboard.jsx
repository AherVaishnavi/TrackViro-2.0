import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import ExpenseTable from "../components/ExpenseTable";
import ChartCard from "../components/ChartCard";
import RejectModal from "../components/RejectModal";
import managerApi from "../services/managerApi";
import { formatCurrency } from "../utils/formatCurrency";

// Ported from manager_dashboard.html's panel structure: dashboard /
// expenseSection (pending) / limitRequestSection / teamMembers /
// allExpenses — same 5 nav items, same panel toggling behaviour.
export default function ManagerDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [view, setView] = useState("dashboard");
  const [rejectTarget, setRejectTarget] = useState(null);

  function load() {
    managerApi
      .getDashboard()
      .then(setData)
      .catch(() => setError("Could not load dashboard."));
  }

  useEffect(load, []);

  async function handleApprove(id) {
    try {
      await managerApi.approveExpense(id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not approve.");
    }
  }

  async function handleReject(reason) {
    try {
      await managerApi.rejectExpense(rejectTarget, reason);
      setRejectTarget(null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not reject.");
    }
  }

  async function handleLimitApprove(id) {
    try {
      await managerApi.approveLimitRequest(id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not approve.");
    }
  }

  async function handleLimitReject(reason) {
    try {
      await managerApi.rejectLimitRequest(rejectTarget, reason);
      setRejectTarget(null);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not reject.");
    }
  }

  if (error) return <div className="alert-banner alert-err m-4">{error}</div>;
  if (!data) return <div className="p-4">Loading…</div>;

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: view === "dashboard", onClick: () => setView("dashboard") },
    { label: "Team Expenses", icon: "📄", active: view === "expenseSection", onClick: () => setView("expenseSection") },
    {
      label: "Limit Requests",
      icon: "📋",
      active: view === "limitRequestSection",
      onClick: () => setView("limitRequestSection"),
      badge: data.pendingLimitRequests.length,
    },
    { label: "Team Members", icon: "👥", active: view === "teamMembers", onClick: () => setView("teamMembers") },
    { label: "All Team Expenses", icon: "📂", active: view === "allExpenses", onClick: () => setView("allExpenses") },
  ];

  // "reject" mode differs for expenses vs limit requests — track which
  // kind rejectTarget refers to via a small wrapper.
  const isLimitReject = view === "limitRequestSection";

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="Manager Dashboard" subtitle="Review and approve your team's activity" />

        {view === "dashboard" && (
          <div>
            <div className="row g-3 mb-4">
              <StatBlock icon="⏳" cls="c2" label="Pending" value={data.pendingCount} />
              <StatBlock icon="✅" cls="c3" label="Approved" value={data.approvedCount} />
              <StatBlock icon="🚫" cls="c4" label="Rejected" value={data.rejectedCount} />
            </div>
            <div className="charts-grid">
              <ChartCard title="Department Spending by Category" chartData={data.categoryChart} type="doughnut" />
              <ChartCard title="Department Spending by Month" chartData={data.monthlyChart} type="bar" />
            </div>
          </div>
        )}

        {view === "expenseSection" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Team Expenses — Pending</div>
            </div>
            <div className="panel-body">
              <ExpenseTable
                expenses={data.pendingExpenses}
                showEmployee
                actions={(e) => (
                  <div className="d-flex gap-1">
                    <button className="btn btn-sm btn-success" onClick={() => handleApprove(e.id)}>
                      Approve
                    </button>
                    <button className="btn btn-sm btn-danger" onClick={() => setRejectTarget(e.id)}>
                      Reject
                    </button>
                  </div>
                )}
              />
            </div>
          </div>
        )}

        {view === "allExpenses" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">All Team Expenses</div>
            </div>
            <div className="panel-body">
              <ExpenseTable expenses={data.allDeptExpenses} showEmployee />
            </div>
          </div>
        )}

        {view === "limitRequestSection" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Pending Limit Requests</div>
            </div>
            <div className="panel-body">
              {data.pendingLimitRequests.length === 0 ? (
                <div className="no-results">No pending limit requests.</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>Employee</th>
                      <th>Requested</th>
                      <th>Reason</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.pendingLimitRequests.map((r) => (
                      <tr key={r.id}>
                        <td>{r.employeeName}</td>
                        <td>{formatCurrency(r.requestedAmount)}</td>
                        <td>{r.reason}</td>
                        <td>
                          <div className="d-flex gap-1">
                            <button className="btn btn-sm btn-success" onClick={() => handleLimitApprove(r.id)}>
                              Approve
                            </button>
                            <button className="btn btn-sm btn-danger" onClick={() => setRejectTarget(r.id)}>
                              Reject
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {view === "teamMembers" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Team Members</div>
            </div>
            <div className="panel-body">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Monthly Limit</th>
                  </tr>
                </thead>
                <tbody>
                  {data.team.map((u) => (
                    <tr key={u.id}>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{formatCurrency(u.monthlyLimit)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      <RejectModal
        show={rejectTarget !== null}
        onCancel={() => setRejectTarget(null)}
        onConfirm={isLimitReject ? handleLimitReject : handleReject}
      />
    </>
  );
}

function StatBlock({ icon, cls, label, value }) {
  return (
    <div className="col-md-4">
      <div className="stat-card">
        <div className={`stat-icon ${cls}`}>{icon}</div>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
      </div>
    </div>
  );
}
