import { Link } from 'react-router-dom'

// Stripe redirects here if the shopper backs out of payment. Nothing was charged;
// their cart is still intact (the order stays PENDING_PAYMENT on the backend).
export default function CheckoutCancel() {
  return (
    <main className="wrap">
      <div className="state">
        <div className="result-icon cancel">↩</div>
        <h1 style={{ fontSize: 36, color: 'var(--ink)' }}>Payment cancelled</h1>
        <p className="lede" style={{ margin: '12px auto 0' }}>
          No charge was made and your cart is still here. You can pick up right where you left off.
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center', marginTop: 26 }}>
          <Link to="/cart" className="btn btn-terra">Back to cart</Link>
          <Link to="/" className="btn btn-ghost">Keep shopping</Link>
        </div>
      </div>
    </main>
  )
}
