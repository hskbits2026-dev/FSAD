import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { useCart } from '../context/CartContext'
import { categoryInfo, categoryLabel, money, productInitial } from '../lib/catalog'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()

  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [qty, setQty] = useState(1)
  const [adding, setAdding] = useState(false)
  const [added, setAdded] = useState(false)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')
    api
      .get(`/api/products/${id}`)
      .then((res) => active && setProduct(res))
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false))
    return () => {
      active = false
    }
  }, [id])

  async function handleAdd() {
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
        <div className="d-thumb" style={{ background: info.grad }}>{productInitial(product.name)}</div>
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
    </main>
  )
}
