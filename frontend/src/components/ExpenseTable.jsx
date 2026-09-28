import { formatCurrency, formatDate, statusPillClass } from "../utils/formatCurrency";

/**
 * Renders ExpenseResponse rows inside the old .table markup (thead/
 * tbody styling comes from index.css, ported from the Thymeleaf
 * templates). `actions(expense)` is an optional render-prop so the
 * same table serves Employee/Manager/Finance with different buttons.
 */
export default function ExpenseTable({ expenses, showEmployee = false, actions }) {
  if (!expenses || expenses.length === 0) {
    return <div className="no-results">No expenses to show.</div>;
  }

  return (
    <div className="table-responsive">
      <table className="table">
        <thead>
          <tr>
            <th>Date</th>
            {showEmployee && <th>Employee</th>}
            <th>Category</th>
            <th>Amount</th>
            <th>Description</th>
            <th>Status</th>
            {actions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {expenses.map((e) => (
            <tr key={e.id}>
              <td>{formatDate(e.expenseDate)}</td>
              {showEmployee && <td>{e.employeeName}</td>}
              <td>{e.categoryName}</td>
              <td>{formatCurrency(e.amount)}</td>
              <td className="text-truncate" style={{ maxWidth: 220 }}>
                {e.description}
              </td>
              <td>
                <span className={`status-pill ${statusPillClass(e.status)}`}>{e.status}</span>
                {e.rejectReason && (
                  <div className="small text-danger mt-1">Reason: {e.rejectReason}</div>
                )}
              </td>
              {actions && <td>{actions(e)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
