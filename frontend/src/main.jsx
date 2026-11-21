import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { CartProvider } from './context/CartContext.jsx'
import { FavouritesProvider } from './context/FavouritesContext.jsx'

// Providers wrap the whole app so any page can read the logged-in user (AuthProvider),
// the current cart (CartProvider) and the wishlist (FavouritesProvider). BrowserRouter
// turns URLs into page changes.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <FavouritesProvider>
            <App />
          </FavouritesProvider>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
