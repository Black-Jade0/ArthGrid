import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { Logo } from './ui.jsx'

export function DemoNotice() {
  return (
    <div className="demo-notice">
      Academic demo bank. Balances are simulated and no real money moves, but email alerts are real.
    </div>
  )
}

export function PublicHeader({ user }) {
  const [open, setOpen] = useState(false)
  return (
    <>
      <DemoNotice />
      <header className="site-header">
        <div className="wrap site-header-inner">
          <Logo />
          <button className="menu-btn" aria-expanded={open} aria-label="Menu" onClick={() => setOpen(!open)}>
            <span /><span /><span />
          </button>
          <nav className={'site-nav' + (open ? ' open' : '')} onClick={() => setOpen(false)}>
            <a href="/#services">Services</a>
            <a href="/#security">Security</a>
            <a href="/#architecture">How it works</a>
            <a href="/#about">About</a>
            <span className="site-nav-actions">
              {user ? (
                <Link className="btn btn-primary" to="/banking">Go to net banking</Link>
              ) : (
                <>
                  <NavLink className="btn btn-ghost" to="/login">Log in</NavLink>
                  <Link className="btn btn-primary" to="/open-account">Open an account</Link>
                </>
              )}
            </span>
          </nav>
        </div>
      </header>
    </>
  )
}

export function PublicFooter() {
  return (
    <footer className="site-footer">
      <div className="wrap footer-grid">
        <div className="footer-brand">
          <Logo light />
          <p>
            A cloud-based distributed banking and financial transaction platform, built as a
            B.Tech project at the Institute of Engineering &amp; Technology, DAVV, Indore.
          </p>
        </div>
        <div>
          <h4>Banking</h4>
          <Link to="/open-account">Open an account</Link>
          <Link to="/login">Net banking login</Link>
          <a href="/#services">Services</a>
        </div>
        <div>
          <h4>Help</h4>
          <a href="/#security">Security practices</a>
          <a href="/#architecture">How ArthGrid works</a>
          <a href="/#about">Project team</a>
        </div>
        <div className="footer-warning">
          <h4>Stay safe</h4>
          <p>ArthGrid will never ask for your password or transaction PIN by email, phone or SMS.</p>
        </div>
      </div>
      <div className="wrap footer-base">
        <span>© 2026 ArthGrid. Group 13, B.Tech IV Year, IET DAVV.</span>
        <span>Demo application for academic evaluation. Not a licensed bank.</span>
      </div>
    </footer>
  )
}
