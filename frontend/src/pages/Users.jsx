import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import financeApi from "../services/financeApi";
import { formatCurrency } from "../utils/formatCurrency";

// Ported from user.html. role stays EMPLOYEE/MANAGER only in the UI —
// the old form's radios never offered FINANCE either.
const ROLE_OPTIONS = ["EMPLOYEE", "MANAGER"];

const emptyForm = {
  employeeCode: "", name: "", email: "", password: "", phone: "",
  role: "EMPLOYEE", departmentId: "", monthlyLimit: "",
};

export default function Users() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  function load() {
    financeApi.getUsers().then(setUsers).catch(() => setError("Could not load users."));
    financeApi.getDepartments().then(setDepartments).catch(() => {});
  }
  useEffect(load, []);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      await financeApi.createUser({
        employeeCode: form.employeeCode,
        name: form.name,
        email: form.email,
        password: form.password,
        phone: form.phone,
        role: form.role,
        departmentId: form.departmentId === "" ? null : Number(form.departmentId),
        monthlyLimit: form.monthlyLimit === "" ? 0 : Number(form.monthlyLimit),
      });
      setForm(emptyForm);
      load();
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors);
      setError(data?.message || "Could not create user.");
    } finally {
      setSubmitting(false);
    }
  }

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: false, onClick: () => navigate("/finance") },
    { label: "Add User", icon: "👤", active: true, onClick: () => {} },
  ];

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="Users" subtitle="Create and manage user accounts" />

        <div className="table-panel active">
          <div className="panel-header">
            <div className="panel-title">Add User</div>
          </div>
          <div className="panel-body">
            {error && <div className="alert-msg alert-err">{error}</div>}
            <form onSubmit={handleCreate} className="row g-2">
              <div className="col-sm-4">
                <label className="field-label">Employee Code</label>
                <input className="field-control" value={form.employeeCode} onChange={(e) => update("employeeCode", e.target.value)} required />
                {fieldErrors.employeeCode && <div className="small text-danger mt-1">{fieldErrors.employeeCode}</div>}
              </div>
              <div className="col-sm-4">
                <label className="field-label">Full Name</label>
                <input className="field-control" value={form.name} onChange={(e) => update("name", e.target.value)} required />
                {fieldErrors.name && <div className="small text-danger mt-1">{fieldErrors.name}</div>}
              </div>
              <div className="col-sm-4">
                <label className="field-label">Email</label>
                <input type="email" className="field-control" value={form.email} onChange={(e) => update("email", e.target.value)} required />
                {fieldErrors.email && <div className="small text-danger mt-1">{fieldErrors.email}</div>}
              </div>

              <div className="col-sm-4">
                <label className="field-label">Temporary Password</label>
                <input type="password" className="field-control" value={form.password} onChange={(e) => update("password", e.target.value)} required />
                {fieldErrors.password && <div className="small text-danger mt-1">{fieldErrors.password}</div>}
              </div>
              <div className="col-sm-4">
                <label className="field-label">Phone</label>
                <input className="field-control" value={form.phone} onChange={(e) => update("phone", e.target.value)} />
              </div>
              <div className="col-sm-4">
                <label className="field-label">Role</label>
                <select className="field-control" value={form.role} onChange={(e) => update("role", e.target.value)}>
                  {ROLE_OPTIONS.map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>

              <div className="col-sm-4">
                <label className="field-label">Department</label>
                <select className="field-control" value={form.departmentId} onChange={(e) => update("departmentId", e.target.value)}>
                  <option value="">No department</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                  ))}
                </select>
              </div>
              <div className="col-sm-4">
                <label className="field-label">Monthly Limit</label>
                <input type="number" min="0" step="0.01" className="field-control" value={form.monthlyLimit} onChange={(e) => update("monthlyLimit", e.target.value)} />
                {fieldErrors.monthlyLimit && <div className="small text-danger mt-1">{fieldErrors.monthlyLimit}</div>}
              </div>
              <div className="col-sm-4 d-flex align-items-end">
                <button type="submit" className="btn-primary-custom w-100" disabled={submitting}>
                  {submitting ? "Creating…" : "Add User"}
                </button>
              </div>
            </form>
          </div>
        </div>

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
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td><span className="status-pill pill-approved">{u.role}</span></td>
                    <td>{u.departmentName || "—"}</td>
                    <td>{formatCurrency(u.monthlyLimit)}</td>
                  </tr>
                ))}
                {users.length === 0 && (
                  <tr>
                    <td colSpan={5} className="no-results">No users yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  );
}
