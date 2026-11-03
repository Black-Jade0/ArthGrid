import { fmtDateTime, inr } from '../../api.js'

const LABELS = {
  Opening_Balance: 'Opening balance',
  Deposit: 'Credit',
  Withdraw: 'Withdrawal',
  Withdrawal_Held: 'Transfer',
}

export const txnLabel = (t) => LABELS[t.transactionType] || t.transactionType

export default function TxnList({ txns }) {
  if (!txns.length) return <p className="empty">No transactions yet. Money you send or receive will appear here.</p>
  return (
    <ul className="txn-list">
      {txns.map((t) => {
        const credit = Number(t.amount) >= 0
        return (
          <li key={t.id}>
            <span className={'txn-dir ' + (credit ? 'cr' : 'dr')} aria-hidden="true">{credit ? '+' : '−'}</span>
            <span className="txn-text">
              <b>{t.description || txnLabel(t)}</b>
              <small>{fmtDateTime(t.timeStamp)}</small>
            </span>
            <span className={'txn-amt num ' + (credit ? 'cr' : 'dr')}>
              {credit ? '+' : '−'}{inr(Math.abs(t.amount))}
            </span>
          </li>
        )
      })}
    </ul>
  )
}
