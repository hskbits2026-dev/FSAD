// Admin reports: sales summary + customer feedback, with on-brand bar charts.
// Everything here is read-only (GET) from the admin report/sales endpoints.
// A period selector (week/month/quarter/year) re-fetches the time-bound numbers.

import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import { categoryLabel, money } from '../../lib/catalog'

const PERIODS = ['WEEK', 'MONTH', 'QUARTER', 'YEAR']
// A small categorical palette drawn from the site's own tokens, so the charts
// feel part of the shop rather than a generic dashboard.
const PALETTE = ['#c9683f', '#7f8f5a', '#c69a53', '#b06678', '#6a6480']

// Short, friendly relative-ish date (e.g. "2 Nov 2026").
function shortDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

// One horizontal bar row. `value` sets the fill width relative to `max`; `display`
// is the primary number and `sub` an optional muted secondary (e.g. a % or units).
function Bar({ label, value, max, display, sub, color }) {
  const pct = max > 0 ? Math.max(2, Math.round((value / max) * 100)) : 0
  return (
    <div className="bar-row">
      <span className="bar-label" title={label}>{label}</span>
      <span className="bar-track">
        <span className="bar-fill" style={{ width: `${pct}%`, background: color }} />
      </span>
      <span className="bar-value">
        {display}{sub != null && <span className="bar-sub">{sub}</span>}
      </span>
    </div>
  )
}

// One feedback/review card used in both the store and product lists.
function FeedbackItem({ f }) {
  return (
    <div className="fb-item">
      <div className="fb-top">
        <span className="fb-stars">{'★'.repeat(f.rating)}{'☆'.repeat(5 - f.rating)}</span>
        <span className="muted" style={{ fontSize: 12.5 }}>{f.userName || f.userEmail}</span>
      </div>
      {f.productName && <span className="fb-product">on “{f.productName}”</span>}
      {f.message && <p style={{ fontSize: 14, marginTop: 4 }}>{f.message}</p>}
      <span className="fb-date">{shortDate(f.createdAt)}</span>
    </div>
  )
}

