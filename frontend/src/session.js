// Logged-in user lives in sessionStorage, which is per browser TAB.
// So for a class demo you can be user A in one tab and user B in another.
const KEY = 'arthgrid-user'
const REASON = 'arthgrid-logout-reason'

export const IDLE_LIMIT_MS = 5 * 60 * 1000 // automatic logout after 5 minutes without activity

function safe(fn, fallback = null) {
  try { return fn() } catch { return fallback }
}

export const session = {
  get: () => safe(() => JSON.parse(sessionStorage.getItem(KEY))),
  set: (u) => safe(() => sessionStorage.setItem(KEY, JSON.stringify(u))),
  clear: (reason) =>
    safe(() => {
      sessionStorage.removeItem(KEY)
      if (reason) sessionStorage.setItem(REASON, reason)
    }),
  takeReason: () =>
    safe(() => {
      const r = sessionStorage.getItem(REASON)
      sessionStorage.removeItem(REASON)
      return r
    }),
}
