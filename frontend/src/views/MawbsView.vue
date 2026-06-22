<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex flex-wrap justify-between items-end gap-2 border-b border-slate-200 pb-3 shrink-0">
      <div class="flex items-end gap-3">
        <div>
          <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">CrossReport — MAWBs</h1>
          <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">Matriz MAWB x Vuelo // Distribucion de Piezas</p>
        </div>
        <div class="flex items-end gap-2">
          <button @click="showFilter = !showFilter"
            class="px-2 py-1 rounded border transition-all"
            :class="showFilter || filterText ? 'bg-slate-950 text-white border-slate-950' : 'bg-white text-slate-400 border-slate-200 hover:border-slate-400'"
            title="Filtro dinamico">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
          </button>
          <div v-if="showFilter" class="flex items-center gap-1">
            <input v-model="filterText" @keyup.enter="applyFilter" placeholder="* = contenga, = exacto, > < numerico"
              class="w-56 px-2 py-1 text-[10px] font-mono border border-slate-300 rounded bg-white focus:outline-none focus:border-slate-950 placeholder:text-slate-300" />
            <span class="text-[8px] font-mono text-slate-400 min-w-[30px]">{{ filterTypeLabel }}</span>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <span class="text-[9px] font-mono text-slate-400">Filas: {{ filteredRows.length }} / {{ matrixRows.length }} | Cols: {{ flightColumns.length }}</span>
        <select v-model="localFlightId" @change="onFlightChange"
          class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-[10px] cursor-pointer min-w-[140px]">
          <option value="">Todos los vuelos</option>
          <option v-for="flight in store.flights" :key="flight.id" :value="flight.id">
            UPS-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }})
          </option>
        </select>
      </div>
    </header>

    <section class="grid grid-cols-1 md:grid-cols-6 gap-2 my-3 shrink-0">
      <div v-for="stat in mawbStats" :key="stat.label"
        class="py-1.5 px-2.5 rounded bg-white border border-slate-200 border-l-4 shadow-pencil-marine flex flex-col justify-between min-h-[54px]"
        :class="stat.border">
        <div>
          <h3 class="text-[7.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-lg font-mono font-black tracking-tight text-slate-950 mt-0">{{ stat.value }}</div>
        </div>
        <div class="text-[7px] font-mono text-slate-400 truncate"><span>{{ stat.sub }}</span></div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden bg-white flex flex-col mb-1.5">
      <div v-if="loadingMatrix" class="flex-1 flex items-center justify-center">
        <span class="text-[10px] font-mono text-slate-400 animate-pulse">Construyendo matriz...</span>
      </div>
      <div v-else-if="!filteredRows.length" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Sin resultados</p>
      </div>
      <template v-else>
        <div class="overflow-auto flex-1 min-h-0 scrollbar-none">
          <table class="w-full border-collapse text-[10px] font-mono">
            <thead class="sticky top-0 z-20">
              <tr class="bg-slate-800 text-white text-[9px]">
                <th class="sticky left-0 z-30 bg-slate-800 text-left px-2 py-1.5 font-black uppercase tracking-wider min-w-[120px] border-r border-slate-600">MAWB</th>
                <th class="sticky left-[120px] z-30 bg-slate-800 text-left px-2 py-1.5 font-black uppercase tracking-wider min-w-[130px] border-r border-slate-600">Shipper / Consignee</th>
                <th class="px-2 py-1.5 text-right font-black min-w-[44px]">Pzas</th>
                <th class="px-2 py-1.5 text-right font-black min-w-[48px]">Kg</th>
                <th class="px-2 py-1.5 text-left font-black min-w-[44px]">Dest</th>
                <th class="px-2 py-1.5 text-center font-black min-w-[40px]">St</th>
                <th v-for="f in flightColumns" :key="f.id"
                  class="px-1.5 py-1 text-center font-black min-w-[72px] border-x border-slate-600/30"
                  :class="highlightFlightId === f.id ? 'bg-amber-500 text-slate-950' : ''">
                  <div class="text-[8px] leading-tight">UPS-{{ f.flightNumber }}</div>
                  <div class="text-[6px] font-normal opacity-70">{{ formatDate(f.flightDate) }}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in filteredRows" :key="row.mawbId"
                class="border-b border-slate-100 transition-colors"
                :class="rowBgClass(row)">
                <td class="sticky left-0 z-10 bg-white px-2 py-1.5 font-bold text-slate-950 border-r border-slate-200 truncate max-w-[120px]"
                  :title="row.awbNumber">{{ row.awbNumber || '—' }}</td>
                <td class="sticky left-[120px] z-10 bg-white px-2 py-1.5 text-slate-600 truncate max-w-[130px] border-r border-slate-200"
                  :title="row.shipperName || row.consigneeName || ''">{{ row.shipperName || row.consigneeName || '—' }}</td>
                <td class="px-2 py-1.5 text-right font-bold text-slate-900">{{ row.totalPieces || '—' }}</td>
                <td class="px-2 py-1.5 text-right font-bold text-slate-950">{{ row.totalWeightKg ? Number(row.totalWeightKg).toLocaleString() : '—' }}</td>
                <td class="px-2 py-1.5 text-left text-slate-500 uppercase text-[9px]">{{ row.destination || '—' }}</td>
                <td class="px-2 py-1.5 text-center">
                  <span :class="statusDot(row)"></span>
                </td>
                <td v-for="f in flightColumns" :key="f.id"
                  class="px-1.5 py-1.5 text-center border-x border-slate-100 font-bold text-[10px]"
                  :class="cellBg(row, f)">
                  <template v-if="getPieces(row, f)">{{ getPieces(row, f) }}</template>
                  <template v-else><span class="text-slate-200">&middot;</span></template>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="bg-slate-50 border-t border-slate-200 px-4 py-1 text-[8px] text-slate-400 font-mono flex justify-between items-center shrink-0">
          <span>Piezas en matriz: {{ totalTracked }} / {{ totalMawbPcs }}</span>
          <span class="text-slate-300">Celdas ocupadas: {{ activeCells }} | Vuelos: {{ flightColumns.length }} | MAWBs: {{ filteredRows.length }}</span>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, shallowRef } from 'vue'
