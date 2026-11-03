// Live notifications feed. Opens the admin WebSocket (via useAdminSocket) and
// shows a running list of events as the backend pushes them:
//   • new paid order        → /topic/admin/orders   (OrderResponse)
//   • low stock crossed      → /topic/admin/low-stock (LowStockEvent)
//   • new feedback           → /topic/admin/feedback  (FeedbackResponse)
// Nothing is polled — the server pushes, we just render what arrives.

import { useCallback, useRef, useState } from 'react'
import { useAdminSocket } from '../../lib/useAdminSocket'
import { money } from '../../lib/catalog'

let seq = 0 // stable unique key for each event row

const STATUS = {
  connecting: { label: 'Connecting…', cls: 'dot-wait' },
  connected: { label: 'Live', cls: 'dot-ok' },
  closed: { label: 'Disconnected — retrying…', cls: 'dot-wait' },
  error: { label: 'Connection error', cls: 'dot-err' },
}

function time() {
  return new Date().toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

export default function LiveFeed() {
  const [events, setEvents] = useState([])

  const push = useCallback((event) => {
    setEvents((list) => [{ ...event, id: ++seq, at: time() }, ...list].slice(0, 50))
  }, [])

  // Keep the handler object identity stable so the socket isn't re-created.
  const handlers = useRef({
    onOrder: (o) =>
      push({ kind: 'order', icon: '🧾', title: `New order #${o.id}`,
             detail: `${money(o.totalAmount)} · ${o.items?.length ?? 0} item(s)` }),
    onLowStock: (s) =>
      push({ kind: 'low', icon: '⚠️', title: `Low stock: ${s.name}`,
             detail: `only ${s.qoh} left (id ${s.productId})` }),
    onFeedback: (f) =>
      push({ kind: 'fb', icon: '💬', title: `New feedback · ${f.rating}★`,
             detail: f.message ? `"${f.message}"` : f.userEmail }),
  }).current

  const { status } = useAdminSocket(handlers)
  const s = STATUS[status] ?? STATUS.connecting

  return (
    <div>
      <div className="panel-head">
        <div>
          <h2 style={{ fontSize: 26 }}>Live activity</h2>
          <p className="muted" style={{ fontSize: 14 }}>Orders, low stock and feedback as they happen.</p>
        </div>
        <span className={`live-dot ${s.cls}`}>{s.label}</span>
      </div>

      {status === 'error' && (
        <div className="alert alert-error">
          Couldn’t open the live connection. Make sure you’re logged in as an admin and the backend is running.
        </div>
      )}

      {events.length === 0 ? (
        <div className="state">
          <div className="spinner" />
          Waiting for activity… place a paid order or submit feedback to see it appear here instantly.
        </div>
      ) : (
        <div className="feed">
          {events.map((e) => (
            <div className={`feed-item feed-${e.kind}`} key={e.id}>
              <span className="feed-icon">{e.icon}</span>
              <div className="feed-main">
                <span className="feed-title">{e.title}</span>
                <span className="feed-detail muted">{e.detail}</span>
              </div>
              <span className="feed-time muted">{e.at}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
