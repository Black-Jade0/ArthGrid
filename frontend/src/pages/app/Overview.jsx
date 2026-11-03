import { Link } from 'react-router-dom'
import { groupAcc, inr } from '../../api.js'
import { Alert, BankCard } from '../../components/ui.jsx'
import { useAccount } from './useAccount.js'
import TxnList from './TxnList.jsx'

export default function Overview({ user }) {
  const { account, txns, error } = useAccount(user.accountNumber, 6)
  const held = account ? Number(account.currentBalance) - Number(account.availableBalance) : 0

  return (
    <>
      <h1 className="page-title">Overview</h1>
      <Alert>{error}</Alert>

      <section className="summary">
        <div className="summary-balance">
          <span className="summary-label">Available balance</span>
          <span className="summary-amount num">{account ? inr(account.availableBalance) : '—'}</span>
          <dl className="summary-meta">
            <div><dt>Ledger balance</dt><dd className="num">{account ? inr(account.currentBalance) : '—'}</dd></div>
            <div><dt>On hold</dt><dd className="num">{inr(held)}</dd></div>
            <div><dt>Account number</dt><dd className="num">{groupAcc(user.accountNumber)}</dd></div>
            <div><dt>Status</dt><dd><span className="badge">{account?.status === 'ACTIVE' ? 'Active' : account?.status || '…'}</span></dd></div>
          </dl>
          <div className="summary-actions">
            <Link className="btn btn-accent" to="/banking/send">Send money</Link>
            <Link className="btn btn-outline-light" to="/banking/statement">View statement</Link>
          </div>
        </div>
        <div className="summary-card">
          <BankCard name={`${user.firstName} ${user.lastName}`} number={user.accountNumber} compact />
        </div>
      </section>

      <section className="panel">
        <div className="panel-head">
          <h2>Recent transactions</h2>
          <Link to="/banking/statement">See all</Link>
        </div>
        <TxnList txns={txns} />
      </section>
    </>
  )
}
