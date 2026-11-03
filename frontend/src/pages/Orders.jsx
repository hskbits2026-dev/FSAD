import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { money } from '../lib/catalog'

const STATUS = {
  PAID: { label: 'Paid', cls: 'badge-paid' },
  PENDING_PAYMENT: { label: 'Pending payment', cls: 'badge-pending' },
  CANCELLED: { label: 'Cancelled', cls: 'badge-cancelled' },
}

function formatDate(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  function load() {
    setLoading(true)
    setError('')
    api
      .get('/api/orders')
      .then(setOrders)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleDelete(id) {
    if (!confirm('Remove this order from your history?')) return
    try {
      await api.del(`/api/orders/${id}`)
      setOrders((list) => list.filter((o) => o.id !== id))
    } catch (e) {
      alert(e.message)
    }
  }

  if (loading) {
    return <main className="wrap"><div className="state"><div className="spinner" />Loading orders…</div></main>
  }

  return (
    <main className="wrap">
      <h1 style={{ fontSize: 40, padding: '28px 0 6px' }}>My orders</h1>

      {error && <div className="state"><div className="alert alert-error" style={{ display: 'inline-block' }}>{error}</div></div>}

      {!error && orders.length === 0 && (
        <div className="state">
          You haven’t placed any orders yet.
          <div style={{ marginTop: 20 }}><Link to="/" className="btn btn-terra">Start shopping</Link></div>
        </div>
      )}

      <div style={{ paddingBottom: 70 }}>
        {orders.map((o) => {
          const s = STATUS[o.status] || { label: o.status, cls: 'badge-pending' }
          return (
            <div className="order-card" key={o.id}>
              <div className="order-head">
                <div>
                  <span className="order-id">Order #{o.id}</span>
                  <span className="muted" style={{ marginLeft: 12, fontSize: 13.5 }}>{formatDate(o.orderDate)}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                  <span className={`badge ${s.cls}`}>{s.label}</span>
                  <button onClick={() => handleDelete(o.id)}
                    style={{ background: 'none', border: 'none', color: 'var(--danger)', fontWeight: 700, cursor: 'pointer', fontSize: 13 }}>
                    Delete
                  </button>
                </div>
              </div>
              <div className="order-items">
                {o.items.map((it) => (
                  <div className="order-line" key={it.id}>
                    <span>{it.productName} × {it.quantity}</span>
                    <span className="muted">{money(it.priceAtPurchase)} each</span>
                  </div>
                ))}
              </div>
              <div className="order-foot">
                <span className="muted" style={{ fontWeight: 600 }}>Total</span>
                <span style={{ fontWeight: 800, fontSize: 18 }}>{money(o.totalAmount)}</span>
              </div>
            </div>
          )
        })}
      </div>
    </main>
  )
}
