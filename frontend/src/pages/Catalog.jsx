import { useEffect, useState } from 'react'
import { api } from '../api/client'
import ProductCard from '../components/ProductCard'
import { CATEGORIES, categoryLabel } from '../lib/catalog'

const PAGE_SIZE = 9

export default function Catalog() {
  const [category, setCategory] = useState(null) // null = "All"
  const [page, setPage] = useState(0) // backend pages are 0-based
  const [data, setData] = useState(null) // the Spring Page object
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    const params = new URLSearchParams({ page, size: PAGE_SIZE, sort: 'id' })
    if (category) params.set('category', category)

    api
      .get(`/api/products?${params.toString()}`)
      .then((res) => active && setData(res))
      .catch((err) => active && setError(err.message))
      .finally(() => active && setLoading(false))

    return () => {
      active = false // ignore a stale response if the filter changed quickly
    }
  }, [category, page])

  function pick(cat) {
    setCategory(cat)
    setPage(0)
  }

  const products = data?.content ?? []

  return (
    <main className="wrap">
      <section className="hero">
        <span className="eyebrow">🌿 Thoughtfully stocked</span>
        <h1 className="display">
          Everyday things,<br />
          <em>nicely</em> made.
        </h1>
        <p className="lede" style={{ marginTop: 18 }}>
          A friendly little shop for home, desk and self — honest prices, real stock,
          and a checkout that takes seconds.
        </p>
      </section>

      <div className="catbar">
        <h2 style={{ fontSize: 26 }}>Fresh picks</h2>
        <div className="chips">
          <button className={`chip ${!category ? 'on' : ''}`} onClick={() => pick(null)}>
            All
          </button>
          {CATEGORIES.map((c) => (
            <button key={c} className={`chip ${category === c ? 'on' : ''}`} onClick={() => pick(c)}>
              {categoryLabel(c)}
            </button>
          ))}
        </div>
      </div>

      {loading && (
        <div className="state">
          <div className="spinner" />
          Loading products…
        </div>
      )}

      {error && !loading && (
        <div className="state">
          <div className="alert alert-error" style={{ display: 'inline-block' }}>{error}</div>
        </div>
      )}

      {!loading && !error && products.length === 0 && (
        <div className="state">No products here yet.</div>
      )}

      {!loading && !error && products.length > 0 && (
        <>
          <section className="grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </section>

          {data.totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: 14, alignItems: 'center', paddingBottom: 60 }}>
              <button className="btn btn-ghost" disabled={data.first} onClick={() => setPage((p) => p - 1)}>
                ← Prev
              </button>
              <span className="muted" style={{ fontWeight: 700 }}>
                Page {data.number + 1} of {data.totalPages}
              </span>
              <button className="btn btn-ghost" disabled={data.last} onClick={() => setPage((p) => p + 1)}>
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </main>
  )
}
