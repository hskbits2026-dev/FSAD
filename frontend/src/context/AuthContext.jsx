import { createContext, useContext, useState } from 'react'
import { api, getToken, setToken } from '../api/client'

// "Context" = shared state any component can read without passing props down by hand.
// This one remembers who is logged in (their JWT token, role and email).
const AuthContext = createContext(null)

function readStored() {
  const token = getToken()
  if (!token) return { token: null, role: null, email: null }
  return {
    token,
    role: localStorage.getItem('everystore.role'),
    email: localStorage.getItem('everystore.email'),
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStored)

  function persist(token, role, email) {
    setToken(token)
    localStorage.setItem('everystore.role', role ?? '')
    localStorage.setItem('everystore.email', email ?? '')
    setAuth({ token, role, email })
  }

  // Calls the backend; on success stores the token so later requests are authenticated.
  async function login(email, password) {
    const res = await api.post('/api/auth/login', { email, password }, { auth: false })
    persist(res.token, res.role, email)
    return res
  }

  async function register(email, password, fullName) {
    const res = await api.post('/api/auth/register', { email, password, fullName }, { auth: false })
    persist(res.token, res.role, email)
    return res
  }

  function logout() {
    setToken(null)
    localStorage.removeItem('everystore.role')
    localStorage.removeItem('everystore.email')
    setAuth({ token: null, role: null, email: null })
  }

  const value = {
    ...auth,
    isAuthenticated: !!auth.token,
    isAdmin: auth.role === 'ADMIN',
    login,
    register,
    logout,
  }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// Small helper so components can write `const { login } = useAuth()`.
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}
