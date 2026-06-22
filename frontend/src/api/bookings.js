import api from './client'
const UPS = '00000000-0000-0000-0000-000000000001'

export const bookingsApi = {
  getAll: (params = {}) => api.get('/bookings', { params: { airlineId: UPS, ...params } }),
  getByFlight: (flightId) => api.get('/bookings', { params: { airlineId: UPS, flightId } }),
  getById: (id) => api.get(`/bookings/${id}`),
  create: (dto) => api.post('/bookings', { airlineId: UPS, ...dto }),
  update: (id, dto) => api.put(`/bookings/${id}`, dto),
  delete: (id) => api.delete(`/bookings/${id}`),
  updateAwb: (id, awbNumber) => api.patch(`/bookings/${id}/awb`, { awbNumber }),
}
