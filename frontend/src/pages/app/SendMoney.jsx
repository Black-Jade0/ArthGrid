import { useState } from 'react'
import { Link } from 'react-router-dom'
import { api, fmtDateTime, inr, maskAcc } from '../../api.js'
import { Alert, Field, PinInput } from '../../components/ui.jsx'
import { useAccount } from './useAccount.js'

const MAX_PIN_TRIES = 3

function friendly(reason = '') {
  if (/insufficient/i.test(reason)) return 'Your available balance is too low for this transfer.'
  if (/own account/i.test(reason)) return 'You cannot send money to your own account.'
  return reason || 'The transfer could not be completed. No money has left your account.'
}

export default function SendMoney({ user }) {
  const { account, refresh } = useAccount(user.accountNumber, 1)
  const [step, setStep] = useState('details') // details -> review -> pin -> done
  const [email, setEmail] = useState('')
  const [amount, setAmount] = useState('')
  const [remark, setRemark] = useState('')
  const [payee, setPayee] = useState(null)
  const [pin, setPin] = useState('')
  const [tries, setTries] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [receipt, setReceipt] = useState(null)

  const amt = Number(amount)
  const senderName = `${user.firstName} ${user.lastName}`

  function restart() {
    setStep('details'); setEmail(''); setAmount(''); setRemark(''); setPayee(null)
    setPin(''); setTries(0); setError(''); setReceipt(null)
  }

  async function toReview(e) {
    e.preventDefault()
    setError('')
    if (!(amt > 0)) return setError('Enter an amount greater than ₹0.')
    if (Math.round(amt * 100) !== amt * 100) return setError('Amount can have at most 2 decimal places.')
    if (email.trim().toLowerCase() === user.email.toLowerCase()) return setError('You cannot send money to your own account.')
    if (account && amt > Number(account.availableBalance)) {
      return setError(`Your available balance is ${inr(account.availableBalance)}. Enter a smaller amount.`)
    }
    setBusy(true)
    try {
      const p = await api.lookup(email.trim())
      if (!p.accountNumber) throw new Error('This customer does not have an active account yet.')
      setPayee(p)
      setStep('review')
    } catch (err) {
      setError(/No ArthGrid user/i.test(err.message) ? 'No ArthGrid account uses this email address. Check it and try again.' : err.message)
    } finally {
      setBusy(false)
    }
  }

  async function confirm(e) {
    e.preventDefault()
    if (pin.length !== 4) return setError('Enter your 4-digit transaction PIN.')
    setBusy(true)
    setError('')
    try {
      await api.verifyPin(user.email, pin)
    } catch (err) {
      const n = tries + 1
      setTries(n)
      setPin('')
      setBusy(false)
      if (/Incorrect PIN/i.test(err.message)) {
        if (n >= MAX_PIN_TRIES) {
          setStep('details')
          return setError('Too many incorrect PIN attempts. The transfer was cancelled and no money was sent.')
        }
        return setError(`Incorrect PIN. ${MAX_PIN_TRIES - n} attempt${MAX_PIN_TRIES - n === 1 ? '' : 's'} left.`)
      }
      return setError(err.message)
    }

    const payeeName = `${payee.firstName} ${payee.lastName}`
    try {
      const p = await api.transfer({
        sourceAccountNumber: user.accountNumber,
        targetAccountNumber: payee.accountNumber,
        amount: amt,
        billerName: 'Transfer to ' + payeeName,
        billReferenceNumber: remark.trim() || null,
        email: user.email,
        targetEmail: payee.email,
        senderName,
        targetName: payeeName,
      })
      if (p.status !== 'SUCCESS') throw new Error(friendly(p.reason))
      setReceipt(p)
      setStep('done')
      refresh()
    } catch (err) {
      setError(err.message)
      setStep('review')
      setPin('')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <h1 className="page-title">Send money</h1>

      <div className="send-grid">
        <section className="panel">
          {step !== 'done' && (
            <ol className="stepper compact" aria-label="Progress">
              {['Details', 'Review', 'Authorise'].map((s, i) => {
                const cur = ['details', 'review', 'pin'].indexOf(step)
                return (
                  <li key={s} className={i === cur ? 'current' : i < cur ? 'done' : ''}>
                    <span className="stepper-num">{i < cur ? '✓' : i + 1}</span>
                    <span className="stepper-label">{s}</span>
                  </li>
                )
              })}
            </ol>
          )}

          {step === 'details' && (
            <form className="form" onSubmit={toReview} noValidate>
              <Field label="Recipient's email address" hint="The email they used to open their ArthGrid account.">
                <input type="email" autoFocus required value={email} onChange={(e) => setEmail(e.target.value)} />
              </Field>
              <Field label="Amount">
                <div className="input-prefix"><span>₹</span>
                  <input type="number" inputMode="decimal" min="1" step="0.01" required value={amount}
                    onChange={(e) => setAmount(e.target.value)} />
                </div>
              </Field>
              <div className="chips">
                {[500, 1000, 2000].map((v) => (
                  <button type="button" key={v} className="chip" onClick={() => setAmount(String(v))}>{inr(v).replace('.00', '')}</button>
                ))}
              </div>
              <Field label="Remark" optional hint="Shown to you in the payment record, for example ‘Rent’.">
                <input maxLength={40} value={remark} onChange={(e) => setRemark(e.target.value)} />
              </Field>
              <Alert>{error}</Alert>
              <div className="form-actions">
                <button className="btn btn-primary" disabled={busy}>{busy ? 'Checking recipient…' : 'Continue'}</button>
              </div>
            </form>
          )}

          {step === 'review' && payee && (
            <div className="form">
              <p className="muted">Check the details carefully. Transfers cannot be reversed once sent.</p>
              <dl className="kv">
                <div><dt>From</dt><dd>{senderName}<small>Savings {maskAcc(user.accountNumber)}</small></dd></div>
                <div><dt>To</dt><dd>{payee.firstName} {payee.lastName}<small>{payee.email}, account {maskAcc(payee.accountNumber)}</small></dd></div>
                <div><dt>Amount</dt><dd className="num big">{inr(amt)}</dd></div>
                {remark && <div><dt>Remark</dt><dd>{remark}</dd></div>}
                <div><dt>Charges</dt><dd className="num">₹0.00</dd></div>
              </dl>
              <Alert>{error}</Alert>
              <div className="form-actions">
                <button className="btn btn-ghost" onClick={() => { setError(''); setStep('details') }}>Edit details</button>
                <button className="btn btn-primary" onClick={() => { setError(''); setStep('pin') }}>Confirm and continue</button>
              </div>
            </div>
          )}

          {step === 'pin' && (
            <form className="form pin-step" onSubmit={confirm}>
              <h2>Enter your transaction PIN</h2>
              <p className="muted">To send <b className="num">{inr(amt)}</b> to {payee.firstName} {payee.lastName}.</p>
              <PinInput label="Transaction PIN" value={pin} onChange={setPin} autoFocus />
              <Alert>{error}</Alert>
              <div className="form-actions">
                <button type="button" className="btn btn-ghost" onClick={() => { setError(''); setPin(''); setStep('review') }} disabled={busy}>Back</button>
                <button className="btn btn-primary" disabled={busy || pin.length !== 4}>{busy ? 'Sending…' : `Send ${inr(amt)}`}</button>
              </div>
            </form>
          )}

          {step === 'done' && receipt && (
            <div className="receipt" id="receipt">
              <div className="success-mark" aria-hidden="true">✓</div>
              <h2>Transfer successful</h2>
              <p className="receipt-amount num">{inr(receipt.amount)}</p>
              <dl className="kv">
                <div><dt>Paid to</dt><dd>{payee.firstName} {payee.lastName}<small>{payee.email}</small></dd></div>
                <div><dt>From account</dt><dd className="num">{maskAcc(user.accountNumber)}</dd></div>
                <div><dt>Date and time</dt><dd>{fmtDateTime(receipt.createdAt) || new Date().toLocaleString('en-IN')}</dd></div>
                <div><dt>Reference number</dt><dd className="num ref">{receipt.transactionReferenceId}</dd></div>
                {remark && <div><dt>Remark</dt><dd>{remark}</dd></div>}
              </dl>
              <p className="muted small">Email alerts have been sent to you and to {payee.email}.</p>
              <div className="form-actions no-print">
                <button className="btn btn-ghost" onClick={() => window.print()}>Print receipt</button>
                <button className="btn btn-primary" onClick={restart}>Make another transfer</button>
              </div>
            </div>
          )}
        </section>

        <aside className="side-note">
          <div className="side-balance">
            <span>Available balance</span>
            <b className="num">{account ? inr(account.availableBalance) : '—'}</b>
          </div>
          <h3>Good to know</h3>
          <ul className="tips">
            <li>You can send money to any ArthGrid customer using their email address.</li>
            <li>Transfers are instant and free.</li>
            <li>After 3 incorrect PIN attempts the transfer is cancelled.</li>
          </ul>
          <Link to="/banking/statement">View your statement</Link>
        </aside>
      </div>
    </>
  )
}