export default function ReportsPanel() {
  const [period, setPeriod] = useState('MONTH')
  const [topBy, setTopBy] = useState('units') // 'units' | 'revenue' — Top Sellers ranking
  const [sales, setSales] = useState(null) // SalesReportResponse
  const [byProduct, setByProduct] = useState([]) // SalesLineResponse[] (per-product: qty + revenue)
  const [feedback, setFeedback] = useState(null) // FeedbackReportResponse
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    Promise.all([
      api.get(`/api/admin/reports/sales?period=${period}`),
      api.get(`/api/admin/sales?period=${period}&groupBy=PRODUCT`), // has BOTH units + revenue
      api.get('/api/admin/reports/feedback'), // feedback report is not period-bound
    ])
      .then(([s, p, f]) => {
        if (!active) return
        setSales(s)
        setByProduct(p)
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

  // ---- Revenue by category (+ share %) ----
  const byCategory = sales?.byCategory ?? []
  const totalCatRevenue = byCategory.reduce((sum, c) => sum + Number(c.totalRevenue), 0)
  const maxCatRevenue = Math.max(0, ...byCategory.map((c) => Number(c.totalRevenue)))

  // ---- Top sellers (toggle units ⇄ revenue) ----
  const metric = (p) => (topBy === 'units' ? Number(p.totalQuantity) : Number(p.totalRevenue))
  const topMovers = [...byProduct].sort((a, b) => metric(b) - metric(a)).slice(0, 8)
  const maxMoved = Math.max(0, ...topMovers.map(metric))

  // ---- Feedback summary (overall) + split lists ----
  const ratingCounts = feedback?.ratingCounts ?? {}
  const maxRating = Math.max(0, ...[1, 2, 3, 4, 5].map((r) => Number(ratingCounts[r] ?? 0)))
  const totalFb = feedback?.totalCount ?? 0
  const positive = Number(ratingCounts[4] ?? 0) + Number(ratingCounts[5] ?? 0)
  const positivePct = totalFb > 0 ? Math.round((positive / totalFb) * 100) : 0
  const storeFeedback = feedback?.storeFeedback ?? []
  const productReviews = feedback?.productReviews ?? []

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
            {feedback?.averageRating ? `${feedback.averageRating.toFixed(1)} ★` : '—'}
          </span>
        </div>
      </div>

      <div className="report-grid">
        {/* Revenue by category (with share %) */}
        <div className="report-card">
          <h3 style={{ fontSize: 18, marginBottom: 14 }}>Revenue by category</h3>
          {byCategory.length === 0 ? (
            <p className="muted" style={{ fontSize: 14 }}>No sales in this period.</p>
          ) : (
            byCategory.map((c, i) => {
              const share = totalCatRevenue > 0 ? Math.round((Number(c.totalRevenue) / totalCatRevenue) * 100) : 0
              return (
                <Bar key={c.label} label={categoryLabel(c.label)} value={Number(c.totalRevenue)}
                  max={maxCatRevenue} display={money(c.totalRevenue)} sub={`${share}%`}
                  color={PALETTE[i % PALETTE.length]} />
              )
            })
          )}
        </div>

        {/* Top sellers (units ⇄ revenue toggle) */}
        <div className="report-card">
          <div className="panel-head" style={{ marginBottom: 14 }}>
            <h3 style={{ fontSize: 18 }}>Top sellers</h3>
            <div className="chips">
              <button className={`chip ${topBy === 'units' ? 'on' : ''}`} onClick={() => setTopBy('units')}>Units</button>
              <button className={`chip ${topBy === 'revenue' ? 'on' : ''}`} onClick={() => setTopBy('revenue')}>Revenue</button>
            </div>
          </div>
          {topMovers.length === 0 ? (
            <p className="muted" style={{ fontSize: 14 }}>No sales in this period.</p>
          ) : (
            topMovers.map((p, i) => (
              <Bar key={p.label} label={p.label} value={metric(p)} max={maxMoved}
                display={topBy === 'units' ? `${p.totalQuantity}` : money(p.totalRevenue)}
                sub={topBy === 'units' ? money(p.totalRevenue) : `${p.totalQuantity} u`}
                color={PALETTE[i % PALETTE.length]} />
            ))
          )}
        </div>
      </div>

      {/* Customer feedback — one card: summary header + store/product sections */}
      <div className="report-card" style={{ marginTop: 20 }}>
        <h3 style={{ fontSize: 18, marginBottom: 16 }}>Customer feedback</h3>

        {totalFb === 0 ? (
          <p className="muted" style={{ fontSize: 14 }}>No feedback yet.</p>
        ) : (
          <>
            <div className="fb-summary">
              <div className="fb-score">
                <span className="fb-avg">{(feedback.averageRating ?? 0).toFixed(1)}</span>
                <span className="fb-stars" style={{ fontSize: 18 }}>
                  {'★'.repeat(Math.round(feedback.averageRating ?? 0))}{'☆'.repeat(5 - Math.round(feedback.averageRating ?? 0))}
                </span>
                <span className="muted" style={{ fontSize: 13 }}>
                  {totalFb} review{totalFb === 1 ? '' : 's'} · {positivePct}% positive
                </span>
              </div>
              <div className="fb-dist">
                {[5, 4, 3, 2, 1].map((r) => (
                  <Bar key={r} label={`${r} ★`} value={Number(ratingCounts[r] ?? 0)} max={maxRating}
                    display={String(ratingCounts[r] ?? 0)} color="var(--sage)" />
                ))}
              </div>
            </div>

            <div className="fb-sections">
              <section>
                <div className="fb-section-head">
                  <span>Store feedback</span>
                  <span className="fb-count">{storeFeedback.length}</span>
                </div>
                {storeFeedback.length === 0 ? (
                  <p className="muted" style={{ fontSize: 13.5 }}>No store feedback yet.</p>
                ) : (
                  <div className="fb-list">
                    {storeFeedback.slice(0, 6).map((f) => <FeedbackItem key={f.id} f={f} />)}
                  </div>
                )}
              </section>

              <section>
                <div className="fb-section-head">
                  <span>Product reviews</span>
                  <span className="fb-count">{productReviews.length}</span>
                </div>
                {productReviews.length === 0 ? (
                  <p className="muted" style={{ fontSize: 13.5 }}>No product reviews yet.</p>
                ) : (
                  <div className="fb-list">
                    {productReviews.slice(0, 6).map((f) => <FeedbackItem key={f.id} f={f} />)}
                  </div>
                )}
              </section>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
