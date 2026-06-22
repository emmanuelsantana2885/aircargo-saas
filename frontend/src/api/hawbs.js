import api from './client'

export const hawbsApi = {
  getByMawb: (mawbId) => api.get(`/cargo/hawbs/mawb/${mawbId}`),
  update: (id, dto) => api.put(`/cargo/hawbs/${id}`, dto),
}
