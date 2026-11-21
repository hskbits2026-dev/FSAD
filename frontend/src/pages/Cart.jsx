import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { useCart } from '../context/CartContext'
import { money, productInitial } from '../lib/catalog'

export default function Cart() {
  const { cart, loading, removeItem } = useCart()
  const [checkingOut, setCheckingOut] = useState(false)
  const [error, setError] = useState('')

  // Saved delivery addresses + which one is selected. Loaded on mount so the customer can
  // pick where this order ships. Checkout is blocked until an address is chosen.
  const [addresses, setAddresses] = useState([])
  const [addrLoading, setAddrLoading] = useState(true)
  const [selectedId, setSelectedId] = useState(null)

  useEffect(() => {
    let active = true
    api
      .get('/api/addresses')
      .then((list) => {
        if (!active) return
        setAddresses(list)
        // Default to the customer's default address (list is default-first from the backend).
        const preferred = list.find((a) => a.isDefault) ?? list[0]
        if (preferred) setSelectedId(preferred.id)
      })
      .catch((e) => active && setError(e.message))
      .finally(() => active && setAddrLoading(false))
    return () => {
      active = false
    }
  }, [])

  async function handleCheckout() {
    if (!selectedId) {
      setError('Please select a delivery address.')
      return
    }
    setCheckingOut(true)
    setError('')
    try {
      // Backend creates a Stripe Checkout session, snapshotting the chosen address onto the order.
      const res = await api.post('/api/checkout', { addressId: selectedId })
      window.location.href = res.checkoutUrl // hand off to Stripe's payment page
    } catch (e) {
      setError(e.message)
      setCheckingOut(false)
    }
  }

  if (loading && !cart) {
    return (
      <main className="wrap">
        <div className="state"><div className="spinner" />Loading your cart…</div>
      </main>
    )
  }

  const items = cart?.items ?? []

  return (
    <main className="wrap">
      <h1 style={{ fontSize: 40, padding: '28px 0 6px' }}>Your cart</h1>

      {items.length === 0 ? (
        <div className="state">
          Your cart is empty.
          <div style={{ marginTop: 20 }}>
            <Link to="/" className="btn btn-terra">Browse the shop</Link>
          </div>
        </div>
      ) : (
        <div className="cart-grid">
          <div>
            {items.map((item) => (
              <div className="cart-item" key={item.id}>
                <div className="ci-thumb" style={{ background: 'linear-gradient(145deg,#c08a6a,#7a4c2e)' }}>
                  {productInitial(item.productName)}
                </div>
                <div className="ci-main">
                  <div className="ci-name">{item.productName}</div>
                  <div className="muted" style={{ fontSize: 14 }}>
                    {money(item.price)} × {item.quantity}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontWeight: 800 }}>{money(item.lineTotal)}</div>
                  <button
                    onClick={() => removeItem(item.id)}
                    style={{ background: 'none', border: 'none', color: 'var(--danger)', fontWeight: 700, cursor: 'pointer', fontSize: 13, marginTop: 6 }}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <aside className="summary">
            <h3>Order summary</h3>

            {/* ---- Delivery address ---- */}
            <div style={{ marginBottom: 16 }}>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 700, color: 'var(--soft)', marginBottom: 7 }}>
                Deliver to
              </label>
              {addrLoading ? (
                <p className="muted" style={{ fontSize: 14 }}>Loading addresses…</p>
              ) : addresses.length === 0 ? (
                <div className="alert alert-error" style={{ fontSize: 14 }}>
                  No saved address. <Link to="/profile" style={{ fontWeight: 700 }}>Add one in your profile</Link> to check out.
                </div>
              ) : (
                <>
                  <select className="select" value={selectedId ?? ''}
                    onChange={(e) => setSelectedId(Number(e.target.value))}>
                    {addresses.map((a) => (
                      <option key={a.id} value={a.id}>
                        {a.label} — {a.line1}, {a.city} {a.isDefault ? '(default)' : ''}
                      </option>
                    ))}
                  </select>
                  <Link to="/profile" className="muted" style={{ fontSize: 12.5, fontWeight: 600, display: 'inline-block', marginTop: 8 }}>
                    Manage addresses
                  </Link>
                </>
              )}
            </div>

            <div className="line">
              <span>Items</span>
              <span>{items.reduce((s, i) => s + i.quantity, 0)}</span>
            </div>
            <div className="total">
              <span>Total</span>
              <span>{money(cart.total)}</span>
            </div>

            {error && <div className="alert alert-error">{error}</div>}

            <button className="btn btn-terra btn-block" style={{ marginTop: 18 }}
              onClick={handleCheckout} disabled={checkingOut || !selectedId}>
              {checkingOut ? 'Redirecting…' : 'Proceed to checkout'}
            </button>
            <p className="muted" style={{ fontSize: 12.5, marginTop: 10, textAlign: 'center' }}>
              Secure payment via Stripe
            </p>
          </aside>
        </div>
      )}
    </main>
  )
}
