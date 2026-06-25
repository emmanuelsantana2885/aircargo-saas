import api from './client'
const UPS = '00000000-0000-0000-0000-000000000001'

export const uldsApi = {
  getAll: (params = {}) => api.get('/ulds', { params: { airlineId: UPS, ...params } }),
  getByFlight: (flightId) => api.get('/ulds', { params: { airlineId: UPS, flightId } }),
  getById: (id) => api.get(`/ulds/${id}`),
  create: (dto) => api.post('/ulds', { airlineId: UPS, ...dto }),
  update: (id, dto) => api.put(`/ulds/${id}`, dto),
  delete: (id) => api.delete(`/ulds/${id}`),
  assignFlight: (id, flightId) => api.patch(`/ulds/${id}/flight`, { flightId }),
}
