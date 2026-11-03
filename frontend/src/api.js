// All calls go to the API Gateway, which forwards them to the right microservice.
// Where the gateway is, in order of priority:
//   1. ?api=https://something.trycloudflare.com in the page URL (saved in this browser for next time)
//   2. VITE_API_URL set when building (e.g. in Vercel's environment variables)
//   3. http://localhost:9090 (backend running on this same computer)
function resolveApi() {
  try {
    const fromUrl = new URLSearchParams(window.location.search).get('api')
    if (fromUrl) localStorage.setItem('arthgrid-api', fromUrl.replace(/\/+$/, ''))
    const saved = localStorage.getItem('arthgrid-api')
    if (saved) return saved
  } catch { /* storage blocked: fall through */ }
  return import.meta.env.VITE_API_URL || 'http://localhost:9090'
}
const API = resolveApi()

async function call(path, options = {}) {
  let res
  try {
    res = await fetch(API + path, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    })
  } catch {
    throw new Error('We could not reach the bank servers. Check that the backend is running (docker compose up).')
  }
  const text = await res.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { data = text }
  if (!res.ok) {
    let msg = (data && (data.message || data.error)) || `Request failed (${res.status})`
    if (/Validation failed/i.test(msg)) msg = 'Some details are not valid. Please check the form and try again.'
    throw new Error(msg)
  }
  return data
}

export const api = {
  // customer-service
  openAccount: (body) =>
    call('/customer-service/api/v1/customers', { method: 'POST', body: JSON.stringify(body) }),
  login: (email, password) =>
    call('/customer-service/api/v1/customers/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  me: (userId) =>
    call('/customer-service/api/v1/customers/me', { headers: { 'X-User-Id': userId } }),
  lookup: (email) =>
    call('/customer-service/api/v1/customers/lookup?email=' + encodeURIComponent(email)),
  verifyPin: (email, pin) =>
    call('/customer-service/api/v1/customers/verify-pin', { method: 'POST', body: JSON.stringify({ email, pin }) }),

  // accounts-service
  account: (acc) => call(`/accounts-service/api/v1/accounts/${acc}`),
  transactions: (acc, page = 0, size = 10) =>
    call(`/accounts-service/api/v1/accounts/${acc}/transactions?page=${page}&size=${size}`),

  // payment-service
  transfer: (body) =>
    call('/payment-service/api/v1/payments/transfer', { method: 'POST', body: JSON.stringify(body) }),
}

export const inr = (n) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', minimumFractionDigits: 2 }).format(Number(n || 0))

// The backend stores times in UTC without a zone marker, so add one before converting to local time
export const toDate = (d) => (d ? new Date(/Z|[+-]\d\d:\d\d$/.test(d) ? d : d + 'Z') : null)

export const fmtDateTime = (d) =>
  d ? toDate(d).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : ''

export const fmtDate = (d) =>
  d ? toDate(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : ''

export const groupAcc = (a = '') => a.replace(/(\d{4})(\d{3})(\d+)/, '$1 $2 $3')
export const maskAcc = (a = '') => 'XXXX XXX ' + a.slice(-3)
