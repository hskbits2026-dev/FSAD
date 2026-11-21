// Admin Customers tab: a KPI summary row + a table of every registered customer
// with their order count and lifetime spend. Read-only: it just GETs the
// admin-only /api/admin/customers endpoint (ROLE_ADMIN, enforced by the backend).

import { useEffect, useState } from 'react'
import { api } from '../../api/client'
import { money } from '../../lib/catalog'

// Format an ISO timestamp as a short, friendly date (e.g. "2 Nov 2026").
function joinedDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export default function CustomersPanel() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    api
      .get('/api/admin/customers')
      .then((list) => active && setCustomers(list ?? []))
      .catch((e) => active && setError(e.message))
      .finally(() => active && setLoading(false))
    return () => { active = false }
  }, [])

  if (loading) {
    return <div className="state"><div className="spinner" />Loading customers…</div>
  }
  if (error) {
    return <div className="alert alert-error">{error}</div>
  }

  // Headline numbers, derived from the list we already have (no extra request).
  const activeCount = customers.filter((c) => c.status === 'ACTIVE').length
  const inactiveCount = customers.length - activeCount
  const lifetimeRevenue = customers.reduce((sum, c) => sum + Number(c.totalSpent ?? 0), 0)

  return (
    <div>
      <div className="panel-head">
        <div>
          <h2 style={{ fontSize: 26 }}>Customers</h2>
          <p className="muted" style={{ fontSize: 14 }}>{customers.length} registered</p>
        </div>
      </div>

      <div className="stat-row">
        <div className="stat-tile">
          <span className="stat-k">Customers</span>
          <span className="stat-v">{customers.length}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Active</span>
          <span className="stat-v">{activeCount}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Inactive</span>
          <span className="stat-v">{inactiveCount}</span>
        </div>
        <div className="stat-tile">
          <span className="stat-k">Lifetime revenue</span>
          <span className="stat-v">{money(lifetimeRevenue)}</span>
        </div>
      </div>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Addresses</th>
              <th>Status</th>
              <th>Joined</th>
              <th className="num">Orders</th>
              <th className="num">Spent</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((c) => (
              <tr key={c.id} className={c.status === 'INACTIVE' ? 'row-muted' : ''}>
                <td className="muted">{c.id}</td>
                <td style={{ fontWeight: 700 }}>{c.fullName}</td>
                <td className="muted">{c.email}</td>
                <td className="muted">{c.phone || '—'}</td>
                <td>
                  {(c.addresses?.length ?? 0) === 0 ? (
                    <span className="muted">—</span>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6, maxWidth: 320 }}>
                      {c.addresses.map((a) => (
                        <div key={a.id} style={{ fontSize: 13 }}>
                          <span style={{ fontWeight: 700 }}>{a.label}</span>
                          {a.isDefault && (
                            <span className="badge badge-paid" style={{ marginLeft: 6, fontSize: 10, padding: '2px 8px' }}>
                              Default
                            </span>
                          )}
                          <div className="muted">
                            {a.line1}{a.line2 ? `, ${a.line2}` : ''}, {a.city}, {a.state} — {a.pincode}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </td>
                <td>
                  <span className={`badge ${c.status === 'ACTIVE' ? 'badge-paid' : 'badge-cancelled'}`}>
                    {c.status === 'ACTIVE' ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="muted">{joinedDate(c.joinedAt)}</td>
                <td className="num">{c.orderCount}</td>
                <td className="num">{money(c.totalSpent)}</td>
              </tr>
            ))}
            {customers.length === 0 && (
              <tr><td colSpan={9} className="muted" style={{ textAlign: 'center', padding: 30 }}>No customers yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
