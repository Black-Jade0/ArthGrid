import { useState } from 'react'
import Auth from './Auth.jsx'
import Dashboard from './Dashboard.jsx'

// The logged-in user is kept in sessionStorage (per browser TAB).
// So for a class demo you can log in as user A in one tab and user B in another.
const KEY = 'arthgrid-user'

function loadUser() {
  try { return JSON.parse(sessionStorage.getItem(KEY)) } catch { return null }
}

export default function App() {
  const [user, setUser] = useState(loadUser)

  const onLogin = (u) => {
    sessionStorage.setItem(KEY, JSON.stringify(u))
    setUser(u)
  }
  const onLogout = () => {
    sessionStorage.removeItem(KEY)
    setUser(null)
  }

  return user ? <Dashboard user={user} onLogout={onLogout} /> : <Auth onLogin={onLogin} />
}
