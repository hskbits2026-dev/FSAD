import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api } from '../api/client'
import { useAuth } from './AuthContext'

// Shared cart state: the current cart plus actions to change it.
// Any component (header badge, catalog card, cart page) reads the same cart.
const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(false)

  // Fetch the cart from the backend. Wrapped in useCallback so it's stable.
  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null)
      return
    }
    setLoading(true)
    try {
      setCart(await api.get('/api/cart'))
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  // Load the cart whenever the user logs in (or on first mount if already logged in).
  useEffect(() => {
    refresh()
  }, [refresh])

  async function addItem(productId, quantity = 1) {
    const updated = await api.post('/api/cart/items', { productId, quantity })
    setCart(updated)
    return updated
  }

  async function removeItem(itemId) {
    await api.del(`/api/cart/items/${itemId}`)
    await refresh()
  }

  const count = cart?.items?.reduce((sum, i) => sum + i.quantity, 0) ?? 0

  const value = { cart, loading, count, refresh, addItem, removeItem }
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used inside <CartProvider>')
  return ctx
}
