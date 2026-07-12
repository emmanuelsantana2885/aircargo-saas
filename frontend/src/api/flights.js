import api from './client'

export const flightsApi = {
  getAll: (params = {}) => api.get('/flights', { params }),
  getById: (id) => api.get(`/flights/${id}`),
  create: (dto) => api.post('/flights', dto),
  update: (id, dto) => api.put(`/flights/${id}`, dto),
  delete: (id) => api.delete(`/flights/${id}`),
}
