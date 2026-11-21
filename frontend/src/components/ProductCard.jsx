import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useFavourites } from '../context/FavouritesContext'
import { useAuth } from '../context/AuthContext'
import { categoryInfo, categoryLabel, money, productInitial } from '../lib/catalog'

export default function ProductCard({ product }) {
  const { addItem } = useCart()
  const { isFavourite, toggle } = useFavourites()
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [adding, setAdding] = useState(false)
  const [imgBroken, setImgBroken] = useState(false)
  const info = categoryInfo(product.category)
  const outOfStock = product.qoh <= 0
  const showImage = product.image && !imgBroken
  const faved = isFavourite(product.id)

  async function handleAdd() {
    setAdding(true)
    try {
      await addItem(product.id, 1)
    } catch (e) {
      alert(e.message) // simple for now; a nicer toast comes later
    } finally {
      setAdding(false)
    }
  }

  // The heart sits inside a <Link>, so stop the click from navigating to the product page.
  // Guests are sent to log in (favourites live on their account).
  async function handleFav(e) {
    e.preventDefault()
    e.stopPropagation()
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/products/${product.id}` } })
      return
    }
    try {
      await toggle(product.id)
    } catch (err) {
      alert(err.message)
    }
  }

  return (
    <article className="card">
      <Link to={`/products/${product.id}`} className="thumb" style={{ background: info.grad }}>
        {showImage ? (
          // Real product photo (Base64 data URL from the backend). If it ever fails to load,
          // onError flips imgBroken and we fall back to the gradient + letter tile below.
          <img
            className="thumb-img"
            src={product.image}
            alt={product.name}
            onError={() => setImgBroken(true)}
          />
        ) : (
          <b>{productInitial(product.name)}</b>
        )}
        <button
          type="button"
          className={`fav${faved ? ' on' : ''}`}
          onClick={handleFav}
          aria-pressed={faved}
          aria-label={faved ? 'Remove from favourites' : 'Add to favourites'}
          title={faved ? 'Remove from favourites' : 'Add to favourites'}
        >
          {faved ? '♥' : '♡'}
        </button>
      </Link>
      <div className="cat">{categoryLabel(product.category)}</div>
      <Link to={`/products/${product.id}`} className="nm">
        {product.name}
      </Link>
      <div className="row">
        <span className="pr">{money(product.price)}</span>
        {outOfStock ? (
          <span className="stock-out">Sold out</span>
        ) : (
          <button className="btn btn-butter" onClick={handleAdd} disabled={adding}>
            {adding ? 'Adding…' : 'Add to cart'}
          </button>
        )}
      </div>
    </article>
  )
}
