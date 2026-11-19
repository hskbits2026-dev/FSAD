// A pop-up form for creating OR editing a product. The same form does both:
//  - no `product` prop  → "Add product" (we POST to create)
//  - a `product` prop    → "Edit product" (we PUT to update)
// It only collects + validates input, then hands a clean payload to the parent
// via onSave(payload). The parent decides which API call to make.

import { useState } from 'react'
import { CATEGORIES, categoryLabel } from '../../lib/catalog'

const EMPTY = { name: '', description: '', price: '', qoh: '', category: 'ELECTRONICS', image: '' }

// Must match the backend cap: the browser-encoded Base64 string stays under ProductRequest's
// @Size(max = 1_400_000). A ~1 MB source file encodes to ~1.33 MB of text — comfortably inside it.
const MAX_IMAGE_BYTES = 1024 * 1024 // 1 MB

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
          image: product.image ?? '',
        }
      : EMPTY,
  )
  const [error, setError] = useState('')

  function set(key, value) {
    setForm((f) => ({ ...f, [key]: value }))
  }

  // When the admin picks a file, the BROWSER converts it to a Base64 "data URL" string
  // (FileReader.readAsDataURL). That string is plain text, so it rides along in the normal
  // JSON create/update request — no file-upload endpoint needed.
  function onPickImage(e) {
    const file = e.target.files?.[0]
    e.target.value = '' // let the same file be re-picked after a removal
    if (!file) return
    if (!file.type.startsWith('image/')) return setError('Please choose an image file.')
    if (file.size > MAX_IMAGE_BYTES) return setError('Image is too large — please pick one under 1 MB.')
    const reader = new FileReader()
    reader.onload = () => {
      set('image', reader.result)
      setError('')
    }
    reader.onerror = () => setError('Could not read that file. Please try another.')
    reader.readAsDataURL(file)
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
      image: form.image || null, // null = "no picture" (backend column is nullable)
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
              <label>Price (₹)</label>
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

          <div className="field">
            <label>Image <span className="muted" style={{ fontWeight: 400 }}>(optional, under 1 MB)</span></label>
            {form.image ? (
              <div className="img-edit">
                <img src={form.image} alt="Product preview" className="img-preview" />
                <button type="button" className="btn btn-ghost" onClick={() => set('image', '')}>
                  Remove image
                </button>
              </div>
            ) : (
              <input type="file" accept="image/*" onChange={onPickImage} />
            )}
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
