import { useState } from 'react'
import { api } from './api.js'

const emptySignup = { firstName: '', lastName: '', email: '', phone: '', address: '', password: '' }

export default function Auth({ onLogin }) {
  const [mode, setMode] = useState('login') // 'login' | 'signup'
  const [login, setLogin] = useState({ email: '', password: '' })
  const [signup, setSignup] = useState(emptySignup)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const switchMode = (m) => { setMode(m); setError('') }

  async function doLogin(e) {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      onLogin(await api.login(login.email.trim(), login.password))
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function doSignup(e) {
    e.preventDefault()
    if (signup.password.length < 6) return setError('Password must be at least 6 characters')
    setBusy(true); setError('')
    try {
      const customer = await api.signup({
        ...signup,
        email: signup.email.trim(),
        // a fresh id per sign-up, so a double-click doesn't create two customers
        externalId: 'web-' + crypto.randomUUID(),
      })
      onLogin(customer)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const s = (field) => ({
    value: signup[field],
    onChange: (e) => setSignup({ ...signup, [field]: e.target.value }),
  })

  return (
    <div className="auth-page">
      <section className="auth-hero">
        <div className="brand brand-lg">Arth<span>Grid</span></div>
        <h1>Send money in seconds.</h1>
        <p>
          Open an account and get <b>₹5,000</b> to start. Send it to anyone registered on ArthGrid —
          both of you get an email the moment it lands.
        </p>
        <ul className="hero-points">
          <li>7 Spring Boot microservices behind one gateway</li>
          <li>Kafka events trigger real email alerts</li>
          <li>Hold → capture flow keeps every transfer safe</li>
        </ul>
        <p className="demo-note">College demo · balances are numbers in our database, no real money moves.</p>
      </section>

      <section className="auth-card">
        <div className="tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => switchMode('login')}>Log in</button>
          <button className={mode === 'signup' ? 'active' : ''} onClick={() => switchMode('signup')}>Create account</button>
        </div>

        {mode === 'login' ? (
          <form onSubmit={doLogin}>
            <label>Email
              <input type="email" required autoFocus value={login.email}
                onChange={(e) => setLogin({ ...login, email: e.target.value })} />
            </label>
            <label>Password
              <input type="password" required value={login.password}
                onChange={(e) => setLogin({ ...login, password: e.target.value })} />
            </label>
            {error && <div className="error">{error}</div>}
            <button className="primary" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
            <p className="switch">New here? <a onClick={() => switchMode('signup')}>Create an account</a></p>
          </form>
        ) : (
          <form onSubmit={doSignup}>
            <div className="row">
              <label>First name<input required autoFocus {...s('firstName')} /></label>
              <label>Last name<input required {...s('lastName')} /></label>
            </div>
            <label>Email <small>(alerts are sent here)</small><input type="email" required {...s('email')} /></label>
            <div className="row">
              <label>Phone<input required inputMode="tel" {...s('phone')} /></label>
              <label>City<input required {...s('address')} /></label>
            </div>
            <label>Password<input type="password" required minLength={6} {...s('password')} /></label>
            {error && <div className="error">{error}</div>}
            <button className="primary" disabled={busy}>
              {busy ? 'Opening your account…' : 'Create account & get ₹5,000'}
            </button>
            <p className="switch">Already have an account? <a onClick={() => switchMode('login')}>Log in</a></p>
          </form>
        )}
      </section>
    </div>
  )
}
