import api from './client'

export const bookingsApi = {
  getAll: (params = {}) => api.get('/bookings', { params }),
  getByFlight: (flightId, airlineId) => api.get('/bookings', { params: { airlineId, flightId } }),
  getById: (id) => api.get(`/bookings/${id}`),
  create: (dto) => api.post('/bookings', dto),
  update: (id, dto) => api.put(`/bookings/${id}`, dto),
  delete: (id) => api.delete(`/bookings/${id}`),
  updateAwb: (id, awbNumber) => api.patch(`/bookings/${id}/awb`, { awbNumber }),
}
