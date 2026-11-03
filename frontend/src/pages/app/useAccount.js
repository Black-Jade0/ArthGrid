import { useCallback, useEffect, useState } from 'react'
import { api } from '../../api.js'

/** Loads balance + recent transactions, and refreshes every 5 s so incoming money shows up live. */
export function useAccount(accountNumber, size = 5) {
  const [account, setAccount] = useState(null)
  const [txns, setTxns] = useState([])
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    try {
      const [acc, page] = await Promise.all([api.account(accountNumber), api.transactions(accountNumber, 0, size)])
      setAccount(acc)
      setTxns(page?.content || [])
      setError('')
    } catch (err) {
      setError(err.message)
    }
  }, [accountNumber, size])

  useEffect(() => {
    refresh()
    const t = setInterval(refresh, 5000)
    return () => clearInterval(t)
  }, [refresh])

  return { account, txns, error, refresh }
}
