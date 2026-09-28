import { useState } from "react";

// Ported from the old #limitRequestModal (triggered by the sidebar's
// "＋ Request Limit Increase" button in employee_dashboard.html).
export default function LimitRequestModal({ show, onCancel, onSubmit }) {
  const [amount, setAmount] = useState("");
  const [reason, setReason] = useState("");

  if (!show) return null;

  return (
    <div className="modal d-block" style={{ background: "rgba(0,0,0,0.5)" }} onClick={onCancel}>
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Request Limit Increase</h5>
            <button type="button" className="btn-close" onClick={onCancel} />
          </div>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              onSubmit(Number(amount), reason);
              setAmount("");
              setReason("");
            }}
          >
            <div className="modal-body">
              <div className="mb-3">
                <label className="form-label">Additional Amount</label>
                <input
                  type="number"
                  min="1"
                  step="0.01"
                  className="form-control"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  required
                  autoFocus
                />
              </div>
              <div className="mb-2">
                <label className="form-label">Reason</label>
                <textarea
                  className="form-control"
                  rows={3}
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  required
                />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onCancel}>
                Cancel
              </button>
              <button type="submit" className="btn-primary-custom">
                Submit Request
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
