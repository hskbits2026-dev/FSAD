import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from './AuthContext'

// Shared wishlist state: which product ids the logged-in customer has favourited, plus
// actions to toggle them. Mirrors CartContext so any component (a product card's heart,
// the product detail page, the wishlist page) reads and updates the same set.
const FavouritesContext = createContext(null)

export function FavouritesProvider({ children }) {
  const { isAuthenticated } = useAuth()
  // We keep the full favourite entries (for the wishlist page) AND a Set of product ids
  // (for fast isFavourite(id) lookups by the hearts).
  const [favourites, setFavourites] = useState([])
  const [ids, setIds] = useState(() => new Set())
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setFavourites([])
      setIds(new Set())
      return
    }
    setLoading(true)
    try {
      const list = await api.get('/api/favourites')
      setFavourites(list)
      setIds(new Set(list.map((f) => f.product.id)))
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  // Load favourites whenever the user logs in (or on first mount if already logged in).
  useEffect(() => {
    refresh()
  }, [refresh])

  function isFavourite(productId) {
    return ids.has(productId)
  }

  // Add or remove, then refresh so every heart + the wishlist page stay in sync.
  async function toggle(productId) {
    if (ids.has(productId)) {
      await api.del(`/api/favourites/${productId}`)
    } else {
      await api.post(`/api/favourites/${productId}`)
    }
    await refresh()
  }

  const count = favourites.length
  const value = { favourites, count, loading, isFavourite, toggle, refresh }
  return <FavouritesContext.Provider value={value}>{children}</FavouritesContext.Provider>
}

export function useFavourites() {
  const ctx = useContext(FavouritesContext)
  if (!ctx) throw new Error('useFavourites must be used inside <FavouritesProvider>')
  return ctx
}
