import { useEffect, useState } from 'react'
import { api } from '../api/client'
import AddressFormModal from '../components/AddressFormModal'

// The logged-in customer's account page: edit name + phone, and manage saved addresses
// (add / edit / delete / set default). Data is loaded from GET /api/profile on mount.
export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Name/phone form
  const [fullName, setFullName] = useState('')
  const [phone, setPhone] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileMsg, setProfileMsg] = useState('')

  // Address modal
  const [modalOpen, setModalOpen] = useState(false)
  const [editingAddress, setEditingAddress] = useState(null)
  const [savingAddress, setSavingAddress] = useState(false)

  async function load() {
    setLoading(true)
    try {
      const p = await api.get('/api/profile')
      setProfile(p)
      setFullName(p.fullName ?? '')
      setPhone(p.phone ?? '')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function saveProfile(e) {
    e.preventDefault()
    setProfileMsg('')
    setError('')
    if (!fullName.trim()) return setError('Name is required.')
    if (!/^\d{10}$/.test(phone.trim())) return setError('Phone must be 10 digits.')
    setSavingProfile(true)
    try {
      const updated = await api.put('/api/profile', { fullName: fullName.trim(), phone: phone.trim() })
      setProfile(updated)
      setProfileMsg('Profile updated. 🌿')
    } catch (e) {
      setError(e.message)
    } finally {
      setSavingProfile(false)
    }
  }

  function openAdd() {
    setEditingAddress(null)
    setModalOpen(true)
  }

  function openEdit(addr) {
    setEditingAddress(addr)
    setModalOpen(true)
  }

  async function saveAddress(payload) {
    setSavingAddress(true)
    setError('')
    try {
      if (editingAddress) {
        await api.put(`/api/addresses/${editingAddress.id}`, payload)
      } else {
        await api.post('/api/addresses', payload)
      }
      setModalOpen(false)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSavingAddress(false)
    }
  }

  async function deleteAddress(addr) {
    if (!window.confirm(`Delete the "${addr.label}" address?`)) return
    setError('')
    try {
      await api.del(`/api/addresses/${addr.id}`)
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  async function makeDefault(addr) {
    setError('')
    try {
      await api.put(`/api/addresses/${addr.id}/default`)
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  if (loading) {
    return (
      <main className="wrap">
        <div className="state"><div className="spinner" />Loading your profile…</div>
      </main>
    )
  }

  const addresses = profile?.addresses ?? []

  return (
    <main className="wrap">
      <h1 style={{ fontSize: 40, padding: '28px 0 6px' }}>Your profile</h1>

      {error && <div className="alert alert-error">{error}</div>}

      {/* ---- Name + phone ---- */}
      <section style={{ maxWidth: 520, border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)', padding: '22px 24px', marginTop: 14 }}>
        <h2 style={{ fontSize: 22, marginBottom: 4 }}>Account details</h2>
        <form onSubmit={saveProfile}>
          <div className="field">
            <label>Email</label>
            <input value={profile?.email ?? ''} disabled />
          </div>
          <div className="field">
            <label>Full name</label>
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} />
          </div>
          <div className="field">
            <label>Phone</label>
            <input value={phone} onChange={(e) => setPhone(e.target.value)}
              inputMode="numeric" placeholder="10-digit number" />
          </div>
          {profileMsg && <div className="alert alert-ok" style={{ marginTop: 14 }}>{profileMsg}</div>}
          <button className="btn btn-terra" style={{ marginTop: 18 }} disabled={savingProfile}>
            {savingProfile ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      </section>

      {/* ---- Addresses ---- */}
      <section style={{ marginTop: 36 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', maxWidth: 760 }}>
          <h2 style={{ fontSize: 22 }}>Delivery addresses</h2>
          <button className="btn btn-ghost" onClick={openAdd}>+ Add address</button>
        </div>

        {addresses.length === 0 ? (
          <p className="muted" style={{ marginTop: 12 }}>No addresses saved yet. Add one so you can check out.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 16, maxWidth: 760 }}>
            {addresses.map((a) => (
              <div key={a.id} style={{ border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)', padding: '16px 18px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                  <strong>{a.label}</strong>
                  {a.isDefault && <span className="badge badge-paid">Default</span>}
                </div>
                <div>{a.recipientName} · {a.phone}</div>
                <div className="muted" style={{ marginTop: 2 }}>
                  {a.line1}{a.line2 ? `, ${a.line2}` : ''}, {a.city}, {a.state} — {a.pincode}
                </div>
                <div style={{ display: 'flex', gap: 16, marginTop: 12 }}>
                  {!a.isDefault && (
                    <button onClick={() => makeDefault(a)} className="link-btn">Make default</button>
                  )}
                  <button onClick={() => openEdit(a)} className="link-btn">Edit</button>
                  <button onClick={() => deleteAddress(a)} className="link-btn" style={{ color: 'var(--danger)' }}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {modalOpen && (
        <AddressFormModal
          address={editingAddress}
          onSave={saveAddress}
          onClose={() => setModalOpen(false)}
          busy={savingAddress}
        />
      )}
    </main>
  )
}
