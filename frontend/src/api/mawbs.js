import api from './client'

export const mawbsApi = {
  getAll: () => api.get('/cargo/mawbs'),
  getByFlight: (flightId) => api.get(`/cargo/mawbs/flight/${flightId}`),
  create: (dto) => api.post('/cargo/mawbs', dto),
  update: (mawbId, dto) => api.put(`/cargo/mawbs/${mawbId}`, dto),
  updateStatus: (mawbId, status) => api.patch(`/cargo/mawbs/${mawbId}/status`, { status }),
  getSupportingDocs: (mawbId) => api.get(`/cargo/mawbs/${mawbId}/supporting-docs`),
  updateSupportingDocs: (mawbId, docs) => api.put(`/cargo/mawbs/${mawbId}/supporting-docs`, docs),
  getSupportingDocsPdf: (mawbId) => api.get(`/cargo/mawbs/${mawbId}/supporting-docs/pdf`, { responseType: 'blob' }),
}
