// Ported from the old .stat-card/.stat-icon/.stat-label/.stat-value
// markup (identical across all three dashboards).
export default function StatCard({ label, value, icon, colorClass = "c1" }) {
  return (
    <div className="col-md-3">
      <div className="stat-card">
        <div className={`stat-icon ${colorClass}`}>{icon}</div>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value}</div>
      </div>
    </div>
  );
}
