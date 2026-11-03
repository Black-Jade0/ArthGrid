import { useCallback, useState } from 'react'
import { Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { session } from './session.js'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import OpenAccount from './pages/OpenAccount.jsx'
import AppLayout from './pages/app/AppLayout.jsx'
import Overview from './pages/app/Overview.jsx'
import SendMoney from './pages/app/SendMoney.jsx'
import Statement from './pages/app/Statement.jsx'
import Profile from './pages/app/Profile.jsx'

export default function App() {
  const [user, setUser] = useState(session.get)
  const navigate = useNavigate()

  const login = useCallback((u) => {
    session.set(u)
    setUser(u)
    navigate('/banking')
  }, [navigate])

  const logout = useCallback((reason) => {
    session.clear(reason)
    setUser(null)
    navigate('/login')
  }, [navigate])

  const updateUser = useCallback((u) => {
    session.set(u)
    setUser(u)
  }, [])

  return (
    <Routes>
      <Route path="/" element={<Home user={user} />} />
      <Route path="/login" element={user ? <Navigate to="/banking" replace /> : <Login onLogin={login} />} />
      <Route path="/open-account" element={<OpenAccount onLogin={login} />} />

      <Route
        path="/banking"
        element={user ? <AppLayout user={user} onLogout={logout} /> : <Navigate to="/login" replace />}
      >
        <Route index element={<Overview user={user} />} />
        <Route path="send" element={<SendMoney user={user} />} />
        <Route path="statement" element={<Statement user={user} />} />
        <Route path="profile" element={<Profile user={user} onUpdate={updateUser} />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
