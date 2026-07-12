import api from './client'

export const sitesApi = {
  getAll: () => api.get('/sites'),
  getById: (id) => api.get(`/sites/${id}`),
  create: (dto) => api.post('/sites', dto),
  update: (id, dto) => api.put(`/sites/${id}`, dto),
  delete: (id) => api.delete(`/sites/${id}`),
}
