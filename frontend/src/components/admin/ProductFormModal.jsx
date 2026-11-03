// A pop-up form for creating OR editing a product. The same form does both:
//  - no `product` prop  → "Add product" (we POST to create)
//  - a `product` prop    → "Edit product" (we PUT to update)
// It only collects + validates input, then hands a clean payload to the parent
// via onSave(payload). The parent decides which API call to make.

import { useState } from 'react'
import { CATEGORIES, categoryLabel } from '../../lib/catalog'

const EMPTY = { name: '', description: '', price: '', qoh: '', category: 'ELECTRONICS' }

export default function ProductFormModal({ product, onSave, onClose, busy }) {
  const editing = !!product
  const [form, setForm] = useState(
    product
      ? {
          name: product.name ?? '',
          description: product.description ?? '',
          price: String(product.price ?? ''),
          qoh: String(product.qoh ?? ''),
          category: product.category ?? 'ELECTRONICS',
        }
      : EMPTY,
  )
  const [error, setError] = useState('')

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  function submit(e) {
    e.preventDefault()
    setError('')
    // Match the backend's ProductRequest validation so we fail fast with a
    // friendly message instead of bouncing off a 400.
    const price = Number(form.price)
    const qoh = Number(form.qoh)
    if (!form.name.trim()) return setError('Please give the product a name.')
    if (!(price > 0)) return setError('Price must be greater than 0.')
    if (!Number.isInteger(qoh) || qoh < 0) return setError('Stock must be 0 or a whole number.')

    onSave({
      name: form.name.trim(),
      description: form.description.trim(),
      price, // number; backend reads it as BigDecimal
      qoh,
      category: form.category,
    })
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      {/* stopPropagation = clicking inside the card shouldn't close it */}
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h2 style={{ fontSize: 24 }}>{editing ? 'Edit product' : 'Add product'}</h2>
          <button className="modal-x" onClick={onClose} aria-label="Close">×</button>
        </div>

        <form onSubmit={submit}>
          <div className="field">
            <label>Name</label>
            <input value={form.name} onChange={(e) => set('name', e.target.value)} autoFocus />
          </div>

          <div className="field">
            <label>Description</label>
            <textarea
              rows={3}
              value={form.description}
              onChange={(e) => set('description', e.target.value)}
              style={{
                width: '100%', border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)',
                padding: '13px 15px', fontFamily: 'var(--font-body)', fontSize: 15, resize: 'vertical',
                background: '#fff', color: 'var(--ink)',
              }}
            />
          </div>

          <div className="form-row">
            <div className="field" style={{ flex: 1 }}>
              <label>Price ($)</label>
              <input type="number" step="0.01" min="0" value={form.price}
                onChange={(e) => set('price', e.target.value)} />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label>Stock (qoh)</label>
              <input type="number" step="1" min="0" value={form.qoh}
                onChange={(e) => set('qoh', e.target.value)} />
            </div>
          </div>

          <div className="field">
            <label>Category</label>
            <select className="select" value={form.category} onChange={(e) => set('category', e.target.value)}>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>{categoryLabel(c)}</option>
              ))}
            </select>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
            <button type="button" className="btn btn-ghost btn-block" onClick={onClose} disabled={busy}>
              Cancel
            </button>
            <button type="submit" className="btn btn-terra btn-block" disabled={busy}>
              {busy ? 'Saving…' : editing ? 'Save changes' : 'Create product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
