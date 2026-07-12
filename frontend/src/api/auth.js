import api from './client'

export const authApi = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  setPassword: (email, newPassword, currentPassword) =>
    api.post('/auth/set-password', { email, newPassword, currentPassword }),
  me: () => api.get('/auth/me'),
}
