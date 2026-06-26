import api from './client'
const UPS = '00000000-0000-0000-0000-000000000001'

export const receiptsApi = {
  getAll:   () => api.get('/receipts', { params: { airlineId: UPS } }),
  getById:  (id) => api.get(`/receipts/${id}`),
  create:   (dto) => api.post('/receipts', { airlineId: UPS, ...dto }),
  update:   (id, dto) => api.put(`/receipts/${id}`, dto),
  delete:   (id) => api.delete(`/receipts/${id}`),
  getPieces: (id) => api.get(`/warehouse/receipts/${id}/pieces`),
  export:   (id) => api.get(`/warehouse/receipts/${id}/export`, { responseType: 'blob' }),
  getSupportingDocsJson: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs`),
  getSupportingDocsHtml: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs/html`, { responseType: 'text' }),
  getSupportingDocsPdf: (id) => api.get(`/warehouse/receipts/${id}/supporting-docs/pdf`, { responseType: 'blob' }),
}
