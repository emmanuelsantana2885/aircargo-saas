import api from './client'
const UPS = '00000000-0000-0000-0000-000000000001'

export const flightsApi = {
  getAll: (params = {}) => api.get('/flights', { params: { airlineId: UPS, ...params } }),
  getById: (id) => api.get(`/flights/${id}`),
  create: (dto) => api.post('/flights', { ...dto, airlineId: dto.airlineId || UPS }),
  update: (id, dto) => api.put(`/flights/${id}`, { ...dto, airlineId: dto.airlineId || UPS }),
  delete: (id) => api.delete(`/flights/${id}`),
}
