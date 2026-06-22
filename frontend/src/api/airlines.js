import api from './client'

export const airlinesApi = {
  getAll: () => api.get('/airlines'),
  getById: (id) => api.get(`/airlines/${id}`),
}
