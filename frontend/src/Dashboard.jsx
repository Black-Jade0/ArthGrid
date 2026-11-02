import { useCallback, useEffect, useState } from 'react'
import { api, inr } from './api.js'

export default function Dashboard({ user, onLogout }) {
  const [account, setAccount] = useState(null)
  const [txns, setTxns] = useState([])
  const [loadError, setLoadError] = useState('')

  // Load balance + statement. Runs on open and every 5 s, so the receiver sees money arrive live.
  const refresh = useCallback(async () => {
    try {
      const [acc, page] = await Promise.all([
        api.account(user.accountNumber),
        api.transactions(user.accountNumber),
      ])
      setAccount(acc)
      setTxns(page?.content || [])
      setLoadError('')
    } catch (err) {
      setLoadError(err.message)
    }
  }, [user.accountNumber])

  useEffect(() => {
    refresh()
    const t = setInterval(refresh, 5000)
    return () => clearInterval(t)
  }, [refresh])

  const fullName = `${user.firstName} ${user.lastName}`

  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">Arth<span>Grid</span></div>
        <div className="who">
          <div className="avatar">{user.firstName?.[0]}{user.lastName?.[0]}</div>
          <div className="who-text">
            <b>{fullName}</b>
            <small>{user.email}</small>
          </div>
          <button className="ghost" onClick={onLogout}>Log out</button>
        </div>
      </header>

      {loadError && <div className="error banner">{loadError}</div>}

      <main className="grid">
        <section className="balance-card">
          <div className="label">Available balance</div>
          <div className="amount">{account ? inr(account.availableBalance) : '—'}</div>
          <div className="acc-row">
            <div>
              <div className="label">Account number</div>
              <div className="mono">{formatAcc(user.accountNumber)}</div>
            </div>
            <div>
              <div className="label">Status</div>
              <div className="pill">{account?.status || '…'}</div>
            </div>
          </div>
        </section>

        <SendMoney user={user} account={account} onSent={refresh} />

        <section className="card history">
          <div className="card-head">
            <h2>Recent activity</h2>
            <button className="ghost small" onClick={refresh}>Refresh</button>
          </div>
          {txns.length === 0 ? (
            <p className="muted">No transactions yet.</p>
          ) : (
            <ul className="txns">
              {txns.map((t) => <Txn key={t.id} t={t} />)}
            </ul>
          )}
        </section>
      </main>
    </div>
  )
}

function SendMoney({ user, account, onSent }) {
  const [email, setEmail] = useState('')
  const [amount, setAmount] = useState('')
  const [recipient, setRecipient] = useState(null)
  const [step, setStep] = useState('form') // form -> confirm -> done
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  const reset = () => {
    setEmail(''); setAmount(''); setRecipient(null); setStep('form'); setError(''); setResult(null)
  }

  async function review(e) {
    e.preventDefault()
    setError('')
    const amt = Number(amount)
    if (!(amt > 0)) return setError('Enter an amount more than ₹0')
    if (email.trim().toLowerCase() === user.email.toLowerCase()) return setError('You cannot send money to yourself')
    if (account && amt > Number(account.availableBalance)) {
      return setError(`Not enough balance. You have ${inr(account.availableBalance)}.`)
    }
    setBusy(true)
    try {
      const r = await api.lookup(email.trim())
      if (!r.accountNumber) throw new Error('That user has no account yet')
      setRecipient(r)
      setStep('confirm')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function send() {
    setBusy(true); setError('')
    const senderName = `${user.firstName} ${user.lastName}`
    const targetName = `${recipient.firstName} ${recipient.lastName}`
    try {
      const p = await api.transfer({
        sourceAccountNumber: user.accountNumber,
        targetAccountNumber: recipient.accountNumber,
        amount: Number(amount),
        billerName: 'Transfer to ' + targetName,
        email: user.email,
        targetEmail: recipient.email,
        senderName,
        targetName,
      })
      if (p.status !== 'SUCCESS') throw new Error(friendlyReason(p.reason))
      setResult(p)
      setStep('done')
      onSent()
    } catch (err) {
      setError(err.message)
      setStep('form')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="card send">
      <h2>Send money</h2>

      {step === 'form' && (
        <form onSubmit={review}>
          <label>Recipient's email
            <input type="email" required placeholder="friend@gmail.com" value={email}
              onChange={(e) => setEmail(e.target.value)} />
          </label>
          <label>Amount (₹)
            <input type="number" required min="1" step="0.01" placeholder="500" value={amount}
              onChange={(e) => setAmount(e.target.value)} />
          </label>
          <div className="quick">
            {[100, 500, 1000, 2000].map((v) => (
              <button type="button" key={v} onClick={() => setAmount(String(v))}>₹{v}</button>
            ))}
          </div>
          {error && <div className="error">{error}</div>}
          <button className="primary" disabled={busy}>{busy ? 'Finding user…' : 'Continue'}</button>
        </form>
      )}

      {step === 'confirm' && recipient && (
        <div className="confirm">
          <div className="confirm-amount">{inr(amount)}</div>
          <div className="muted">to</div>
          <div className="recipient">
            <div className="avatar">{recipient.firstName?.[0]}{recipient.lastName?.[0]}</div>
            <div>
              <b>{recipient.firstName} {recipient.lastName}</b>
              <small>{recipient.email} · A/c {maskAcc(recipient.accountNumber)}</small>
            </div>
          </div>
          {error && <div className="error">{error}</div>}
          <div className="btn-row">
            <button className="ghost" onClick={() => setStep('form')} disabled={busy}>Back</button>
            <button className="primary" onClick={send} disabled={busy}>{busy ? 'Sending…' : 'Send now'}</button>
          </div>
        </div>
      )}

      {step === 'done' && result && (
        <div className="done">
          <div className="tick">✓</div>
          <h3>{inr(result.amount)} sent</h3>
          <p className="muted">
            to {recipient.firstName} {recipient.lastName}. An email is on its way to you and to {recipient.email}.
          </p>
          <p className="mono small">Ref: {result.transactionReferenceId}</p>
          <button className="primary" onClick={reset}>Send again</button>
        </div>
      )}
    </section>
  )
}

function Txn({ t }) {
  const amt = Number(t.amount)
  const credit = amt >= 0
  return (
    <li>
      <div className={'txn-icon ' + (credit ? 'in' : 'out')}>{credit ? '↓' : '↑'}</div>
      <div className="txn-main">
        <b>{t.description || t.transactionType}</b>
        <small>{formatDate(t.timeStamp)}</small>
      </div>
      <div className={'txn-amt ' + (credit ? 'in' : 'out')}>{credit ? '+' : '−'}{inr(Math.abs(amt))}</div>
    </li>
  )
}

function friendlyReason(reason = '') {
  if (/insufficient/i.test(reason)) return 'Not enough balance for this transfer.'
  if (/own account/i.test(reason)) return 'You cannot send money to yourself.'
  return reason || 'Transfer failed. Please try again.'
}

const formatAcc = (a = '') => a.replace(/(\d{4})(\d{3})(\d+)/, '$1 $2 $3')
const maskAcc = (a = '') => '••••' + a.slice(-4)
// The backend (in Docker) stores times in UTC without a zone, so mark them as UTC before showing local time
const formatDate = (d) =>
  d ? new Date(/Z|[+-]\d\d:\d\d$/.test(d) ? d : d + 'Z').toLocaleString('en-IN', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''
