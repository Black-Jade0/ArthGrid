import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, groupAcc } from '../api.js'
import { PublicFooter, PublicHeader } from '../components/PublicChrome.jsx'
import { Alert, Field, PinInput } from '../components/ui.jsx'

const STATES = [
  'Andhra Pradesh', 'Assam', 'Bihar', 'Chhattisgarh', 'Delhi', 'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh',
  'Jharkhand', 'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Odisha', 'Punjab', 'Rajasthan',
  'Tamil Nadu', 'Telangana', 'Uttar Pradesh', 'Uttarakhand', 'West Bengal', 'Other',
]
const OCCUPATIONS = ['Student', 'Salaried', 'Self-employed', 'Business owner', 'Homemaker', 'Retired', 'Other']
const STEPS = ['Personal details', 'Contact and address', 'Security', 'Review']

const empty = {
  firstName: '', lastName: '', dateOfBirth: '', occupation: '',
  email: '', phone: '', address: '', city: '', state: '', postalCode: '',
  password: '', confirmPassword: '', pin: '', confirmPin: '',
}

export default function OpenAccount({ onLogin }) {
  const [step, setStep] = useState(0)
  const [f, setF] = useState(empty)
  const [errors, setErrors] = useState({})
  const [agree, setAgree] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [created, setCreated] = useState(null)

  const bind = (k) => ({ value: f[k], onChange: (e) => setF({ ...f, [k]: e.target.value }) })

  function validate(s) {
    const e = {}
    if (s === 0) {
      if (!f.firstName.trim()) e.firstName = 'Enter your first name'
      if (!f.lastName.trim()) e.lastName = 'Enter your last name'
      if (f.dateOfBirth && new Date(f.dateOfBirth) > new Date()) e.dateOfBirth = 'Date of birth cannot be in the future'
    }
    if (s === 1) {
      if (!/^\S+@\S+\.\S+$/.test(f.email.trim())) e.email = 'Enter a valid email address'
      if (f.phone && !/^[6-9]\d{9}$/.test(f.phone.replace(/\s/g, ''))) e.phone = 'Enter a 10-digit mobile number'
      if (f.postalCode && !/^\d{6}$/.test(f.postalCode)) e.postalCode = 'PIN code has 6 digits'
    }
    if (s === 2) {
      if (f.password.length < 8) e.password = 'Use at least 8 characters'
      else if (!/[A-Za-z]/.test(f.password) || !/\d/.test(f.password)) e.password = 'Use both letters and numbers'
      if (f.confirmPassword !== f.password) e.confirmPassword = 'Passwords do not match'
      if (!/^\d{4}$/.test(f.pin)) e.pin = 'Enter 4 digits'
      else if (/^(\d)\1{3}$/.test(f.pin) || ['1234', '4321', '0123'].includes(f.pin)) e.pin = 'Choose a PIN that is harder to guess'
      if (f.confirmPin !== f.pin) e.confirmPin = 'PINs do not match'
    }
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const next = (e) => {
    e.preventDefault()
    if (validate(step)) setStep(step + 1)
  }

  async function submit(e) {
    e.preventDefault()
    if (!agree) return setError('Please accept the terms to open your account.')
    setBusy(true)
    setError('')
    try {
      const customer = await api.openAccount({
        firstName: f.firstName.trim(),
        lastName: f.lastName.trim(),
        email: f.email.trim(),
        phone: f.phone.replace(/\s/g, ''),
        address: f.address.trim(),
        city: f.city.trim(),
        state: f.state,
        postalCode: f.postalCode,
        dateOfBirth: f.dateOfBirth,
        occupation: f.occupation,
        password: f.password,
        pin: f.pin,
        // a fresh id per sign-up, so a double-click doesn't create two customers
        externalId: 'web-' + crypto.randomUUID(),
      })
      setCreated(customer)
    } catch (err) {
      setError(err.message)
      if (/email/i.test(err.message)) setStep(1)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="public">
      <PublicHeader />
      <main className="auth-main">
        <div className="wrap narrow">
          {created ? (
            <section className="panel success-panel">
              <div className="success-mark" aria-hidden="true">✓</div>
              <h1>Your account is open</h1>
              <p className="muted">
                Welcome to ArthGrid, {created.firstName}. We have sent a welcome email to <b>{created.email}</b>.
              </p>
              <dl className="kv">
                <div><dt>Account holder</dt><dd>{created.firstName} {created.lastName}</dd></div>
                <div><dt>Account number</dt><dd className="num">{groupAcc(created.accountNumber)}</dd></div>
                <div><dt>Account type</dt><dd>Savings account</dd></div>
                <div><dt>Opening balance</dt><dd className="num">₹5,000.00</dd></div>
              </dl>
              <button className="btn btn-primary btn-block" onClick={() => onLogin(created)}>Go to net banking</button>
            </section>
          ) : (
            <section className="panel">
              <h1>Open a savings account</h1>
              <p className="muted">Fields marked optional can be left empty.</p>

              <ol className="stepper" aria-label="Progress">
                {STEPS.map((s, i) => (
                  <li key={s} className={i === step ? 'current' : i < step ? 'done' : ''} aria-current={i === step ? 'step' : undefined}>
                    <span className="stepper-num">{i < step ? '✓' : i + 1}</span>
                    <span className="stepper-label">{s}</span>
                  </li>
                ))}
              </ol>

              {step === 0 && (
                <form onSubmit={next} className="form" noValidate>
                  <div className="form-row">
                    <Field label="First name" error={errors.firstName}><input autoFocus autoComplete="given-name" {...bind('firstName')} /></Field>
                    <Field label="Last name" error={errors.lastName}><input autoComplete="family-name" {...bind('lastName')} /></Field>
                  </div>
                  <div className="form-row">
                    <Field label="Date of birth" optional error={errors.dateOfBirth}><input type="date" {...bind('dateOfBirth')} /></Field>
                    <Field label="Occupation" optional>
                      <select {...bind('occupation')}>
                        <option value="">Select</option>
                        {OCCUPATIONS.map((o) => <option key={o}>{o}</option>)}
                      </select>
                    </Field>
                  </div>
                  <div className="form-actions">
                    <Link to="/" className="btn btn-ghost">Cancel</Link>
                    <button className="btn btn-primary">Continue</button>
                  </div>
                </form>
              )}

              {step === 1 && (
                <form onSubmit={next} className="form" noValidate>
                  <Field label="Email address" hint="Transaction alerts are sent here, and you log in with it." error={errors.email}>
                    <input type="email" autoFocus autoComplete="email" {...bind('email')} />
                  </Field>
                  <Field label="Mobile number" optional error={errors.phone}>
                    <div className="input-prefix"><span>+91</span><input inputMode="numeric" autoComplete="tel-national" maxLength={10} {...bind('phone')} /></div>
                  </Field>
                  <Field label="Address" optional><input autoComplete="street-address" {...bind('address')} /></Field>
                  <div className="form-row three">
                    <Field label="City" optional><input autoComplete="address-level2" {...bind('city')} /></Field>
                    <Field label="State" optional>
                      <select {...bind('state')}>
                        <option value="">Select</option>
                        {STATES.map((s) => <option key={s}>{s}</option>)}
                      </select>
                    </Field>
                    <Field label="PIN code" optional error={errors.postalCode}><input inputMode="numeric" maxLength={6} autoComplete="postal-code" {...bind('postalCode')} /></Field>
                  </div>
                  <Alert>{error}</Alert>
                  <div className="form-actions">
                    <button type="button" className="btn btn-ghost" onClick={() => setStep(0)}>Back</button>
                    <button className="btn btn-primary">Continue</button>
                  </div>
                </form>
              )}

              {step === 2 && (
                <form onSubmit={next} className="form" noValidate>
                  <h2 className="form-section">Net banking password</h2>
                  <div className="form-row">
                    <Field label="Password" hint="At least 8 characters, with letters and numbers." error={errors.password}>
                      <input type="password" autoFocus autoComplete="new-password" {...bind('password')} />
                    </Field>
                    <Field label="Confirm password" error={errors.confirmPassword}>
                      <input type="password" autoComplete="new-password" {...bind('confirmPassword')} />
                    </Field>
                  </div>
                  <h2 className="form-section">Transaction PIN</h2>
                  <p className="muted small">You will enter this 4-digit PIN to approve every transfer. Do not share it with anyone.</p>
                  <div className="form-row">
                    <Field label="PIN" error={errors.pin}>
                      <PinInput label="PIN" value={f.pin} onChange={(v) => setF((x) => ({ ...x, pin: v }))} />
                    </Field>
                    <Field label="Confirm PIN" error={errors.confirmPin}>
                      <PinInput label="Confirm PIN" value={f.confirmPin} onChange={(v) => setF((x) => ({ ...x, confirmPin: v }))} />
                    </Field>
                  </div>
                  <div className="form-actions">
                    <button type="button" className="btn btn-ghost" onClick={() => setStep(1)}>Back</button>
                    <button className="btn btn-primary">Continue</button>
                  </div>
                </form>
              )}

              {step === 3 && (
                <form onSubmit={submit} className="form">
                  <dl className="kv">
                    <div><dt>Name</dt><dd>{f.firstName} {f.lastName}</dd></div>
                    <div><dt>Email</dt><dd>{f.email}</dd></div>
                    <div><dt>Mobile</dt><dd>{f.phone ? '+91 ' + f.phone : 'Not provided'}</dd></div>
                    <div><dt>Date of birth</dt><dd>{f.dateOfBirth || 'Not provided'}</dd></div>
                    <div><dt>Occupation</dt><dd>{f.occupation || 'Not provided'}</dd></div>
                    <div><dt>Address</dt><dd>{[f.address, f.city, f.state, f.postalCode].filter(Boolean).join(', ') || 'Not provided'}</dd></div>
                    <div><dt>Account type</dt><dd>Savings account</dd></div>
                  </dl>
                  <label className="check">
                    <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
                    <span>
                      I understand this is an academic demo: balances are simulated and no real money is involved.
                      I agree to receive transaction alerts at the email address above.
                    </span>
                  </label>
                  <Alert>{error}</Alert>
                  <div className="form-actions">
                    <button type="button" className="btn btn-ghost" onClick={() => setStep(2)} disabled={busy}>Back</button>
                    <button className="btn btn-primary" disabled={busy}>{busy ? 'Opening your account…' : 'Open account'}</button>
                  </div>
                </form>
              )}

              <p className="auth-switch">Already have an account? <Link to="/login">Log in</Link></p>
            </section>
          )}
        </div>
      </main>
      <PublicFooter />
    </div>
  )
}
