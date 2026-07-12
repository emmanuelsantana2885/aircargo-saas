import api from './client'

export const uldTypeConfigApi = {
  getByAirline(airlineId) {
    return api.get(`/uld-type-config/${airlineId}`)
  }
}
