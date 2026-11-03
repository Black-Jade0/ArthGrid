import { useEffect, useState } from 'react'
import { api, fmtDateTime, groupAcc, inr } from '../../api.js'
import { Alert } from '../../components/ui.jsx'
import { txnLabel } from './TxnList.jsx'

const PAGE_SIZE = 10
const FILTERS = [['all', 'All'], ['in', 'Money in'], ['out', 'Money out']]

export default function Statement({ user }) {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [filter, setFilter] = useState('all')
  const [error, setError] = useState('')

  useEffect(() => {
    let live = true
    api.transactions(user.accountNumber, page, PAGE_SIZE)
      .then((d) => { if (live) { setData(d); setError('') } })
      .catch((e) => live && setError(e.message))
    return () => { live = false }
  }, [user.accountNumber, page])

  const rows = (data?.content || []).filter((t) =>
    filter === 'all' ? true : filter === 'in' ? Number(t.amount) >= 0 : Number(t.amount) < 0)
  const totalPages = data?.totalPages || 1

  return (
    <>
      <div className="page-head">
        <h1 className="page-title">Account statement</h1>
        <button className="btn btn-ghost btn-sm no-print" onClick={() => window.print()}>Print statement</button>
      </div>
      <p className="muted statement-sub">
        {user.firstName} {user.lastName}, savings account <span className="num">{groupAcc(user.accountNumber)}</span>
        {data && <>, {data.totalElements} transaction{data.totalElements === 1 ? '' : 's'} in total</>}
      </p>
      <Alert>{error}</Alert>

      <section className="panel">
        <div className="segmented no-print" role="tablist" aria-label="Filter transactions">
          {FILTERS.map(([k, label]) => (
            <button key={k} role="tab" aria-selected={filter === k} className={filter === k ? 'on' : ''} onClick={() => setFilter(k)}>
              {label}
            </button>
          ))}
        </div>

        <div className="table-wrap">
          <table className="statement">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Type</th>
                <th>Reference</th>
                <th className="r">Debit</th>
                <th className="r">Credit</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((t) => {
                const credit = Number(t.amount) >= 0
                return (
                  <tr key={t.id}>
                    <td className="nowrap">{fmtDateTime(t.timeStamp)}</td>
                    <td>{t.description}</td>
                    <td>{txnLabel(t)}</td>
                    <td className="num ref" title={t.referenceId}>{t.referenceId?.slice(0, 8).toUpperCase()}</td>
                    <td className="r num dr">{credit ? '' : inr(Math.abs(t.amount))}</td>
                    <td className="r num cr">{credit ? inr(t.amount) : ''}</td>
                  </tr>
                )
              })}
              {data && rows.length === 0 && (
                <tr><td colSpan={6} className="empty">No transactions to show on this page.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="pager no-print">
          <button className="btn btn-ghost btn-sm" disabled={page === 0} onClick={() => setPage(page - 1)}>Newer</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button className="btn btn-ghost btn-sm" disabled={page + 1 >= totalPages} onClick={() => setPage(page + 1)}>Older</button>
        </div>
      </section>
    </>
  )
}
