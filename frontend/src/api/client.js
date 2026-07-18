import axios from 'axios'
import { useToastStore } from '../stores/toast'
import { handleApiError } from '../utils/error'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const stored = localStorage.getItem('aircargo_auth')
  if (stored) {
    try {
      const { token } = JSON.parse(stored)
      if (token) config.headers.Authorization = `Bearer ${token}`
    } catch {}
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('aircargo_auth')
      window.location.href = '/login'
      return Promise.reject(err)
    }
    if (err.response?.status === 403) {
      const url = err.config?.url || ''
      console.warn('[API 403] Sin permiso para:', err.config?.method?.toUpperCase(), url)
      return Promise.reject(err)
    }
    handleApiError(err, useToastStore())
    return Promise.reject(err)
  }
)

export default api
