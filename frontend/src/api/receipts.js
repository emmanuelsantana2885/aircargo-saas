import api from './client'

export const receiptsApi = {
  getAll:   (params = {}) => api.get('/receipts', { params }),
  getById:  (id) => api.get(`/receipts/${id}`),
  delete:   (id) => api.delete(`/receipts/${id}`),
  getPieces: (id) => api.get(`/warehouse/receipts/${id}/pieces`),
  export:   (id) => api.get(`/warehouse/receipts/${id}/export`, { responseType: 'blob' }),
  updateEmit: (id, payload) => api.put(`/warehouse/receipts/${id}/emit`, payload),
  getSupportingDocsJson: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs`),
  getSupportingDocsHtml: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs/html`, { responseType: 'text' }),
  getSupportingDocsPdf: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs/pdf`, { responseType: 'blob' }),
}
