// Product management for admins: a table of every product with "Add", "Edit"
// and "Delete" actions. Reads the normal public product list; writes go to the
// admin-only endpoints (/api/admin/products), which require ROLE_ADMIN.

import { useCallback, useEffect, useState } from 'react'
import { api } from '../../api/client'
import { categoryLabel, money } from '../../lib/catalog'
import ProductFormModal from './ProductFormModal'

const PAGE_SIZE = 100 // admins want the whole catalogue in one view, not paged

export default function ProductManager() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modal, setModal] = useState(null) // null | { product?: {...} }
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    api
      .get(`/api/products?page=0&size=${PAGE_SIZE}&sort=id`)
      .then((res) => setProducts(res.content ?? []))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(load, [load])

  async function handleSave(payload) {
    setBusy(true)
    try {
      if (modal?.product) {
        await api.put(`/api/admin/products/${modal.product.id}`, payload)
      } else {
        await api.post('/api/admin/products', payload)
      }
      setModal(null)
      load() // refresh the table from the source of truth
    } catch (e) {
      alert(e.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete(p) {
    if (!confirm(`Delete "${p.name}"? This cannot be undone.`)) return
    try {
      await api.del(`/api/admin/products/${p.id}`)
      setProducts((list) => list.filter((x) => x.id !== p.id))
    } catch (e) {
      alert(e.message)
    }
  }

  if (loading) {
    return <div className="state"><div className="spinner" />Loading products…</div>
  }

  return (
    <div>
      <div className="panel-head">
        <div>
          <h2 style={{ fontSize: 26 }}>Products</h2>
          <p className="muted" style={{ fontSize: 14 }}>{products.length} in catalogue</p>
        </div>
        <button className="btn btn-terra" onClick={() => setModal({})}>+ Add product</button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Category</th>
              <th className="num">Price</th>
              <th className="num">Stock</th>
              <th className="num">Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td className="muted">{p.id}</td>
                <td style={{ fontWeight: 700 }}>{p.name}</td>
                <td><span className="cat-pill">{categoryLabel(p.category)}</span></td>
                <td className="num">{money(p.price)}</td>
                <td className="num">
                  <span className={p.qoh === 0 ? 'stock-out' : ''}>{p.qoh}</span>
                </td>
                <td className="num">
                  <button className="link-btn" onClick={() => setModal({ product: p })}>Edit</button>
                  <button className="link-btn danger" onClick={() => handleDelete(p)}>Delete</button>
                </td>
              </tr>
            ))}
            {products.length === 0 && (
              <tr><td colSpan={6} className="muted" style={{ textAlign: 'center', padding: 30 }}>No products yet.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {modal && (
        <ProductFormModal
          product={modal.product}
          busy={busy}
          onSave={handleSave}
          onClose={() => (busy ? null : setModal(null))}
        />
      )}
    </div>
  )
}
