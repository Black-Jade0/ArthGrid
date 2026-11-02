// All calls go to the API Gateway, which forwards them to the right microservice.
// Change with a VITE_API_URL env var if your gateway runs somewhere else.
const API = import.meta.env.VITE_API_URL || 'http://localhost:9090'

async function call(path, options = {}) {
  let res
  try {
    res = await fetch(API + path, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    })
  } catch {
    throw new Error('Cannot reach the server. Is the backend running? (docker compose up)')
  }
  const text = await res.text()
  let data = null
  try { data = text ? JSON.parse(text) : null } catch { data = text }
  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `Request failed (${res.status})`
    throw new Error(msg)
  }
  return data
}

export const api = {
  signup: (body) =>
    call('/customer-service/api/v1/customers', { method: 'POST', body: JSON.stringify(body) }),

  login: (email, password) =>
    call('/customer-service/api/v1/customers/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  me: (userId) =>
    call('/customer-service/api/v1/customers/me', { headers: { 'X-User-Id': userId } }),

  lookup: (email) =>
    call('/customer-service/api/v1/customers/lookup?email=' + encodeURIComponent(email)),

  account: (accountNumber) =>
    call(`/accounts-service/api/v1/accounts/${accountNumber}`),

  transactions: (accountNumber, size = 15) =>
    call(`/accounts-service/api/v1/accounts/${accountNumber}/transactions?page=0&size=${size}`),

  transfer: (body) =>
    call('/payment-service/api/v1/payments/transfer', { method: 'POST', body: JSON.stringify(body) }),
}

export const inr = (n) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(
    Number(n || 0),
  )