import { useAppStore } from '../stores/app'
import api from '../api/client'

const store = useAppStore()

const localFlightId = ref(store.selectedFlightId || '')
const loadingMatrix = ref(false)
const highlightFlightId = ref(null)
const showFilter = ref(false)
const filterText = ref('')

const matrixRows = shallowRef([])
const flightColumns = shallowRef([])
const allUlds = shallowRef([])
const allLinks = shallowRef([])

const filterTypeLabel = computed(() => {
  const t = filterText.value
  if (!t) return ''
  if (t.startsWith('=')) return 'exacto'
  if (t.startsWith('>')) return 'mayor'
  if (t.startsWith('<')) return 'menor'
  if (t.startsWith('*') || t.endsWith('*')) return 'contiene'
  if (/^\d+$/.test(t)) return 'num'
  return 'texto'
})

const filteredRows = computed(() => {
  const rows = matrixRows.value
  const t = filterText.value.trim()
  if (!t) return rows

  let op = 'contains'
  let val = t

  if (t.startsWith('=')) { op = 'exact'; val = t.slice(1) }
  else if (t.startsWith('>')) { op = 'gt'; val = t.slice(1) }
  else if (t.startsWith('<')) { op = 'lt'; val = t.slice(1) }
  else if (t.startsWith('*') && t.endsWith('*')) { op = 'contains'; val = t.slice(1, -1) }
  else if (t.startsWith('*')) { op = 'ends'; val = t.slice(1) }
  else if (t.endsWith('*')) { op = 'starts'; val = t.slice(0, -1) }

  const isNumeric = /^-?\d+(\.\d+)?$/.test(val)

  return rows.filter(row => {
    if (op === 'gt' && isNumeric) {
      const n = parseFloat(val)
      return row.totalPieces > n || (row.totalWeightKg && parseFloat(row.totalWeightKg) > n)
    }
    if (op === 'lt' && isNumeric) {
      const n = parseFloat(val)
      return row.totalPieces < n || (row.totalWeightKg && parseFloat(row.totalWeightKg) < n)
    }
    if (op === 'exact') {
      const lower = val.toLowerCase()
      return (row.awbNumber && row.awbNumber.toLowerCase() === lower) ||
             (row.shipperName && row.shipperName.toLowerCase() === lower) ||
             (row.consigneeName && row.consigneeName.toLowerCase() === lower) ||
             (row.destination && row.destination.toLowerCase() === lower) ||
             (row.totalPieces && row.totalPieces.toString() === val)
    }
    if (op === 'starts') {
      const lower = val.toLowerCase()
      return (row.awbNumber && row.awbNumber.toLowerCase().startsWith(lower)) ||
             (row.shipperName && row.shipperName.toLowerCase().startsWith(lower)) ||
             (row.consigneeName && row.consigneeName.toLowerCase().startsWith(lower)) ||
             (row.destination && row.destination.toLowerCase().startsWith(lower))
    }
    if (op === 'ends') {
      const lower = val.toLowerCase()
      return (row.awbNumber && row.awbNumber.toLowerCase().endsWith(lower)) ||
             (row.shipperName && row.shipperName.toLowerCase().endsWith(lower)) ||
             (row.consigneeName && row.consigneeName.toLowerCase().endsWith(lower)) ||
             (row.destination && row.destination.toLowerCase().endsWith(lower))
    }
    if (isNumeric) {
      const n = parseFloat(val)
      return row.totalPieces === n || (row.totalPieces && row.totalPieces.toString().includes(val)) ||
             (row.awbNumber && row.awbNumber.includes(val))
    }
    const lower = val.toLowerCase()
    return (row.awbNumber && row.awbNumber.toLowerCase().includes(lower)) ||
           (row.shipperName && row.shipperName.toLowerCase().includes(lower)) ||
           (row.consigneeName && row.consigneeName.toLowerCase().includes(lower)) ||
           (row.destination && row.destination.toLowerCase().includes(lower))
  })
})

