import api from './client'

export const uldAwbsApi = {
  getAll:     (params = {}) => api.get('/uld-awbs', { params }),
  getByUld:   (uldId) => api.get('/uld-awbs', { params: { uldId } }),
  getByMawb:  (mawbId) => api.get('/uld-awbs', { params: { mawbId } }),
  create:     (dto) => api.post('/uld-awbs', dto),
  update:     (id, dto) => api.put(`/uld-awbs/${id}`, dto),
  delete:     (id) => api.delete(`/uld-awbs/${id}`),
}
