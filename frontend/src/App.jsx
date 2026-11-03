import { Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Catalog from './pages/Catalog'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import CheckoutSuccess from './pages/CheckoutSuccess'
import CheckoutCancel from './pages/CheckoutCancel'
import Orders from './pages/Orders'
import Feedback from './pages/Feedback'
import Admin from './pages/Admin'

export default function App() {
  return (
    <>
      <Header />
      <Routes>
        {/* Public auth pages */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Everything below requires being logged in (backend needs a JWT too) */}
        <Route path="/" element={<ProtectedRoute><Catalog /></ProtectedRoute>} />
        <Route path="/products/:id" element={<ProtectedRoute><ProductDetail /></ProtectedRoute>} />
        <Route path="/cart" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
        <Route path="/orders" element={<ProtectedRoute><Orders /></ProtectedRoute>} />
        <Route path="/feedback" element={<ProtectedRoute><Feedback /></ProtectedRoute>} />

        {/* Admin-only dashboard (ProtectedRoute bounces non-admins back to /) */}
        <Route path="/admin" element={<ProtectedRoute adminOnly><Admin /></ProtectedRoute>} />

        {/* Stripe return URLs (match stripe.success-url / cancel-url in the backend) */}
        <Route path="/checkout/success" element={<ProtectedRoute><CheckoutSuccess /></ProtectedRoute>} />
        <Route path="/checkout/cancel" element={<ProtectedRoute><CheckoutCancel /></ProtectedRoute>} />

        {/* Fallback */}
        <Route path="*" element={<Catalog />} />
      </Routes>

      <div className="wrap">
        <footer className="footer">
          <span>🌿 everystore — made with care, 2026</span>
          <span>Free shipping over $75</span>
        </footer>
      </div>
    </>
  )
}
