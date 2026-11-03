// Admin reports: sales summary + feedback summary, with simple on-brand bar
// charts. Everything here is read-only (GET) from the admin report/sales
// endpoints. A period selector (week/month/quarter/year) re-fetches the
// time-bound numbers.

import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import { categoryLabel, money } from '../../lib/catalog'

const PERIODS = ['WEEK', 'MONTH', 'QUARTER', 'YEAR']
// A small categorical palette drawn from the site's own tokens, so the charts
// feel part of the shop rather than a generic dashboard.
const PALETTE = ['#c9683f', '#7f8f5a', '#c69a53', '#b06678', '#6a6480']

// One horizontal bar row. `value` sets the fill width relative to `max`.
function Bar({ label, value, max, display, color }) {
  const pct = max > 0 ? Math.max(2, Math.round((value / max) * 100)) : 0
  return (
    <div className="bar-row">
      <span className="bar-label" title={label}>{label}</span>
      <span className="bar-track">
        <span className="bar-fill" style={{ width: `${pct}%`, background: color }} />
      </span>
      <span className="bar-value">{display}</span>
    </div>
  )
}

export default function ReportsPanel() {
  const [period, setPeriod] = useState('MONTH')
  const [sales, setSales] = useState(null) // SalesReportResponse
  const [movement, setMovement] = useState([]) // ProductMovementResponse[]
  const [feedback, setFeedback] = useState(null) // FeedbackReportResponse
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    Promise.all([
      api.get(`/api/admin/reports/sales?period=${period}`),
      api.get(`/api/admin/sales/movement?period=${period}`),
      api.get('/api/admin/reports/feedback'), // feedback report is not period-bound
    ])
      .then(([s, m, f]) => {
        if (!active) return
        setSales(s)
        setMovement(m)
        setFeedback(f)
      })
      .catch((e) => active && setError(e.message))
      .finally(() => active && setLoading(false))
    return () => { active = false }
  }, [period])

  if (loading) {
    return <div className="state"><div className="spinner" />Loading reports…</div>
  }
  if (error) {
    return <div className="alert alert-error">{error}</div>
  }

  const byCategory = sales?.byCategory ?? []
  const maxCatRevenue = Math.max(0, ...byCategory.map((c) => Number(c.totalRevenue)))
  const topMovers = movement.slice(0, 8)
  const maxMoved = Math.max(0, ...topMovers.map((m) => Number(m.totalQuantitySold)))
  const ratingCounts = feedback?.ratingCounts ?? {}
  const maxRating = Math.max(0, ...[1, 2, 3, 4, 5].map((r) => Number(ratingCounts[r] ?? 0)))

  return (
    <div>
      <div className="panel-head">
        <h2 style={{ fontSize: 26 }}>Reports</h2>
        <div className="chips">
          {PERIODS.map((p) => (
            <button key={p} className={`chip ${period === p ? 'on' : ''}`} onClick={() => setPeriod(p)}>
              {p.charAt(0) + p.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {/* Headline numbers */}
      <div className="stat-row">
        <div className="stat-tile">
          <span className="stat-k">Revenue</span>
          <span className="stat-v">{money(sales?.totalRevenue ?? 0)}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Items sold</span>
          <span className="stat-v">{sales?.totalItemsSold ?? 0}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Paid orders</span>
          <span className="stat-v">{sales?.totalPaidOrders ?? 0}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Avg rating</span>
          <span className="stat-v">
            {feedback?.averageRating != null ? `${feedback.averageRating.toFixed(1)} ★` : '—'}
          </span>
        </div>
      </div>

      <div className="report-grid">
        {/* Revenue by category */}
        <div className="report-card">
          <h3 style={{ fontSize: 18, marginBottom: 14 }}>Revenue by category</h3>
          {byCategory.length === 0 ? (
            <p className="muted" style={{ fontSize: 14 }}>No sales in this period.</p>
          ) : (
            byCategory.map((c, i) => (
              <Bar key={c.label} label={categoryLabel(c.label)} value={Number(c.totalRevenue)}
                max={maxCatRevenue} display={money(c.totalRevenue)} color={PALETTE[i % PALETTE.length]} />
            ))
          )}
        </div>

        {/* Top sellers by units */}
        <div className="report-card">
          <h3 style={{ fontSize: 18, marginBottom: 14 }}>Top sellers (units)</h3>
          {topMovers.length === 0 ? (
            <p className="muted" style={{ fontSize: 14 }}>No sales in this period.</p>
          ) : (
            topMovers.map((m, i) => (
              <Bar key={m.productId} label={m.productName} value={Number(m.totalQuantitySold)}
                max={maxMoved} display={String(m.totalQuantitySold)} color={PALETTE[i % PALETTE.length]} />
            ))
          )}
        </div>

        {/* Feedback rating distribution */}
        <div className="report-card">
          <h3 style={{ fontSize: 18, marginBottom: 14 }}>
            Ratings <span className="muted" style={{ fontSize: 14, fontWeight: 600 }}>({feedback?.totalCount ?? 0} total)</span>
          </h3>
          {[5, 4, 3, 2, 1].map((r) => (
            <Bar key={r} label={`${r} ★`} value={Number(ratingCounts[r] ?? 0)} max={maxRating}
              display={String(ratingCounts[r] ?? 0)} color="var(--sage)" />
          ))}
        </div>

        {/* Recent feedback messages */}
        <div className="report-card">
          <h3 style={{ fontSize: 18, marginBottom: 14 }}>Recent feedback</h3>
          {(feedback?.items ?? []).length === 0 ? (
            <p className="muted" style={{ fontSize: 14 }}>No feedback yet.</p>
          ) : (
            <div className="fb-list">
              {feedback.items.slice(0, 6).map((f) => (
                <div className="fb-item" key={f.id}>
                  <div className="fb-top">
                    <span className="fb-stars">{'★'.repeat(f.rating)}{'☆'.repeat(5 - f.rating)}</span>
                    <span className="muted" style={{ fontSize: 12.5 }}>{f.userEmail}</span>
                  </div>
                  {f.message && <p style={{ fontSize: 14, marginTop: 4 }}>{f.message}</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
