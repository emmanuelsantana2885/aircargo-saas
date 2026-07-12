import api from './client'

export function exportData(type, format = 'csv', dateFrom = null, dateTo = null, audit = false) {
  const params = { format, audit }
  if (dateFrom) params.dateFrom = dateFrom
  if (dateTo) params.dateTo = dateTo
  return api.get(`/exports/${type}`, { params, responseType: 'blob' })
}

export function getExportJson(type, dateFrom = null, dateTo = null, audit = false) {
  const params = { format: 'json', audit }
  if (dateFrom) params.dateFrom = dateFrom
  if (dateTo) params.dateTo = dateTo
  return api.get(`/exports/${type}`, { params })
}

export function importLoadPlanning(file) {
  const form = new FormData()
  form.append('file', file)
  return api.post('/exports/import/load-planning', form)
}
