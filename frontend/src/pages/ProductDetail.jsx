import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import FeedbackForm from '../components/FeedbackForm'
import { categoryInfo, categoryLabel, money, productInitial } from '../lib/catalog'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()
  const { isAuthenticated } = useAuth()

  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [qty, setQty] = useState(1)
  const [adding, setAdding] = useState(false)
  const [added, setAdded] = useState(false)
  const [imgBroken, setImgBroken] = useState(false)
  const [reviews, setReviews] = useState(null) // ProductReviewsResponse

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    setImgBroken(false)
    api
      .get(`/api/products/${id}`)
      .then((res) => active && setProduct(res))
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false))
    return () => {
      active = false
    }
  }, [id])

  // Reviews load separately (public endpoint) so a review error never blocks the product view.
  useEffect(() => {
    let active = true
    api.get(`/api/products/${id}/feedback`).then((res) => active && setReviews(res)).catch(() => {})
    return () => {
      active = false
    }
  }, [id])

  function refreshReviews() {
    return api.get(`/api/products/${id}/feedback`).then(setReviews)
  }

  async function submitReview({ rating, message }) {
    await api.post(`/api/products/${id}/feedback`, { message, rating })
    await refreshReviews()
  }

  async function handleAdd() {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/products/${id}` } })
      return
    }
    setAdding(true)
    setAdded(false)
    try {
      await addItem(product.id, qty)
      setAdded(true)
    } catch (e) {
      setError(e.message)
    } finally {
      setAdding(false)
    }
  }

  if (loading) {
    return (
      <main className="wrap">
        <div className="state"><div className="spinner" />Loading…</div>
      </main>
    )
  }
  if (error || !product) {
    return (
      <main className="wrap">
        <div className="state">
          <div className="alert alert-error" style={{ display: 'inline-block' }}>
            {error || 'Product not found'}
          </div>
          <div style={{ marginTop: 20 }}>
            <Link to="/" className="btn btn-ghost">← Back to shop</Link>
          </div>
        </div>
      </main>
    )
  }

  const info = categoryInfo(product.category)
  const outOfStock = product.qoh <= 0

  return (
    <main className="wrap">
      <div style={{ padding: '18px 0' }}>
        <Link to="/" className="muted" style={{ fontWeight: 600 }}>← Back to shop</Link>
      </div>
      <div className="detail">
        <div className="d-thumb" style={{ background: info.grad }}>
          {product.image && !imgBroken ? (
            <img
              className="d-thumb-img"
              src={product.image}
              alt={product.name}
              onError={() => setImgBroken(true)}
            />
          ) : (
            productInitial(product.name)
          )}
        </div>
        <div>
          <div className="cat" style={{ color: 'var(--sage)', fontWeight: 700, letterSpacing: '.06em', textTransform: 'uppercase', fontSize: 12 }}>
            {categoryLabel(product.category)}
          </div>
          <h1 style={{ fontSize: 40, margin: '8px 0 14px' }}>{product.name}</h1>
          <div className="pr" style={{ fontSize: 28, fontWeight: 800 }}>{money(product.price)}</div>
          <p className="lede" style={{ marginTop: 18 }}>{product.description || 'No description provided.'}</p>

          <p className="muted" style={{ marginTop: 14, fontWeight: 600 }}>
            {outOfStock ? 'Currently sold out' : `${product.qoh} in stock`}
          </p>

          {!outOfStock && (
            <div style={{ display: 'flex', gap: 14, alignItems: 'center', marginTop: 24 }}>
              <div className="qty">
                <button onClick={() => setQty((q) => Math.max(1, q - 1))}>−</button>
                <span>{qty}</span>
                <button onClick={() => setQty((q) => Math.min(product.qoh, q + 1))}>+</button>
              </div>
              <button className="btn btn-terra" onClick={handleAdd} disabled={adding}>
                {adding ? 'Adding…' : 'Add to cart'}
              </button>
            </div>
          )}

          {added && (
            <div className="alert alert-ok" style={{ display: 'inline-block', marginTop: 18 }}>
              Added to cart · <button onClick={() => navigate('/cart')} style={{ background: 'none', border: 'none', color: 'inherit', fontWeight: 700, textDecoration: 'underline', cursor: 'pointer' }}>View cart</button>
            </div>
          )}
        </div>
      </div>

      {/* ---- Reviews: anyone can read; writing requires login ---- */}
      <section style={{ marginTop: 44, borderTop: '1px solid var(--line)', paddingTop: 28 }}>
        <h2 style={{ fontSize: 26, margin: '0 0 6px' }}>Reviews</h2>
        {reviews && reviews.count > 0 ? (
          <p className="muted" style={{ fontWeight: 600 }}>
            <span style={{ color: 'var(--terra)' }}>
              {'★'.repeat(Math.round(reviews.averageRating))}{'☆'.repeat(5 - Math.round(reviews.averageRating))}
            </span>{' '}
            {reviews.averageRating.toFixed(1)} · {reviews.count} review{reviews.count > 1 ? 's' : ''}
          </p>
        ) : (
          <p className="muted">No reviews yet — be the first to review this product.</p>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: 14, margin: '18px 0 28px' }}>
          {reviews?.items.map((r, i) => (
            <div key={i} style={{ border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)', padding: '14px 16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong>{r.reviewerName}</strong>
                <span style={{ color: 'var(--terra)' }}>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
              </div>
              <p style={{ margin: '8px 0 0' }}>{r.message}</p>
              <div className="muted" style={{ fontSize: 12, marginTop: 6 }}>
                {new Date(r.createdAt).toLocaleDateString()}
              </div>
            </div>
          ))}
        </div>

        <div style={{ maxWidth: 520, border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)', padding: '20px 22px' }}>
          <h3 style={{ margin: '0 0 10px' }}>Write a review</h3>
          {isAuthenticated ? (
            <FeedbackForm onSubmit={submitReview} submitLabel="Submit review"
              doneText="Thanks! Your review was posted. 🌿" />
          ) : (
            <>
              <p className="muted" style={{ margin: '0 0 14px' }}>Please log in to write a review.</p>
              <button className="btn btn-terra"
                onClick={() => navigate('/login', { state: { from: `/products/${id}` } })}>
                Log in to write a review
              </button>
            </>
          )}
        </div>
      </section>
    </main>
  )
}
