import { useState } from "react";

export default function RejectModal({ show, onCancel, onConfirm }) {
  const [reason, setReason] = useState("");

  if (!show) return null;

  return (
    <div className="modal d-block" style={{ background: "rgba(0,0,0,0.5)" }} onClick={onCancel}>
      <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Reason for rejection</h5>
            <button type="button" className="btn-close" onClick={onCancel} />
          </div>
          <div className="modal-body">
            <textarea
              className="form-control"
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Explain why this is being rejected…"
              autoFocus
            />
          </div>
          <div className="modal-footer">
            <button className="btn btn-secondary" onClick={onCancel}>
              Cancel
            </button>
            <button
              className="btn btn-danger"
              disabled={!reason.trim()}
              onClick={() => {
                onConfirm(reason.trim());
                setReason("");
              }}
            >
              Reject
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
