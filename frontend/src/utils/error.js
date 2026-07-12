export function extractError(e) {
  if (!e) return 'Error desconocido'
  if (e.response?.data?.message && typeof e.response.data.message === 'string') return e.response.data.message
  if (e.response?.data?.error && typeof e.response.data.error === 'string') return e.response.data.error
  if (e.message) return e.message
  return String(e)
}

/**
 * Centralized API error handler.
 * - 401 → redirect to login (already handled in interceptor, no toast)
 * - 403 → silent reject (permissions, expected)
 * - 5xx / network → global toast notification
 * - 4xx → console.warn only (caller handles with toast if needed)
 */
export function handleApiError(err, toast) {
  const status = err.response?.status
  if (status === 401 || status === 403) return
  if (!err.response || status >= 500) {
    const msg = !err.response
      ? 'Error de conexión con el servidor'
      : 'Error interno del servidor'
    if (toast) toast.error(msg)
    else console.error(msg)
    return
  }
  console.warn(`[API] ${err.config?.method?.toUpperCase()} ${err.config?.url} → ${status}: ${extractError(err)}`)
}
