import api from './client'

export const rolesApi = {
  getAllViews: () => api.get('/role-permissions/views'),
  getAllRoles: () => api.get('/role-permissions'),
  getRole: (role) => api.get(`/role-permissions/${role}`),
  updateRole: (role, permissions) => api.put(`/role-permissions/${role}`, { permissions }),
  createView: (dto) => api.post('/role-permissions/views', dto),
  updateView: (id, dto) => api.put(`/role-permissions/views/${id}`, dto),
  deleteView: (id) => api.delete(`/role-permissions/views/${id}`),
}
