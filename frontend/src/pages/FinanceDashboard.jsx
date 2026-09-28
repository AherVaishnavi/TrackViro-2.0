import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import ExpenseTable from "../components/ExpenseTable";
import ChartCard from "../components/ChartCard";
import RejectModal from "../components/RejectModal";
import financeApi from "../services/financeApi";
import { formatCurrency } from "../utils/formatCurrency";

// Ported from finance_dashboard.html's panel structure: dashboard /
// finalApproval / reimbursement / limitRequestSection / categorySummary
// / allUsers, plus the "Setup" section's real links to /finance/user,
// /finance/department, /finance/category (separate pages, not panels —
// matches the old app exactly, confirmed by reading its actual nav).
export default function FinanceDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [view, setView] = useState("dashboard");
  const [rejectTarget, setRejectTarget] = useState(null);
  const [approveAmount, setApproveAmount] = useState({});

  function load() {
    financeApi
      .getDashboard()
      .then(setData)
      .catch(() => setError("Could not load dashboard."));
  }

  useEffect(load, []);

  async function handleFinalApprove(id) {
    try {
      await financeApi.finalApprove(id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not approve.");
    }
  }

  async function handleReimburse(id) {
    try {
      await financeApi.reimburse(id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not reimburse.");
    }
  }

  async function handleLimitApprove(id) {
    const amount = Number(approveAmount[id]);
    if (!amount || amount <= 0) {
      alert("Enter an approved amount first.");
      return;
    }
    try {
      await financeApi.approveLimitRequest(id, amount);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not approve.");
    }
  }

  async function handleLimitReject(reason) {
    try {
      await financeApi.rejectLimitRequest(rejectTarget, reason);
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
    { section: "Approvals" },
    { label: "Final Approval", icon: "✅", active: view === "finalApproval", onClick: () => setView("finalApproval") },
    { label: "Reimbursement", icon: "💳", active: view === "reimbursement", onClick: () => setView("reimbursement") },
    {
      label: "Limit Requests",
      icon: "📋",
      active: view === "limitRequestSection",
      onClick: () => setView("limitRequestSection"),
      badge: data.pendingLimitRequests.length,
    },
    { section: "Reports" },
    { label: "Category Summary", icon: "📊", active: view === "categorySummary", onClick: () => setView("categorySummary") },
    { section: "People" },
    { label: "All Users", icon: "👥", active: view === "allUsers", onClick: () => setView("allUsers") },
  ];

  const setupLinks = [
    { label: "Add User", icon: "👤", to: "/finance/users" },
    { label: "Add Department", icon: "🏢", to: "/finance/departments" },
    { label: "Add Category", icon: "🏷️", to: "/finance/categories" },
  ];

  return (
    <>
      <Sidebar navItems={navItems} setupLinks={setupLinks} />
      <div className="content">
        <Topbar title="Finance Dashboard" subtitle="Final approvals, reimbursement, and reporting" />

        {view === "dashboard" && (
          <div>
            <div className="row g-3 mb-4">
              <StatBlock icon="⏳" cls="c2" label="Pending" value={data.pendingCount} />
              <StatBlock icon="🔍" cls="c1" label="Mgr Approved" value={data.managerApprovedCount} />
              <StatBlock icon="✅" cls="c3" label="Finance Approved" value={data.financeApprovedCount} />
              <StatBlock icon="🏦" cls="c4" label="Reimbursed" value={data.reimbursedCount} />
            </div>
            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <div className="stat-card">
                  <div className="stat-icon c1">📆</div>
                  <div className="stat-label">Approved This Month</div>
                  <div className="stat-value">{formatCurrency(data.monthlyApproved)}</div>
                </div>
              </div>
              <div className="col-md-6">
                <div className="stat-card">
                  <div className="stat-icon c3">💵</div>
                  <div className="stat-label">Total Reimbursed</div>
                  <div className="stat-value">{formatCurrency(data.totalReimbursed)}</div>
                </div>
              </div>
            </div>
            <div className="charts-row1">
              <ChartCard title="By Category" chartData={data.categoryChart} type="doughnut" />
              <ChartCard title="Monthly Trend" chartData={data.monthlyTrendChart} type="bar" large />
            </div>
            <div className="charts-row2">
              <ChartCard title="Top Spenders" chartData={data.topSpendersChart} type="bar" large />
              <ChartCard title="By Department" chartData={data.departmentChart} type="doughnut" />
            </div>
          </div>
        )}

        {view === "finalApproval" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Awaiting Final Approval</div>
            </div>
            <div className="panel-body">
              <ExpenseTable
                expenses={data.managerApprovedExpenses}
                showEmployee
                actions={(e) => (
                  <button className="btn btn-sm btn-primary" onClick={() => handleFinalApprove(e.id)}>
                    Final Approve
                  </button>
                )}
              />
            </div>
          </div>
        )}

        {view === "reimbursement" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Awaiting Reimbursement</div>
            </div>
            <div className="panel-body">
              <ExpenseTable
                expenses={data.finalApprovedExpenses}
                showEmployee
                actions={(e) => (
                  <button className="btn btn-sm btn-success" onClick={() => handleReimburse(e.id)}>
                    Reimburse
                  </button>
                )}
              />
            </div>
          </div>
        )}

        {view === "limitRequestSection" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Limit Requests — Manager Approved</div>
            </div>
            <div className="panel-body">
              {data.pendingLimitRequests.length === 0 ? (
                <div className="no-results">None right now.</div>
              ) : (
                <table className="table">
                  <thead>
                    <tr>
                      <th>Employee</th>
                      <th>Requested</th>
                      <th>Reason</th>
                      <th>Approved Amount</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.pendingLimitRequests.map((r) => (
                      <tr key={r.id}>
                        <td>{r.employeeName}</td>
                        <td>{formatCurrency(r.requestedAmount)}</td>
                        <td>{r.reason}</td>
                        <td style={{ maxWidth: 150 }}>
                          <input
                            type="number"
                            className="form-control form-control-sm"
                            placeholder="₹ amount"
                            value={approveAmount[r.id] || ""}
                            onChange={(e) => setApproveAmount({ ...approveAmount, [r.id]: e.target.value })}
                          />
                        </td>
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

        {view === "categorySummary" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">Category Summary</div>
            </div>
            <div className="panel-body">
              <table className="table">
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Total Finance-Approved</th>
                  </tr>
                </thead>
                <tbody>
                  {data.categoryChart.labels.map((label, i) => (
                    <tr key={label}>
                      <td>{label}</td>
                      <td>{formatCurrency(data.categoryChart.data[i])}</td>
                    </tr>
                  ))}
                  {data.categoryChart.labels.length === 0 && (
                    <tr>
                      <td colSpan={2} className="no-results">No data yet.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {view === "allUsers" && (
          <div className="table-panel active">
            <div className="panel-header">
              <div className="panel-title">All Users</div>
            </div>
            <div className="panel-body">
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Department</th>
                    <th>Monthly Limit</th>
                  </tr>
                </thead>
                <tbody>
                  {data.allUsers.map((u) => (
                    <tr key={u.id}>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td><span className="status-pill pill-approved">{u.role}</span></td>
                      <td>{u.departmentName || "—"}</td>
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
        onConfirm={handleLimitReject}
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
