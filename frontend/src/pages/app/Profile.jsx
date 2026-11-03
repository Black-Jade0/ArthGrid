import { useEffect, useState } from 'react'
import { api, fmtDate, groupAcc } from '../../api.js'

const show = (v) => (v ? v : <span className="muted">Not provided</span>)

export default function Profile({ user, onUpdate }) {
  const [u, setU] = useState(user)

  // Fetch the latest profile from customer-service (local mode: X-User-Id header instead of a JWT)
  useEffect(() => {
    api.me(user.externalId).then((fresh) => { setU(fresh); onUpdate(fresh) }).catch(() => {})
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.externalId])

  return (
    <>
      <h1 className="page-title">Profile</h1>

      <div className="profile-head panel">
        <span className="avatar lg">{u.firstName?.[0]}{u.lastName?.[0]}</span>
        <div>
          <h2>{u.firstName} {u.lastName}</h2>
          <p className="muted">Customer since {u.memberSince ? fmtDate(u.memberSince) : 'today'}</p>
        </div>
        <span className="badge">KYC {u.kycStatus === 'VERIFIED' ? 'verified' : (u.kycStatus || '').toLowerCase()}</span>
      </div>

      <div className="profile-grid">
        <section className="panel">
          <h2>Personal details</h2>
          <dl className="kv">
            <div><dt>Full name</dt><dd>{u.firstName} {u.lastName}</dd></div>
            <div><dt>Date of birth</dt><dd>{u.dateOfBirth ? fmtDate(u.dateOfBirth) : show()}</dd></div>
            <div><dt>Occupation</dt><dd>{show(u.occupation)}</dd></div>
          </dl>
        </section>

        <section className="panel">
          <h2>Contact details</h2>
          <dl className="kv">
            <div><dt>Email</dt><dd>{u.email}</dd></div>
            <div><dt>Mobile</dt><dd>{u.phone ? '+91 ' + u.phone : show()}</dd></div>
            <div><dt>Address</dt><dd>{show([u.address, u.city, u.state, u.postalCode].filter(Boolean).join(', '))}</dd></div>
          </dl>
        </section>

        <section className="panel">
          <h2>Account</h2>
          <dl className="kv">
            <div><dt>Account number</dt><dd className="num">{groupAcc(u.accountNumber)}</dd></div>
            <div><dt>Account type</dt><dd>Savings account</dd></div>
            <div><dt>Currency</dt><dd>Indian rupee (INR)</dd></div>
            <div><dt>Customer ID</dt><dd className="num ref">{u.externalId}</dd></div>
          </dl>
        </section>

        <section className="panel">
          <h2>Security</h2>
          <dl className="kv">
            <div><dt>Login password</dt><dd>Set at account opening, stored encrypted</dd></div>
            <div><dt>Transaction PIN</dt><dd>4 digits, required for every transfer</dd></div>
            <div><dt>Email alerts</dt><dd>On for every debit and credit</dd></div>
            <div><dt>Automatic logout</dt><dd>After 5 minutes of inactivity</dd></div>
          </dl>
        </section>
      </div>
    </>
  )
}
