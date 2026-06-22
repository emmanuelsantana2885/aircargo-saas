import api from './client'

export const mawbsApi = {
  getAll: () => api.get('/cargo/mawbs'),
  getByFlight: (flightId) => api.get(`/cargo/mawbs/flight/${flightId}`),
  create: (dto) => api.post('/cargo/mawbs', dto),
  updateStatus: (mawbId, status) => api.patch(`/cargo/mawbs/${mawbId}/status`, { status }),
}