async function buildMatrix() {
  loadingMatrix.value = true
  try {
    const [uldRes, linkRes] = await Promise.all([
      api.get('/ulds', { params: { airlineId: '00000000-0000-0000-0000-000000000001' } }),
      api.get('/uld-awbs'),
    ])
    const ulds = uldRes.data
    const links = linkRes.data
    allUlds.value = ulds
    allLinks.value = links

    const uldFlightMap = {}
    for (const u of ulds) uldFlightMap[u.id] = u.flightId

    const mawbPcsByFlight = new Map()
    for (const link of links) {
      const mawbId = link.mawbId
      const flightId = uldFlightMap[link.uldId]
      const pcs = link.pieces || 0
      if (!mawbId || !flightId) continue
      if (!mawbPcsByFlight.has(mawbId)) mawbPcsByFlight.set(mawbId, new Map())
      const fm = mawbPcsByFlight.get(mawbId)
      fm.set(flightId, (fm.get(flightId) || 0) + pcs)
    }

    const flightIds = new Set()
    for (const fm of mawbPcsByFlight.values())
      for (const fid of fm.keys()) flightIds.add(fid)

    let filteredMawbs = store.mawbs
    if (localFlightId.value) {
      filteredMawbs = store.mawbs.filter(m => m.flight?.id === localFlightId.value || m.flightId === localFlightId.value)
      const extraFids = new Set()
      for (const m of filteredMawbs) {
        const fm = mawbPcsByFlight.get(m.id)
        if (fm) for (const fid of fm.keys()) extraFids.add(fid)
      }
      flightIds.clear()
      flightIds.add(localFlightId.value)
      for (const fid of extraFids) flightIds.add(fid)
    }

    const flights = store.flights.filter(f => flightIds.has(f.id))
    flights.sort((a, b) => (a.flightDate || '').localeCompare(b.flightDate || '') || (a.flightNumber || '').localeCompare(b.flightNumber || ''))

    const rows = filteredMawbs.map(m => {
      const fm = mawbPcsByFlight.get(m.id) || new Map()
      const cells = {}
      let totalPcs = 0
      for (const f of flights) {
        const pcs = fm.get(f.id) || 0
        cells[f.id] = pcs
        totalPcs += pcs
      }
      return {
        mawbId: m.id,
        awbNumber: m.awbNumber,
        shipperName: m.shipperName,
        consigneeName: m.consigneeName,
        destination: m.destination,
        totalPieces: totalPcs || m.pieces || 0,
        totalWeightKg: m.reportedWeightKg || m.chargeableWeightKg || null,
        status: m.status,
        cells,
      }
    })

    matrixRows.value = rows
    flightColumns.value = flights
  } catch (e) {
    console.error('Matrix error:', e)
  } finally {
    loadingMatrix.value = false
  }
}

