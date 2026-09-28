import { useEffect, useState } from "react";

/**
 * Ported from the old .topbar structure. The notification bell
 * (employee-only in the old app — see NotificationController's
 * Javadoc from Step 6) is optional via the `notifications` prop.
 */
export default function Topbar({ title, subtitle, notifications, unreadCount, onMarkAllRead }) {
  const [clock, setClock] = useState("");
  const [notifOpen, setNotifOpen] = useState(false);

  useEffect(() => {
    function tick() {
      setClock(
        new Date().toLocaleString("en-IN", {
          weekday: "short",
          day: "numeric",
          month: "short",
          hour: "2-digit",
          minute: "2-digit",
        })
      );
    }
    tick();
    const id = setInterval(tick, 30000);
    return () => clearInterval(id);
  }, []);

  return (
    <div className="topbar">
      <div>
        <div className="page-title">{title}</div>
        {subtitle && <div className="page-subtitle">{subtitle}</div>}
      </div>
      <div className="topbar-right">
        {notifications && (
          <div className="notif-wrapper">
            <button className="btn-bell" onClick={() => setNotifOpen((o) => !o)}>
              🔔
              {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
            </button>
            <div className={`notif-dropdown ${notifOpen ? "open" : ""}`}>
              <div className="notif-header">
                <span className="notif-header-title">🔔 Notifications</span>
                {notifications.length > 0 && (
                  <button className="notif-mark-read" onClick={onMarkAllRead}>
                    Mark all read
                  </button>
                )}
              </div>
              <div className="notif-list">
                {notifications.length === 0 && (
                  <div className="notif-empty">No notifications yet.</div>
                )}
                {notifications.map((n) => (
                  <div key={n.id} className={`notif-item ${!n.isRead ? "unread" : ""}`}>
                    <div className={`notif-dot dot-${n.type}`} />
                    <div>
                      <div className="notif-text">{n.message}</div>
                      <div className="notif-time">
                        {n.createdAt ? new Date(n.createdAt).toLocaleDateString() : ""}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              {notifications.length > 0 && (
                <div className="notif-footer">
                  <a onClick={() => setNotifOpen(false)}>Close</a>
                </div>
              )}
            </div>
          </div>
        )}
        <div className="topbar-time">{clock}</div>
      </div>
    </div>
  );
}
