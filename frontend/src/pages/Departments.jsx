import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import financeApi from "../services/financeApi";

// Ported from department.html. No manager picker — the old form never
// had one; a department's manager is set automatically when a MANAGER
// user is created for it (UserServiceImpl.registerUser).
export default function Departments() {
  const navigate = useNavigate();
  const [departments, setDepartments] = useState([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  function load() {
    financeApi.getDepartments().then(setDepartments).catch(() => setError("Could not load departments."));
  }
  useEffect(load, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    try {
      await financeApi.createDepartment(name);
      setName("");
      load();
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors);
      setError(data?.message || "Could not create department.");
    }
  }

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: false, onClick: () => navigate("/finance") },
    { label: "Add Department", icon: "🏢", active: true, onClick: () => {} },
  ];

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="Departments" subtitle="Manage company departments" />

        <div className="table-panel active" style={{ maxWidth: 640 }}>
          <div className="panel-header">
            <div className="panel-title">Add Department</div>
          </div>
          <div className="panel-body">
            {error && <div className="alert-msg alert-err">{error}</div>}
            <form onSubmit={handleCreate} className="row g-2">
              <div className="col-sm-9">
                <label className="field-label">Department Name</label>
                <input
                  className="field-control"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
                {fieldErrors.name && <div className="small text-danger mt-1">{fieldErrors.name}</div>}
              </div>
              <div className="col-sm-3 d-flex align-items-end">
                <button type="submit" className="btn-primary-custom w-100">
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>

        <div className="table-panel active">
          <div className="panel-header">
            <div className="panel-title">All Departments</div>
          </div>
          <div className="panel-body">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Manager</th>
                </tr>
              </thead>
              <tbody>
                {departments.map((d) => (
                  <tr key={d.id}>
                    <td>{d.name}</td>
                    <td>{d.managerName || <span className="text-muted">Unassigned</span>}</td>
                  </tr>
                ))}
                {departments.length === 0 && (
                  <tr>
                    <td colSpan={2} className="no-results">No departments yet.</td>
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
