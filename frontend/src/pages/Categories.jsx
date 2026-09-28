import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import financeApi from "../services/financeApi";
import { formatCurrency } from "../utils/formatCurrency";

// Ported from category.html — same sidebar, same panel-body/field-control
// styling as the rest of the finance area. GET/POST /api/finance/categories.
export default function Categories() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState("");
  const [maxLimit, setMaxLimit] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});

  function load() {
    financeApi.getCategories().then(setCategories).catch(() => setError("Could not load categories."));
  }
  useEffect(load, []);

  async function handleCreate(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    try {
      await financeApi.createCategory(name, maxLimit === "" ? 0 : Number(maxLimit));
      setName("");
      setMaxLimit("");
      load();
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors);
      setError(data?.message || "Could not create category.");
    }
  }

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: false, onClick: () => navigate("/finance") },
    { label: "Add Category", icon: "🏷️", active: true, onClick: () => {} },
  ];

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="Categories" subtitle="Manage expense categories" />

        <div className="table-panel active" style={{ maxWidth: 640 }}>
          <div className="panel-header">
            <div className="panel-title">Add Category</div>
          </div>
          <div className="panel-body">
            {error && <div className="alert-msg alert-err">{error}</div>}
            <form onSubmit={handleCreate} className="row g-2 align-items-start">
              <div className="col-sm-6">
                <label className="field-label">Category Name</label>
                <input
                  className="field-control"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
                {fieldErrors.name && <div className="small text-danger mt-1">{fieldErrors.name}</div>}
              </div>
              <div className="col-sm-4">
                <label className="field-label">Max Limit (optional)</label>
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  className="field-control"
                  value={maxLimit}
                  onChange={(e) => setMaxLimit(e.target.value)}
                />
              </div>
              <div className="col-sm-2 d-flex align-items-end">
                <button type="submit" className="btn-primary-custom w-100">
                  Add
                </button>
              </div>
            </form>
          </div>
        </div>

        <div className="table-panel active">
          <div className="panel-header">
            <div className="panel-title">All Categories</div>
          </div>
          <div className="panel-body">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Max Limit</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((c) => (
                  <tr key={c.id}>
                    <td>{c.name}</td>
                    <td>{c.maxLimit != null ? formatCurrency(c.maxLimit) : "—"}</td>
                  </tr>
                ))}
                {categories.length === 0 && (
                  <tr>
                    <td colSpan={2} className="no-results">No categories yet.</td>
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
