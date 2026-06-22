
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/client'

// UPS airline ID fijo en el sistema
const UPS_AIRLINE_ID = '00000000-0000-0000-0000-000000000001'

// Mapea el tipo de ULD del frontend al enum del backend
function inferUldType(uldId, config) {
  const id = (uldId || '').toUpperCase()
  if (id.startsWith('PMC')) return 'PMC'
  if (id.startsWith('PMH')) return 'PMH'
  if (id.startsWith('PAH')) return 'PAH'
  if (id.startsWith('AAD')) return 'AAD'
  if (id.startsWith('AAY')) return 'AAY'
  if (id.startsWith('AMP')) return 'AMP'
  if (id.startsWith('AMJ')) return 'AMJ'
  if (id.startsWith('PIP')) return 'PIP'
  return 'PMC' // fallback
}

export const useUldsStore = defineStore('ulds', () => {
  const flights = ref([])
  const selectedFlightId = ref(null)
  const uldsByFlight = ref({})
  const loading = ref(false)
  const error = ref(null)

  const selectedFlight = computed(() =>
    flights.value.find(f => f.id === selectedFlightId.value) || null
  )

  const activeUlds = computed(() =>
    selectedFlightId.value ? (uldsByFlight.value[selectedFlightId.value] || []) : []
  )

  async function loadFlights() {
    try {
      loading.value = true
      error.value = null
      const res = await api.get('/flights', { params: { airlineId: UPS_AIRLINE_ID } })
      flights.value = res.data
      if (flights.value.length && !selectedFlightId.value) {
        await selectFlight(flights.value[0].id)
      }
    } catch (e) {
      error.value = 'Error cargando vuelos: ' + (e.response?.data?.message || e.message)
    } finally {
      loading.value = false
    }
  }

  async function loadUldsForFlight(flightId) {
    try {
      loading.value = true
      const res = await api.get('/ulds', { params: { airlineId: UPS_AIRLINE_ID, flightId } })
      uldsByFlight.value[flightId] = res.data
    } catch (e) {
      error.value = 'Error cargando ULDs: ' + (e.response?.data?.message || e.message)
    } finally {
      loading.value = false
    }
  }

  async function selectFlight(flightId) {
    selectedFlightId.value = flightId
    await loadUldsForFlight(flightId)
  }

  async function dispatchUld(uld, flightId) {
    if (!flightId) throw new Error('flightId UUID requerido')

    const dto = {
      airlineId: UPS_AIRLINE_ID,
      flightId: flightId,
      uldNumber: uld.id || uld.uldNumber,
      uldType: uld.uldType || inferUldType(uld.id || uld.uldNumber, uld.config),
      config: uld.config || null,
      position: uld.pos || uld.position || null,
      sealNumber: uld.sealNumber || null,
      grossWeightLbs: uld.grossWeightLbs || uld.grossWeight || 0,
      tareLbs: uld.tareLbs || uld.tareWeight || 0,
      status: uld.status || 'BUILT',
      notes: uld.filledBy ? `Llenado por: ${uld.filledBy}` : null,
    }

    let res
    if (uld.backendId) {
      res = await api.put(`/ulds/${uld.backendId}`, dto)
    } else {
      res = await api.post('/ulds', dto)
    }

    await loadUldsForFlight(flightId)
    return res.data
  }

  return {
    flights, selectedFlightId, selectedFlight,
    uldsByFlight, activeUlds, loading, error,
    loadFlights, loadUldsForFlight, selectFlight, dispatchUld
  }
})