function getPieces(row, flight) { return row.cells[flight.id] || 0 }

function cellBg(row, flight) {
  const pcs = getPieces(row, flight)
  if (!pcs) return ''
  const total = row.totalPieces || 1
  const pct = pcs / total
  if (pct >= 0.75) return 'bg-slate-950/10 text-slate-950 font-black'
  if (pct >= 0.4) return 'bg-slate-950/5 text-slate-700 font-bold'
  return 'bg-slate-950/[0.02] text-slate-500'
}

function rowBgClass(row) {
  if (!row.status) return ''
  if (row.status === 'DEPARTED') return 'bg-emerald-50/40'
  if (row.status === 'MANIFESTED') return 'bg-blue-50/30'
  if (row.status === 'RECEIVED') return 'bg-sky-50/20'
  return ''
}

function statusDot(row) {
  if (!row.status) return 'inline-block w-1.5 h-1.5 rounded-full bg-slate-200'
  const map = { BOOKED: 'bg-amber-500', RECEIVED: 'bg-blue-500', MANIFESTED: 'bg-emerald-500', DEPARTED: 'bg-slate-950' }
  return 'inline-block w-1.5 h-1.5 rounded-full ' + (map[row.status] || 'bg-slate-200')
}

function formatDate(d) {
  if (!d) return '—'
  const parts = d.split('-')
  return parts.length === 3 ? parts[1] + '/' + parts[2] : d
}

const mawbStats = computed(() => {
  const rows = filteredRows.value
  const totalPcs = rows.reduce((s, r) => s + (r.totalPieces || 0), 0)
  const tracked = rows.reduce((s, r) => s + Object.values(r.cells).reduce((a, b) => a + b, 0), 0)
  const departed = rows.filter(r => r.status === 'DEPARTED').length
  return [
    { label: "MAWBs", value: rows.length, sub: `${totalPcs} pzas`, border: "border-l-slate-700" },
    { label: "Vuelos", value: flightColumns.value.length, sub: "en matriz", border: "border-l-slate-500" },
    { label: "Pzas Track", value: tracked, sub: `de ${totalPcs}`, border: "border-l-amber-500" },
    { label: "Track %", value: totalPcs ? Math.round(tracked / totalPcs * 100) + '%' : '—', sub: 'en ULDs', border: "border-l-emerald-500" },
    { label: "Departed", value: departed, sub: `${rows.length - departed} pend.`, border: "border-l-slate-950" },
    { label: "Filtro", value: filterText.value ? `"${filterText.value}"` : '—', sub: filterTypeLabel.value || 'sin filtro', border: "border-l-amber-400" },
  ]
})

const totalTracked = computed(() =>
  filteredRows.value.reduce((s, r) => s + Object.values(r.cells).reduce((a, b) => a + b, 0), 0)
)
const totalMawbPcs = computed(() =>
  filteredRows.value.reduce((s, r) => s + (r.totalPieces || 0), 0)
)
const activeCells = computed(() => {
  let c = 0
  for (const r of filteredRows.value)
    for (const v of Object.values(r.cells)) if (v > 0) c++
  return c
})

async function onFlightChange() {
  if (localFlightId.value) {
    store.selectedFlightId = localFlightId.value
    await store.loadMawbs(localFlightId.value)
  } else {
    store.selectedFlightId = null
    await store.loadAllMawbs()
  }
  highlightFlightId.value = localFlightId.value || null
  await buildMatrix()
}

onMounted(async () => {
  if (!store.flights.length) await store.loadFlights()
  await store.loadReceipts()
  if (store.selectedFlightId) {
    localFlightId.value = store.selectedFlightId
    await store.loadMawbs(store.selectedFlightId)
  } else {
    await store.loadAllMawbs()
  }
  highlightFlightId.value = localFlightId.value || null
  await buildMatrix()
})
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
table { border-spacing: 0; }
thead th { position: sticky; top: 0; }
tbody td { white-space: nowrap; }
</style>
