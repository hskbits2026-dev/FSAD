import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'

// Stripe redirects here after a successful payment. The backend webhook marks the
// order PAID and clears the cart shortly after; we refresh so the badge updates.
export default function CheckoutSuccess() {
  const { refresh } = useCart()

  useEffect(() => {
    refresh()
  }, [refresh])

  return (
    <main className="wrap">
      <div className="state">
        <div className="result-icon ok">✓</div>
        <h1 style={{ fontSize: 36, color: 'var(--ink)' }}>Thank you!</h1>
        <p className="lede" style={{ margin: '12px auto 0' }}>
          Your payment went through and your order is confirmed. A receipt is on its way to your email.
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginTop: 26 }}>
          <Link to="/orders" className="btn btn-terra">View my orders</Link>
          <Link to="/" className="btn btn-ghost">Keep shopping</Link>
        </div>
      </div>
    </main>
  )
}
