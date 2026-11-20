// The admin dashboard shell. It's a single guarded page (/admin) with three
// tabs; clicking a tab just swaps which panel is shown. Keeping it one page
// (instead of nested routes) keeps the routing simple for now.

import { useState } from 'react'
import ProductManager from '../components/admin/ProductManager'
import ReportsPanel from '../components/admin/ReportsPanel'
import CustomersPanel from '../components/admin/CustomersPanel'
import LiveFeed from '../components/admin/LiveFeed'

const TABS = [
  { key: 'products', label: 'Products' },
  { key: 'reports', label: 'Reports' },
  { key: 'customers', label: 'Customers' },
  { key: 'live', label: 'Live' },
]

export default function Admin() {
  const [tab, setTab] = useState('products')

  return (
    <main className="wrap">
      <div className="admin-top">
        <span className="eyebrow">🛠️ Admin</span>
        <h1 style={{ fontSize: 40, padding: '6px 0 2px' }}>Dashboard</h1>
        <p className="muted">Manage the catalogue, read the numbers, and watch the shop live.</p>
      </div>

      <div className="tabs">
        {TABS.map((t) => (
          <button key={t.key} className={`tab ${tab === t.key ? 'on' : ''}`} onClick={() => setTab(t.key)}>
            {t.label}
          </button>
        ))}
      </div>

      <section style={{ paddingBottom: 80 }}>
        {tab === 'products' && <ProductManager />}
        {tab === 'reports' && <ReportsPanel />}
        {tab === 'customers' && <CustomersPanel />}
        {tab === 'live' && <LiveFeed />}
      </section>
    </main>
  )
}
