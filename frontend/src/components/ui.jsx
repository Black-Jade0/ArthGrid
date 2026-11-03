import { useRef } from 'react'
import { Link } from 'react-router-dom'

export function Logo({ to = '/', light = false }) {
  return (
    <Link to={to} className={'logo' + (light ? ' logo-light' : '')} aria-label="ArthGrid home">
      <svg viewBox="0 0 32 32" width="30" height="30" aria-hidden="true">
        <rect width="32" height="32" rx="7" fill={light ? '#ffffff' : '#1d1145'} />
        <path d="M9.5 22 16 7l6.5 15M12.2 16.5h7.6" stroke={light ? '#1d1145' : '#ffffff'} strokeWidth="2.6" fill="none" strokeLinecap="round" strokeLinejoin="round" />
        <rect x="7" y="24.5" width="18" height="2.6" rx="1.3" fill="#f5d547" />
      </svg>
      <span>ArthGrid</span>
    </Link>
  )
}

/** Label + input + hint/error, used by every form */
export function Field({ label, optional, hint, error, children, className = '' }) {
  return (
    <label className={'field ' + className}>
      <span className="field-label">
        {label}
        {optional && <em> (optional)</em>}
      </span>
      {children}
      {error ? <span className="field-error">{error}</span> : hint ? <span className="field-hint">{hint}</span> : null}
    </label>
  )
}

/** Four separate boxes for a 4-digit PIN, like a banking app */
export function PinInput({ value, onChange, autoFocus, label = 'PIN' }) {
  const refs = [useRef(), useRef(), useRef(), useRef()]
  const digits = (value + '    ').slice(0, 4).split('')

  const setDigit = (i, d) => {
    const arr = (value + '    ').slice(0, 4).split('')
    arr[i] = d
    onChange(arr.join('').replace(/\s/g, ''))
  }

  return (
    <div className="pin" role="group" aria-label={label}>
      {digits.map((d, i) => (
        <input
          key={i}
          ref={refs[i]}
          type="password"
          inputMode="numeric"
          autoComplete="off"
          maxLength={1}
          aria-label={`${label} digit ${i + 1}`}
          autoFocus={autoFocus && i === 0}
          value={d.trim()}
          onChange={(e) => {
            const v = e.target.value.replace(/\D/g, '').slice(-1)
            if (!v) return
            setDigit(i, v)
            if (i < 3) refs[i + 1].current.focus()
          }}
          onKeyDown={(e) => {
            if (e.key === 'Backspace') {
              e.preventDefault()
              if (digits[i].trim()) setDigit(i, ' ')
              else if (i > 0) { setDigit(i - 1, ' '); refs[i - 1].current.focus() }
            }
          }}
          onPaste={(e) => {
            const p = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 4)
            if (p) { e.preventDefault(); onChange(p); refs[Math.min(p.length, 3)].current.focus() }
          }}
        />
      ))}
    </div>
  )
}

/** The debit card illustration used on the home page and overview */
export function BankCard({ name = 'Your Name', number = '', compact = false }) {
  const last = number ? number.slice(-4) : '0000'
  return (
    <div className={'bank-card' + (compact ? ' compact' : '')} aria-label="ArthGrid debit card">
      <div className="bank-card-stripe" />
      <div className="bank-card-top">
        <span className="bank-card-brand">ArthGrid</span>
        <span className="bank-card-type">Savings</span>
      </div>
      <div className="bank-card-chip" />
      <div className="bank-card-number">XXXX XXXX XXXX {last}</div>
      <div className="bank-card-bottom">
        <span>{name}</span>
        <span>Debit</span>
      </div>
    </div>
  )
}

export function Alert({ kind = 'error', children }) {
  if (!children) return null
  return <div className={'alert alert-' + kind} role={kind === 'error' ? 'alert' : 'status'}>{children}</div>
}
