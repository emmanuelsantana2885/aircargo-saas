
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/client'

const UPS_AIRLINE_ID = '00000000-0000-0000-0000-000000000001'

interface FlightItem {
  id: string
  flightNumber: string
  origin: string
  destination: string
  flightDate: string
  aircraftType?: string
  aircraftReg?: string
  status?: string
}

interface UldItem {
  id?: string
  uldNumber?: string
  uldType?: string
  config?: string | null
  pos?: string
  position?: string
  sealNumber?: string | null
  grossWeightLbs?: number
  grossWeight?: number
  tareLbs?: number
  tareWeight?: number
  status?: string
  filledBy?: string | null
  backendId?: string
  flightId?: string | null
}

function inferUldType(uldId: string | undefined | null, config?: string | null): string {
  const id = (uldId || '').toUpperCase()
  if (id.startsWith('PMC')) return 'PMC'
  if (id.startsWith('PMH')) return 'PMH'
  if (id.startsWith('PAH')) return 'PAH'
  if (id.startsWith('AAD')) return 'AAD'
  if (id.startsWith('AAY')) return 'AAY'
  if (id.startsWith('AMP')) return 'AMP'
  if (id.startsWith('AMJ')) return 'AMJ'
  if (id.startsWith('PIP')) return 'PIP'
  return 'PMC'
}

export const useUldsStore = defineStore('ulds', () => {
  const flights = ref<FlightItem[]>([])
  const selectedFlightId = ref<string | null>(null)
  const uldsByFlight = ref<Record<string, UldItem[]>>({})
  const floatingUlDs = ref<UldItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isShowingFloating = computed(() => selectedFlightId.value === '__floating__')

  const selectedFlight = computed<FlightItem | null>(() =>
    isShowingFloating.value ? null : (flights.value.find(f => f.id === selectedFlightId.value) || null)
  )

  const activeUlds = computed<UldItem[]>(() => {
    if (isShowingFloating.value) return floatingUlDs.value
    if (!selectedFlightId.value) return []
    return uldsByFlight.value[selectedFlightId.value] || []
  })

  function apiError(e: unknown): string {
  if (e instanceof Error) {
    const axios = (e as unknown as Record<string, unknown>).response as Record<string, unknown> | undefined
    if (axios) {
      const d = axios.data as Record<string, unknown> | undefined
      if (d?.message && typeof d.message === 'string') return d.message
    }
    return e.message
  }
  return String(e)
}

async function loadFlights(): Promise<void> {
    try {
      loading.value = true
      error.value = null
      const res = await api.get('/flights', { params: { airlineId: UPS_AIRLINE_ID } })
      flights.value = res.data
      if (flights.value.length && !selectedFlightId.value) {
        const first = flights.value[0]
        if (first) await selectFlight(first.id)
      }
    } catch (e: unknown) {
      error.value = 'Error cargando vuelos: ' + apiError(e)
    } finally {
      loading.value = false
    }
  }

  async function loadUldsForFlight(flightId: string): Promise<void> {
    try {
      loading.value = true
      const res = await api.get('/ulds', { params: { airlineId: UPS_AIRLINE_ID, flightId } })
      uldsByFlight.value[flightId] = res.data
    } catch (e: unknown) {
      error.value = 'Error cargando ULDs: ' + apiError(e)
    } finally {
      loading.value = false
    }
  }

  async function loadFloatingUlDs(): Promise<void> {
    try {
      loading.value = true
      const res = await api.get('/ulds', { params: { airlineId: UPS_AIRLINE_ID } })
      floatingUlDs.value = (res.data || []).filter((u: UldItem) => !u.flightId)
      selectedFlightId.value = '__floating__'
    } catch (e: unknown) {
      error.value = 'Error cargando ULDs flotantes: ' + apiError(e)
    } finally {
      loading.value = false
    }
  }

  async function selectFlight(flightId: string): Promise<void> {
    if (flightId === '__floating__') {
      await loadFloatingUlDs()
      return
    }
    selectedFlightId.value = flightId
    await loadUldsForFlight(flightId)
  }

  async function dispatchUld(uld: UldItem, flightId: string): Promise<unknown> {
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
    uldsByFlight, floatingUlDs, activeUlds, loading, error,
    isShowingFloating,
    loadFlights, loadUldsForFlight, loadFloatingUlDs, selectFlight, dispatchUld
  }
})
