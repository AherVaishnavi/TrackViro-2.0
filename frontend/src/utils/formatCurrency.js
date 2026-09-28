// Backend amounts are plain Double (₹ is used in ExpenseServiceImpl's
// notification text), so this formats consistently with that.
export function formatCurrency(value) {
  const n = value == null ? 0 : value;
  return "₹" + n.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export function formatDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("en-IN", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

// Small, readable badge color per Expense/LimitRequest status string —
// matches the exact status values the backend actually uses (verified
// against ExpenseServiceImpl/LimitRequestServiceImpl, not guessed):
// PENDING, MANAGER_APPROVED, FINANCE_APPROVED, REJECTED, REIMBURSED,
// APPROVED. Class names match the old app's .pill-* classes exactly
// (see index.css, ported from the Thymeleaf templates' inline CSS).
export function statusPillClass(status) {
  switch (status) {
    case "PENDING":
      return "pill-pending";
    case "MANAGER_APPROVED":
      return "pill-mapproved";
    case "FINANCE_APPROVED":
      return "pill-fapproved";
    case "APPROVED":
      return "pill-approved";
    case "REIMBURSED":
      return "pill-reimbursed";
    case "REJECTED":
      return "pill-rejected";
    default:
      return "pill-pending";
  }
}
