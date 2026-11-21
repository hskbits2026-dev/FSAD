import { useState } from 'react'

// Add/Edit address pop-up. Same form does both:
//  - no `address` prop → "Add address" (parent POSTs)
//  - an `address` prop → "Edit address" (parent PUTs)
// It validates lightly to match the backend (10-digit phone, 6-digit pincode), then hands a
// clean payload to onSave(payload). The parent picks the API call.
const EMPTY = {
  label: '', recipientName: '', phone: '',
  line1: '', line2: '', city: '', state: '', pincode: '',
}

export default function AddressFormModal({ address, onSave, onClose, busy }) {
  const editing = !!address
  const [form, setForm] = useState(
    address
      ? {
          label: address.label ?? '',
          recipientName: address.recipientName ?? '',
          phone: address.phone ?? '',
          line1: address.line1 ?? '',
          line2: address.line2 ?? '',
          city: address.city ?? '',
          state: address.state ?? '',
          pincode: address.pincode ?? '',
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
    if (!form.label.trim()) return setError('Give this address a label (e.g. Home).')
    if (!form.recipientName.trim()) return setError('Recipient name is required.')
    if (!/^\d{10}$/.test(form.phone.trim())) return setError('Phone must be 10 digits.')
    if (!form.line1.trim()) return setError('Address line 1 is required.')
    if (!form.city.trim()) return setError('City is required.')
    if (!form.state.trim()) return setError('State is required.')
    if (!/^\d{6}$/.test(form.pincode.trim())) return setError('Pincode must be 6 digits.')

    onSave({
      label: form.label.trim(),
      recipientName: form.recipientName.trim(),
      phone: form.phone.trim(),
      line1: form.line1.trim(),
      line2: form.line2.trim() || null,
      city: form.city.trim(),
      state: form.state.trim(),
      pincode: form.pincode.trim(),
    })
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h2 style={{ fontSize: 24 }}>{editing ? 'Edit address' : 'Add address'}</h2>
          <button className="modal-x" onClick={onClose} aria-label="Close">×</button>
        </div>

        <form onSubmit={submit}>
          <div className="form-row">
            <div className="field" style={{ flex: 1 }}>
              <label>Label</label>
              <input value={form.label} onChange={(e) => set('label', e.target.value)}
                placeholder="Home / Work" autoFocus />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label>Recipient name</label>
              <input value={form.recipientName} onChange={(e) => set('recipientName', e.target.value)} />
            </div>
          </div>

          <div className="field">
            <label>Phone</label>
            <input value={form.phone} onChange={(e) => set('phone', e.target.value)}
              inputMode="numeric" placeholder="10-digit number" />
          </div>

          <div className="field">
            <label>Address line 1</label>
            <input value={form.line1} onChange={(e) => set('line1', e.target.value)} />
          </div>

          <div className="field">
            <label>Address line 2 <span className="muted" style={{ fontWeight: 400 }}>(optional)</span></label>
            <input value={form.line2} onChange={(e) => set('line2', e.target.value)} />
          </div>

          <div className="form-row">
            <div className="field" style={{ flex: 1 }}>
              <label>City</label>
              <input value={form.city} onChange={(e) => set('city', e.target.value)} />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label>State</label>
              <input value={form.state} onChange={(e) => set('state', e.target.value)} />
            </div>
          </div>

          <div className="field">
            <label>Pincode</label>
            <input value={form.pincode} onChange={(e) => set('pincode', e.target.value)}
              inputMode="numeric" placeholder="6 digits" />
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
            <button type="button" className="btn btn-ghost btn-block" onClick={onClose} disabled={busy}>
              Cancel
            </button>
            <button type="submit" className="btn btn-terra btn-block" disabled={busy}>
              {busy ? 'Saving…' : editing ? 'Save changes' : 'Add address'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
