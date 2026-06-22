import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.message || err.response?.data?.error || err.message
    console.error(`[API] ${err.config?.method?.toUpperCase()} ${err.config?.url} → ${err.response?.status}: ${msg}`)
    return Promise.reject(err)
  }
)

export default api
