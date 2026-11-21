import { Link } from 'react-router-dom'
import ProductCard from '../components/ProductCard'
import { useFavourites } from '../context/FavouritesContext'

// Shows every product the customer has favourited. Each card's heart is already wired to the
// shared FavouritesContext, so clicking it here (to un-favourite) removes the card on refresh.
export default function Wishlist() {
  const { favourites, loading } = useFavourites()

  if (loading && favourites.length === 0) {
    return (
      <main className="wrap">
        <div className="state"><div className="spinner" />Loading your wishlist…</div>
      </main>
    )
  }

  return (
    <main className="wrap">
      <h1 style={{ fontSize: 40, padding: '28px 0 6px' }}>Your wishlist</h1>

      {favourites.length === 0 ? (
        <div className="state">
          You haven’t favourited anything yet. Tap the ♡ on a product to save it here.
          <div style={{ marginTop: 20 }}>
            <Link to="/" className="btn btn-terra">Browse the shop</Link>
          </div>
        </div>
      ) : (
        <>
          <p className="muted" style={{ fontWeight: 600, marginBottom: 18 }}>
            {favourites.length} saved item{favourites.length > 1 ? 's' : ''}
          </p>
          <div className="grid">
            {favourites.map((f) => (
              <ProductCard key={f.favouriteId} product={f.product} />
            ))}
          </div>
        </>
      )}
    </main>
  )
}
