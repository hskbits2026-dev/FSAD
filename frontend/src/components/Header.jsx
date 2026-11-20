import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

export default function Header() {
  const { isAuthenticated, isAdmin, email, logout } = useAuth()
  const { count } = useCart()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="header">
      <NavLink to="/" className="brand">
        <span className="leaf" />
        everystore
      </NavLink>

      <nav className="nav">
        <NavLink to="/">Shop</NavLink>
        {isAuthenticated && !isAdmin && <NavLink to="/orders">Orders</NavLink>}
        {isAuthenticated && !isAdmin && <NavLink to="/feedback">Feedback</NavLink>}
        {isAdmin && <NavLink to="/admin">Admin</NavLink>}
      </nav>

      <div className="header-right">
        {isAuthenticated ? (
          <>
            <span className="muted" style={{ fontSize: 13.5, fontWeight: 600 }}>
              {email}
            </span>
            <NavLink to="/cart" className="btn btn-ghost cart-link">
              Cart
              {count > 0 && <span className="cart-count">{count}</span>}
            </NavLink>
            <button className="btn btn-solid" onClick={handleLogout}>
              Log out
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login" className="btn btn-ghost">
              Log in
            </NavLink>
            <NavLink to="/register" className="btn btn-terra">
              Sign up
            </NavLink>
          </>
        )}
      </div>
    </header>
  )
}
