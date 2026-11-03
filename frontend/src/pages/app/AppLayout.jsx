import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { maskAcc } from '../../api.js'
import { IDLE_LIMIT_MS } from '../../session.js'
import { DemoNotice } from '../../components/PublicChrome.jsx'
import { Logo } from '../../components/ui.jsx'

const NAV = [
  { to: '/banking', label: 'Overview', end: true, icon: 'M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z' },
  { to: '/banking/send', label: 'Send money', icon: 'M4 12h14M13 6l6 6-6 6' },
  { to: '/banking/statement', label: 'Statement', icon: 'M6 3h9l4 4v14H6zM9 11h7M9 15h7M9 7h4' },
  { to: '/banking/profile', label: 'Profile', icon: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21a8 8 0 0 1 16 0' },
]

function greeting() {
  const h = new Date().getHours()
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
}

export default function AppLayout({ user, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const timer = useRef()

  // Automatic logout after a period without mouse/keyboard/touch activity
  useEffect(() => {
    const reset = () => {
      clearTimeout(timer.current)
      timer.current = setTimeout(
        () => onLogout('You were logged out after 5 minutes of inactivity, to keep your account safe.'),
        IDLE_LIMIT_MS,
      )
    }
    const events = ['mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll']
    events.forEach((e) => window.addEventListener(e, reset, { passive: true }))
    reset()
    return () => {
      clearTimeout(timer.current)
      events.forEach((e) => window.removeEventListener(e, reset))
    }
  }, [onLogout])

  return (
    <div className="bank">
      <DemoNotice />
      <div className="bank-shell">
        <aside className={'sidebar' + (menuOpen ? ' open' : '')}>
          <div className="sidebar-head">
            <Logo to="/banking" light />
          </div>
          <nav className="sidebar-nav" onClick={() => setMenuOpen(false)}>
            {NAV.map((n) => (
              <NavLink key={n.to} to={n.to} end={n.end}>
                <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
                  <path d={n.icon} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                {n.label}
              </NavLink>
            ))}
          </nav>
          <div className="sidebar-foot">
            <p>Session ends after 5 minutes of inactivity.</p>
            <button className="btn btn-outline-light btn-block" onClick={() => onLogout()}>Log out</button>
          </div>
        </aside>

        <div className="bank-main">
          <header className="topbar">
            <button className="menu-btn dark" aria-label="Menu" aria-expanded={menuOpen} onClick={() => setMenuOpen(!menuOpen)}>
              <span /><span /><span />
            </button>
            <div className="topbar-greeting">
              <b>{greeting()}, {user.firstName}</b>
              <small>Savings account {maskAcc(user.accountNumber)}</small>
            </div>
            <div className="topbar-user">
              <span className="avatar">{user.firstName?.[0]}{user.lastName?.[0]}</span>
              <button className="btn btn-ghost btn-sm" onClick={() => onLogout()}>Log out</button>
            </div>
          </header>
          <main className="bank-content">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}
