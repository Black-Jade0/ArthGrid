import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api.js'
import { session } from '../session.js'
import { PublicFooter, PublicHeader } from '../components/PublicChrome.jsx'
import { Alert, Field } from '../components/ui.jsx'

export default function Login({ onLogin }) {
  const [reason] = useState(session.takeReason)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [show, setShow] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      onLogin(await api.login(email.trim(), password))
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  return (
    <div className="public">
      <PublicHeader />
      <main className="auth-main">
        <div className="wrap auth-grid">
          <section className="panel auth-panel">
            <h1>Log in to net banking</h1>
            <p className="muted">Use the email address and password you chose when you opened your account.</p>

            <Alert kind="info">{reason}</Alert>

            <form onSubmit={submit} className="form">
              <Field label="Email address">
                <input type="email" required autoFocus autoComplete="username" value={email}
                  onChange={(e) => setEmail(e.target.value)} />
              </Field>
              <Field label="Password">
                <div className="input-with-btn">
                  <input type={show ? 'text' : 'password'} required autoComplete="current-password" value={password}
                    onChange={(e) => setPassword(e.target.value)} />
                  <button type="button" className="input-btn" onClick={() => setShow(!show)}>
                    {show ? 'Hide' : 'Show'}
                  </button>
                </div>
              </Field>
              <Alert>{error}</Alert>
              <button className="btn btn-primary btn-block" disabled={busy}>
                {busy ? 'Logging in…' : 'Log in'}
              </button>
            </form>
            <p className="auth-switch">
              Don't have an account? <Link to="/open-account">Open one in two minutes</Link>
            </p>
          </section>

          <aside className="auth-aside">
            <h2>Before you log in</h2>
            <ul className="tips">
              <li>Check that the address bar shows the ArthGrid site you normally use.</li>
              <li>ArthGrid will never ask for your password or PIN by email or phone.</li>
              <li>Log out when you finish, especially on a shared computer. We also log you out after 5 minutes of inactivity.</li>
              <li>You get an email for every debit and credit. If one looks unfamiliar, log in and check your statement.</li>
            </ul>
          </aside>
        </div>
      </main>
      <PublicFooter />
    </div>
  )
}
