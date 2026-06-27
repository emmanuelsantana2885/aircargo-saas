<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xs font-black tracking-tight text-slate-950 uppercase font-mono">Payload Dashboard</h1>
        <p class="text-xs font-mono text-slate-500 mt-0.5 uppercase tracking-widest font-bold">SDQ Hub // Payload Despachado por Vuelo</p>
      </div>
      <div class="flex items-center gap-3 text-xs font-mono font-bold">
        <span class="flex items-center gap-1 text-slate-500">
          <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> LIVE
        </span>
        <span class="text-slate-300">|</span>
        <span class="text-slate-950">{{ filteredFlights.length }} vuelos</span>
      </div>
    </header>

    <section class="flex items-center gap-3 my-3 shrink-0 flex-wrap">
      <div class="flex items-center gap-2">
        <label class="text-xs font-mono font-black uppercase tracking-widest text-slate-950">Desde</label>
        <input v-model="dateFrom" type="date"
          class="text-sm font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950" />
      </div>
      <div class="flex items-center gap-2">
        <label class="text-xs font-mono font-black uppercase tracking-widest text-slate-950">Hasta</label>
        <input v-model="dateTo" type="date"
          class="text-sm font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950" />
      </div>
      <div class="text-xs font-mono text-slate-500 ml-auto flex items-center gap-4">
        <span>Total Neto: <strong class="text-slate-950">{{ totalNetPayload }} lbs</strong></span>
        <span>Total ULDs: <strong class="text-slate-950">{{ totalUldsCount }}</strong></span>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div ref="tableWrapper" class="overflow-auto flex-1 min-h-0 scrollbar-none">
        <table class="w-full border-collapse text-xs font-mono">
          <thead class="sticky top-0 z-20">
            <tr class="bg-slate-950 text-white text-xs font-black uppercase tracking-wider border-b border-slate-700">
              <th class="text-center px-2 py-2.5 whitespace-nowrap">#</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Vuelo</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Ruta</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Fecha</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Estado</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">ULDs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Posiciones</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Gross Lbs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Tare Lbs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Neto Lbs</th>
              <th class="text-center px-2 py-2.5 whitespace-nowrap">Docs</th>
              <th class="text-right px-2 py-2.5 whitespace-nowrap">Payload Lbs</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading" class="h-32">
              <td colspan="11" class="text-center text-xs font-mono text-slate-400 animate-pulse">Cargando datos...</td>
            </tr>
            <tr v-else-if="filteredFlights.length === 0" class="h-32">
              <td colspan="11" class="text-center text-xs font-mono text-slate-400 uppercase tracking-widest">No hay vuelos en este rango</td>
            </tr>
            <tr v-for="(f, fi) in filteredFlights" :key="f.id"
              class="border-b border-slate-100 transition-colors hover:bg-slate-50">
              <td class="text-center px-2 py-2 text-slate-400">{{ fi + 1 }}</td>
              <td class="text-center px-2 py-2 font-bold">UPS-{{ f.flightNumber }}</td>
              <td class="text-center px-2 py-2">{{ f.origin }}→{{ f.destination }}</td>
              <td class="text-center px-2 py-2 text-slate-500">{{ f.flightDate }}</td>
              <td class="text-center px-2 py-2">
                <span class="inline-flex items-center gap-1">
                  <span :class="getStatusDot(f.status)" class="inline-block w-2 h-2 rounded-full"></span>
                  <span class="text-xs uppercase" :class="statusTextClass(f.status)">{{ statusLabel(f.status) }}</span>
                </span>
              </td>
              <td class="text-center px-2 py-2 font-bold">{{ flightUlds(f.id).length }}</td>
              <td class="text-center px-2 py-2 font-bold">{{ flightPositions(f.id) }}</td>
              <td class="text-right px-2 py-2 font-bold">{{ grossLbs(f.id) }}</td>
              <td class="text-right px-2 py-2 font-bold text-amber-600">{{ totalTareLbs(f.id) }}</td>
              <td class="text-right px-2 py-2 font-bold">{{ netLbs(f.id) }}</td>
              <td class="text-center px-2 py-2 text-slate-400">5</td>
              <td class="text-right px-2 py-2 font-bold text-emerald-600">{{ payloadLbs(f.id) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAppStore } from '../stores/app'

const appStore = useAppStore()

const dateFrom = ref('')
const dateTo = ref('')
const loading = ref(false)

const filteredFlights = computed(() => {
  let list = appStore.flights
  if (dateFrom.value) {
    list = list.filter(f => f.flightDate >= dateFrom.value)
  }
  if (dateTo.value) {
    list = list.filter(f => f.flightDate <= dateTo.value)
  }
  return list
})

const allUlDs = computed(() => appStore.ulds)

function flightUlds(flightId) {
  return allUlDs.value.filter(u => u.flightId === flightId)
}

function flightPositions(flightId) {
  const ulds = flightUlds(flightId)
  return new Set(ulds.map(u => u.position).filter(Boolean)).size
}

function grossLbs(flightId) {
  const ulds = flightUlds(flightId)
  return ulds.reduce((s, u) => s + (Number(u.grossWeightLbs) || 0), 0)
}

function totalTareLbs(flightId) {
  const ulds = flightUlds(flightId)
  return ulds.reduce((s, u) => s + (Number(u.tareLbs) || 0), 0)
}

function netLbs(flightId) {
  return grossLbs(flightId) - totalTareLbs(flightId)
}

function payloadLbs(flightId) {
  return netLbs(flightId)
}

const totalNetPayload = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + payloadLbs(f.id), 0)
})

const totalUldsCount = computed(() => {
  return filteredFlights.value.reduce((s, f) => s + flightUlds(f.id).length, 0)
})

function getStatusDot(status) {
  if (status === 'SCHEDULED') return 'bg-slate-400'
  if (status === 'BOARDING') return 'bg-amber-500'
  if (status === 'DEPARTED') return 'bg-emerald-500'
  if (status === 'ARRIVED') return 'bg-blue-500'
  if (status === 'CANCELLED') return 'bg-rose-500'
  if (status === 'DELAYED') return 'bg-orange-500'
  return 'bg-slate-200'
}

function statusLabel(status) {
  const map = {
    SCHEDULED: 'SCH',
    BOARDING: 'BRD',
    DEPARTED: 'DPT',
    ARRIVED: 'ARR',
    CANCELLED: 'CNL',
    DELAYED: 'DLY',
  }
  return map[status] || status?.slice(0, 3) || '—'
}

function statusTextClass(status) {
  if (status === 'SCHEDULED') return 'text-slate-400'
  if (status === 'BOARDING') return 'text-amber-600'
  if (status === 'DEPARTED') return 'text-emerald-600'
  if (status === 'ARRIVED') return 'text-blue-600'
  if (status === 'CANCELLED') return 'text-rose-500'
  if (status === 'DELAYED') return 'text-orange-500'
  return 'text-slate-400'
}

onMounted(async () => {
  loading.value = true
  await appStore.loadFlights()
  if (appStore.flights.length) {
    await Promise.all([
      appStore.loadUlds(),
      appStore.loadAllMawbs(),
    ])
  }
  loading.value = false
})
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04); }
</style>
