import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import employeeApi from "../services/employeeApi";

// Ported from submit_expense.html: same mini sidebar (Dashboard / ➕
// Submit Expense, this one active) and .form-card/.field-control markup.
export default function SubmitExpense() {
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [expenseDate, setExpenseDate] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [billFile, setBillFile] = useState(null);

  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    employeeApi.getCategories().then(setCategories).catch(() => setError("Could not load categories."));
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldErrors({});
    setSubmitting(true);
    try {
      await employeeApi.submitExpense(
        { expenseDate, categoryId: Number(categoryId), amount: Number(amount), description },
        billFile
      );
      navigate("/employee");
    } catch (err) {
      const data = err.response?.data;
      if (data?.fieldErrors) setFieldErrors(data.fieldErrors);
      setError(data?.message || "Could not submit expense.");
    } finally {
      setSubmitting(false);
    }
  }

  const navItems = [
    { section: "Navigation" },
    { label: "Dashboard", icon: "🏠", active: false, onClick: () => navigate("/employee") },
    { label: "Submit Expense", icon: "➕", active: true, onClick: () => {} },
  ];

  return (
    <>
      <Sidebar navItems={navItems} />
      <div className="content">
        <Topbar title="Submit Expense" subtitle="Fill in the details below to submit a new expense" />

        <div className="form-card">
          <div className="form-card-header">
            <h4>New Expense</h4>
            <p>All fields except the bill are required</p>
          </div>
          <div className="form-card-body">
            {error && <div className="alert-msg alert-err">{error}</div>}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="field-label">Expense Date</label>
                <input
                  type="date"
                  className="field-control"
                  value={expenseDate}
                  onChange={(e) => setExpenseDate(e.target.value)}
                  required
                />
                {fieldErrors.expenseDate && <div className="small text-danger mt-1">{fieldErrors.expenseDate}</div>}
              </div>

              <div className="mb-3">
                <label className="field-label">Category</label>
                <select
                  className="field-control"
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  required
                >
                  <option value="">Select a category…</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
                {fieldErrors.categoryId && <div className="small text-danger mt-1">{fieldErrors.categoryId}</div>}
              </div>

              <div className="mb-3">
                <label className="field-label">Amount</label>
                <div className="amount-wrapper">
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="field-control"
                    value={amount}
                    onChange={(e) => setAmount(e.target.value)}
                    required
                  />
                </div>
                {fieldErrors.amount && <div className="small text-danger mt-1">{fieldErrors.amount}</div>}
              </div>

              <div className="mb-3">
                <label className="field-label">Description</label>
                <textarea
                  className="field-control"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>

              <div className="mb-4">
                <label className="field-label">Bill (optional)</label>
                <label className="upload-box d-block">
                  <input
                    type="file"
                    className="d-none"
                    accept=".jpg,.jpeg,.png,.pdf"
                    onChange={(e) => setBillFile(e.target.files[0] || null)}
                  />
                  {billFile ? billFile.name : "📎 Click to upload JPEG, PNG, or PDF — max 5MB"}
                </label>
              </div>

              <button type="submit" className="btn-primary-custom w-100" disabled={submitting}>
                {submitting ? "Submitting…" : "Submit Expense"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}
