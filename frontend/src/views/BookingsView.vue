<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div class="flex items-center gap-4">
        <div>
          <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Bookings Hub</h1>
          <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Control Desk</p>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Vuelo</span>
          <select v-model="localFlightId" @change="onFlightChange"
            class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-xs cursor-pointer min-w-[160px]">
            <option value="" disabled>Seleccionar vuelo</option>
            <option v-for="flight in flightList" :key="flight.id" :value="flight.id">
              UPS-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }})
            </option>
          </select>
        </div>
        <div v-if="store.selectedFlight" class="flex gap-3 text-[9px] font-mono font-bold text-slate-500">
          <span>{{ store.selectedFlight.aircraftReg || '—' }}</span>
          <span>{{ store.selectedFlight.flightDate }}</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button @click="triggerImport"
          class="flex items-center gap-1.5 text-[10px] px-2.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine text-slate-700 hover:bg-slate-50">
          <span class="text-xs font-semibold leading-none">↑</span> Import XLSX
        </button>
        <button @click="openCreate"
          class="flex items-center gap-1.5 text-[10px] px-2.5 py-1.5 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine bg-slate-950 text-white hover:bg-slate-800">
          <span class="text-xs font-semibold leading-none">+</span> New Booking
        </button>
        <input type="file" ref="fileInput" @change="handleFileImport" accept=".xlsx,.xls" class="hidden" />
      </div>
    </header>

    <section class="grid grid-cols-1 md:grid-cols-4 gap-4 my-4 shrink-0">
      <div v-for="stat in computedStats" :key="stat.label"
        :class="stat.borderClass"
        class="pencil-sketch py-2 px-3.5 rounded bg-white border border-slate-200 shadow-pencil-marine transition-all flex flex-col justify-between min-h-[78px] cursor-pointer">
        <div class="relative z-10">
          <h3 class="text-[9px] font-black text-slate-400 uppercase tracking-wider font-mono">{{ stat.label }}</h3>
          <div class="flex items-baseline gap-1 mt-0.5">
            <div class="text-2xl font-mono font-black tracking-tight text-slate-950">{{ stat.value }}</div>
          </div>
        </div>
        <div class="pt-1 border-t border-slate-150 text-[9px] font-mono text-slate-400 relative z-10 truncate">
          {{ stat.sub }}
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Booking ID</div>
        <div class="col-span-1 text-left">Vuelo</div>
        <div class="col-span-2 text-left">Agente / Broker</div>
        <div class="col-span-1 text-left">Shipper</div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-1 text-right pr-2">Peso</div>
        <div class="col-span-4 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Flujograma de Estado Intermitente (Holgado)</div>
      </div>

      <div v-if="store.loading && !store.bookings.length" class="flex-1 flex items-center justify-center">
        <span class="text-[10px] font-mono text-slate-400 animate-pulse">Cargando bookings...</span>
      </div>

      <div v-else-if="visibleBookings.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">{{ store.selectedFlightId ? 'No hay reservas para este vuelo' : 'No hay reservas. Crea una con el botón New Booking.' }}</p>
      </div>

      <div v-else class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="b in visibleBookings" :key="b.id"
          class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer">

          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 text-[10px]">
            {{ b.awbNumber || b.id?.slice(0, 8) || 'N/A' }}
          </div>

          <div class="col-span-1 font-mono font-bold text-[10px] text-slate-800 bg-white border border-slate-200 rounded px-1 py-0.5 w-max relative z-10 shadow-sm">
            {{ flightNumber(b.flightId) || '—' }}
          </div>

          <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">
            {{ b.clientName || '—' }}
          </div>

          <div class="col-span-1 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[10px]">
            {{ b.shipperName || '—' }}
          </div>

          <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
            {{ b.skids || b.units || '—' }}
          </div>

          <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
            {{ b.reservedKg ? Number(b.reservedKg).toLocaleString() : '—' }}<span class="text-[9px] text-slate-400 font-normal font-mono">k</span>
          </div>

          <div class="col-span-4 px-3 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
            <div class="flex items-center w-full justify-between relative px-4">
              <div class="absolute top-[5px] left-6 right-6 h-[3px] bg-slate-100 z-0 rounded-full overflow-hidden flex">
                <div class="h-full rounded-full transition-all duration-300"
                     :class="getLineProgressColor(b.status)"
                     :style="{ width: getProgressWidth(b.status) }">
                </div>
              </div>
              <div v-for="step in statusSteps" :key="step.key" class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="b.status === step.key" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full opacity-75" :class="step.pingColor"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="getStatusDotClass(b.status, step.key, step)"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase"
                  :class="b.status === step.key ? step.activeLabel : (statusOrder.indexOf(b.status) > statusOrder.indexOf(step.key) ? 'text-slate-500' : 'text-slate-300')">
                  {{ step.short }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CREATE MODAL -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="closeModal">
      <div class="bg-white rounded-xl border border-slate-200 shadow-2xl w-full max-w-lg p-6">
        <div class="flex justify-between items-center mb-5 pb-3 border-b border-slate-200">
          <h2 class="text-sm font-black font-mono uppercase tracking-wider text-slate-950">Nuevo Booking</h2>
          <button @click="closeModal" class="text-slate-400 hover:text-slate-700"><IconX :size="16" :stroke-width="2" /></button>
        </div>
        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Cliente *</label>
              <input v-model="form.clientName" type="text" placeholder="Nombre del agente"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Contacto *</label>
              <input v-model="form.contactName" type="text" placeholder="Persona de contacto"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Shipper</label>
              <input v-model="form.shipperName" type="text" placeholder="Nombre del shipper"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Consignee (CNEE)</label>
              <input v-model="form.cnee" type="text" placeholder="Nombre del consignatario"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Número MAWB</label>
              <input v-model="form.awbNumber" type="text" placeholder="UPS-XXX-XXXX"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition uppercase" />
            </div>
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Peso Reservado (kg) *</label>
              <input v-model.number="form.reservedKg" type="number" step="0.001"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Destino</label>
              <input v-model="form.destination" type="text" maxlength="3" placeholder="MIA"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 uppercase transition" />
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Skids</label>
                <input v-model.number="form.skids" type="number" min="0"
                  class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
              </div>
              <div>
                <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Unidades</label>
                <input v-model.number="form.units" type="number" min="0"
                  class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Tipo de Commodity</label>
              <select v-model="form.commodityType"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition">
                <option v-for="c in commodityTypes" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
            <div>
              <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Prioridad</label>
              <input v-model.number="form.priority" type="number" min="0" max="10"
                class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div>
            <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Notas</label>
            <textarea v-model="form.notes" rows="2" placeholder="Instrucciones especiales..."
              class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 outline-none focus:border-slate-950 transition resize-none"></textarea>
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6 pt-4 border-t border-slate-200">
          <button @click="closeModal"
            class="text-[10px] px-3.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-600 hover:bg-slate-50 transition">
            Cancelar
          </button>
          <button @click="saveBooking" :disabled="saving"
            class="flex items-center gap-1.5 text-[10px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition active:scale-95 disabled:opacity-50">
            <span>{{ saving ? 'Guardando...' : 'Crear Booking' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- IMPORT PREVIEW MODAL -->
    <div v-if="showImportModal" class="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="closeImportModal">
      <div class="bg-white rounded-xl border border-slate-200 shadow-2xl w-full max-w-4xl max-h-[80vh] flex flex-col">
        <div class="flex justify-between items-center px-6 py-4 border-b border-slate-200 shrink-0">
          <div>
            <h2 class="text-sm font-black font-mono uppercase tracking-wider text-slate-950">Previsualización de Importación</h2>
            <p class="text-[10px] font-mono text-slate-400 mt-0.5">{{ parsedRows.length }} registros encontrados en el archivo</p>
          </div>
          <button @click="closeImportModal" class="text-slate-400 hover:text-slate-700"><IconX :size="16" :stroke-width="2" /></button>
        </div>

        <div class="overflow-auto flex-1 min-h-0">
          <table class="w-full text-[10px] font-mono">
            <thead class="bg-slate-50 sticky top-0 z-10">
              <tr class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider">
                <th class="text-left px-4 py-2 border-b border-slate-200">#</th>
                <th class="text-left px-4 py-2 border-b border-slate-200">Cliente</th>
                <th class="text-left px-4 py-2 border-b border-slate-200">Contacto</th>
                <th class="text-left px-4 py-2 border-b border-slate-200">Shipper</th>
                <th class="text-left px-4 py-2 border-b border-slate-200">CNEE</th>
                <th class="text-center px-3 py-2 border-b border-slate-200">AWb</th>
                <th class="text-center px-3 py-2 border-b border-slate-200">Skids</th>
                <th class="text-center px-3 py-2 border-b border-slate-200">Uni</th>
                <th class="text-right px-3 py-2 border-b border-slate-200">Kg</th>
                <th class="text-center px-3 py-2 border-b border-slate-200">Dest</th>
                <th class="text-center px-3 py-2 border-b border-slate-200">Com</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="(row, idx) in parsedRows" :key="idx" class="hover:bg-slate-50 transition-colors">
                <td class="px-4 py-2 text-slate-400">{{ idx + 1 }}</td>
                <td class="px-4 py-2 font-semibold text-slate-800">{{ row.clientName }}</td>
                <td class="px-4 py-2 text-slate-600">{{ row.contactName }}</td>
                <td class="px-4 py-2 text-slate-600 truncate max-w-[120px]">{{ row.shipperName }}</td>
                <td class="px-4 py-2 text-slate-600 truncate max-w-[120px]">{{ row.cnee }}</td>
                <td class="px-3 py-2 text-center text-slate-700 font-mono">{{ row.awbNumber || '—' }}</td>
                <td class="px-3 py-2 text-center font-bold text-slate-900">{{ row.skids || '—' }}</td>
                <td class="px-3 py-2 text-center font-bold text-slate-900">{{ row.units || '—' }}</td>
                <td class="px-3 py-2 text-right font-bold text-slate-900">{{ row.reservedKg.toLocaleString() }}</td>
                <td class="px-3 py-2 text-center font-bold text-slate-700">{{ row.destination }}</td>
                <td class="px-3 py-2 text-center"><span class="inline-block text-[9px] px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 font-semibold">{{ row.commodityType }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-between items-center px-6 py-3 border-t border-slate-200 bg-slate-50 rounded-b-xl shrink-0">
          <span class="text-[10px] font-mono text-slate-400">Se crearán {{ parsedRows.length }} bookings + MAWBs automáticamente</span>
          <div class="flex gap-2">
            <button @click="closeImportModal"
              class="text-[10px] px-3.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-600 hover:bg-white transition">
              Cancelar
            </button>
            <button @click="confirmImport" :disabled="importing"
              class="flex items-center gap-1.5 text-[10px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition active:scale-95 disabled:opacity-50">
              <span>{{ importing ? 'Importando...' : `Importar ${parsedRows.length} registros` }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useAppStore } from '../stores/app'
import { IconX } from '@tabler/icons-vue'
import * as XLSX from 'xlsx'

const store = useAppStore()

const showModal = ref(false)
const saving = ref(false)

const fileInput = ref(null)
const showImportModal = ref(false)
const parsedRows = ref([])
const importing = ref(false)

const flightList = computed(() => store.flights)
const localFlightId = ref(store.selectedFlightId)

function onFlightChange() {
  if (localFlightId.value) {
    store.selectFlight(localFlightId.value)
  }
}



const COM_MAP = {
  'H/V': 'HIGH_VALUES',
  'GEN': 'GENERAL',
  'PAC': 'SMALL_PACKAGES',
  'WWEF': 'WWEF',
  'CIG': 'CIGARETTES',
  'PLAN': 'LIVE_PLANTS',
  'WAL': 'GENERAL',
  'PROD': 'GENERAL',
}

function parseCommodity(abbr) {
  const key = (abbr || '').trim().toUpperCase()
  return COM_MAP[key] || 'GENERAL'
}

function parseBookingsFromXLSX(data) {
  const rows = []
  for (let i = 2; i < data.length; i++) {
    const r = data[i]
    const clientName = String(r[0] || '').trim()
    if (!clientName || clientName === clientName.toUpperCase()) continue
    const contactName = String(r[1] || '').trim()
    const awbRaw = r[2]
    const awbNumber = awbRaw ? String(awbRaw).trim() : ''
    const skids = parseInt(r[3]) || 0
    const units = parseInt(r[4]) || 0
    const reservedKg = parseFloat(r[5]) || 0
    if (skids === 0 && units === 0 && reservedKg === 0) continue
    rows.push({
      clientName,
      contactName,
      shipperName: String(r[19] || '').trim() || contactName,
      cnee: String(r[18] || '').trim(),
      awbNumber,
      skids,
      units,
      reservedKg,
      destination: String(r[9] || '').trim().toUpperCase() || 'MIA',
      commodityType: parseCommodity(r[11]),
      priority: parseInt(r[10]) || 0,
      notes: String(r[20] || '').trim(),
    })
  }
  return rows
}

const commodityTypes = ['DRY_CARGO','ELECTRONICS','PERISHABLE','HIGH_VALUES','CIGARETTES','SMALL_PACKAGES','WWEF','LIVE_PLANTS','GENERAL','COMAT','FCC']

const statusSteps = [
  { key: 'BOOKED', short: 'BKD', pingColor: 'bg-amber-400', active: 'bg-amber-500 border-amber-600 scale-125 shadow-[0_0_6px_#f59e0b]', activeLabel: 'text-amber-600', dotDone: 'bg-slate-400 border-slate-500' },
  { key: 'RECEIVED', short: 'RCV', pingColor: 'bg-blue-400', active: 'bg-blue-500 border-blue-600 scale-125 shadow-[0_0_6px_#3b82f6]', activeLabel: 'text-blue-600', dotDone: 'bg-slate-400 border-slate-500' },
  { key: 'MANIFESTED', short: 'MNF', pingColor: 'bg-emerald-400', active: 'bg-emerald-500 border-emerald-600 scale-125 shadow-[0_0_6px_#10b981]', activeLabel: 'text-emerald-600', dotDone: 'bg-slate-400 border-slate-500' },
  { key: 'DEPARTED', short: 'DEP', pingColor: 'bg-slate-800', active: 'bg-slate-950 border-slate-950 scale-125 shadow-[0_0_6px_#0f172a]', activeLabel: 'text-slate-950', dotDone: 'bg-slate-950 border-slate-950' },
]
const statusOrder = ['BOOKED','RECEIVED','MANIFESTED','DEPARTED','ARRIVED','CANCELLED']

const visibleBookings = computed(() =>
  store.selectedFlightId
    ? store.bookings.filter(b => b.flightId === store.selectedFlightId)
    : store.bookings
)

function flightNumber(flightId) {
  if (!flightId) return '—'
  const f = store.flights.find(f => f.id === flightId)
  return f ? `UPS-${f.flightNumber}` : flightId.slice(0, 8)
}

const form = ref({
  clientName: '',
  contactName: '',
  shipperName: '',
  cnee: '',
  awbNumber: '',
  skids: 1,
  units: 0,
  reservedKg: null,
  destination: 'MIA',
  commodityType: 'GENERAL',
  priority: 0,
  notes: '',
})

const computedStats = computed(() => {
  const b = store.bookings
  return [
    { label: "Total Reservas", value: b.length, sub: "Activas en sistema", borderClass: "border-l-slate-700" },
    { label: "Booked", value: b.filter(x => x.status === 'BOOKED' || !x.status).length, sub: "Pendientes", borderClass: "border-l-amber-500" },
    { label: "Received", value: b.filter(x => x.status === 'RECEIVED' || x.status === 'MANIFESTED' || x.status === 'DEPARTED').length, sub: "En proceso", borderClass: "border-l-emerald-500" },
    { label: "Manifested", value: b.filter(x => x.status === 'MANIFESTED' || x.status === 'DEPARTED').length + '/' + b.filter(x => x.status === 'DEPARTED').length, sub: "Facturado/Volado", borderClass: "border-l-rose-500" },
  ]
})

function getStatusDotClass(currentStatus, stepKey, step) {
  if (currentStatus === stepKey) return step.active
  if (statusOrder.indexOf(currentStatus) >= statusOrder.indexOf(stepKey)) return 'bg-slate-400 border-slate-500'
  return 'bg-slate-200 border-slate-300'
}

function getProgressWidth(status) {
  if (!status || status === 'BOOKED') return '0%'
  if (status === 'RECEIVED') return '33%'
  if (status === 'MANIFESTED') return '66%'
  if (status === 'DEPARTED' || status === 'ARRIVED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'RECEIVED') return 'bg-blue-500'
  if (status === 'MANIFESTED') return 'bg-emerald-500'
  if (status === 'DEPARTED' || status === 'ARRIVED') return 'bg-slate-950'
  return 'bg-slate-200'
}

function openCreate() {
  const flightNum = store.selectedFlight?.flightNumber || 'XXX'
  form.value = {
    clientName: '', contactName: '', shipperName: '', cnee: '',
    awbNumber: `UPS-${flightNum}-${Date.now().toString(36).toUpperCase()}`,
    skids: 1, units: 0,
    reservedKg: null, destination: 'MIA', commodityType: 'GENERAL', priority: 0, notes: ''
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function triggerImport() {
  fileInput.value?.click()
}

function handleFileImport(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (ev) => {
    try {
      const data = new Uint8Array(ev.target.result)
      const wb = XLSX.read(data, { type: 'array' })
      const ws = wb.Sheets[wb.SheetNames[0]]
      const rows = XLSX.utils.sheet_to_json(ws, { header: 1, defval: '' })
      parsedRows.value = parseBookingsFromXLSX(rows)
      showImportModal.value = true
    } catch (err) {
      alert('Error al leer el archivo: ' + err.message)
    }
  }
  reader.readAsArrayBuffer(file)
  e.target.value = ''
}

function closeImportModal() {
  showImportModal.value = false
  parsedRows.value = []
}

async function confirmImport() {
  if (!store.selectedFlightId) {
    alert('Selecciona un vuelo primero')
    return
  }
  if (parsedRows.value.length === 0) return
  importing.value = true
  let success = 0
  let errors = 0
  for (const row of parsedRows.value) {
    try {
      const dto = {
        airlineId: '00000000-0000-0000-0000-000000000001',
        flightId: store.selectedFlightId,
        awbNumber: row.awbNumber,
        clientName: row.clientName,
        contactName: row.contactName,
        shipperName: row.shipperName || row.clientName,
        cnee: row.cnee,
        reservedKg: row.reservedKg,
        skids: row.skids || 1,
        units: row.units || 0,
        destination: row.destination,
        commodityType: row.commodityType,
        priority: row.priority,
        notes: row.notes,
      }
      const booking = await store.createBooking(dto)
      if (booking?.id) {
        const awbNumber = row.awbNumber || `UPS-${store.selectedFlight?.flightNumber || 'XXX'}-${Date.now().toString(36).toUpperCase()}`
        const mawb = await store.createMawb({
          airline: { id: '00000000-0000-0000-0000-000000000001' },
          flight: { id: store.selectedFlightId },
          awbNumber: awbNumber,
          shipperName: row.shipperName || row.clientName,
          consigneeName: row.cnee || row.clientName,
          origin: store.selectedFlight?.origin || 'SDQ',
          destination: row.destination || store.selectedFlight?.destination || 'MIA',
          pieces: row.skids || row.units || 1,
          reportedWeightKg: row.reservedKg,
          chargeableWeightKg: row.reservedKg,
          commodityType: row.commodityType,
          status: 'BOOKED',
        })
        if (mawb?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawb.id })
        }
      }
      success++
    } catch (e) {
      console.warn('Error importing row:', row.clientName, e.message)
      errors++
    }
  }
  await Promise.all([
    store.loadBookings(store.selectedFlightId),
    store.loadMawbs(store.selectedFlightId),
  ])
  importing.value = false
  closeImportModal()
  alert(`Importación completada: ${success} exitosos, ${errors} errores`)
}

async function saveBooking() {
  if (!form.value.clientName || !form.value.contactName || !form.value.reservedKg) {
    alert('Cliente, Contacto y Peso Reservado son obligatorios')
    return
  }
  if (!store.selectedFlightId) {
    alert('Selecciona un vuelo primero')
    return
  }
  try {
    saving.value = true
    const dto = {
      airlineId: '00000000-0000-0000-0000-000000000001',
      flightId: store.selectedFlightId,
      awbNumber: form.value.awbNumber,
      clientName: form.value.clientName,
      contactName: form.value.contactName,
      shipperName: form.value.shipperName || form.value.clientName,
      cnee: form.value.cnee,
      reservedKg: form.value.reservedKg || 0,
      skids: form.value.skids || 1,
      units: form.value.units || 0,
      destination: form.value.destination,
      commodityType: form.value.commodityType,
      priority: form.value.priority,
      notes: form.value.notes,
    }
    const booking = await store.createBooking(dto)
    // Auto-create MAWB from booking so it appears in MawbsView/WarehouseReceiptsView/UldsView
    if (booking?.id) {
      const awbNumber = form.value.awbNumber || `UPS-${store.selectedFlight?.flightNumber || 'XXX'}-${Date.now().toString(36).toUpperCase()}`
      try {
        const mawb = await store.createMawb({
          airline: { id: '00000000-0000-0000-0000-000000000001' },
          flight: { id: store.selectedFlightId },
          awbNumber: awbNumber,
          shipperName: form.value.shipperName || form.value.clientName,
          consigneeName: form.value.cnee || form.value.clientName,
          origin: store.selectedFlight?.origin || 'SDQ',
          destination: form.value.destination || store.selectedFlight?.destination || 'MIA',
          pieces: form.value.skids || form.value.units || 1,
          reportedWeightKg: form.value.reservedKg || 0,
          chargeableWeightKg: form.value.reservedKg || 0,
          commodityType: form.value.commodityType || 'GENERAL',
          status: 'BOOKED',
        })
        // Link booking to MAWB
        if (mawb?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawb.id })
        }
      } catch (e2) {
        console.warn('MAWB creation non-critical:', e2.message)
      }
    }
    await Promise.all([
      store.loadBookings(store.selectedFlightId),
      store.loadMawbs(store.selectedFlightId),
    ])
    closeModal()
  } catch (e) {
    alert('Error: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!store.flights.length) {
    await store.loadFlights()
  }
  if (store.selectedFlightId) {
    localFlightId.value = store.selectedFlightId
  }
  store.loadBookings()
})

watch(() => store.selectedFlightId, (id) => {
  localFlightId.value = id
})
</script>

<style scoped>
.border-slate-150 { border-color: rgba(226, 232, 240, 0.6); }
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine {
  box-shadow:
    0px 1px 2px rgba(15, 32, 67, 0.08),
    1px 3px 6px rgba(15, 32, 67, 0.06),
    3px 6px 12px rgba(15, 32, 67, 0.04);
}
.pencil-sketch {
  position: relative;
  overflow: hidden;
  transition: transform 0.15s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.15s cubic-bezier(0.16, 1, 0.3, 1);
}
.pencil-sketch::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.22;
  transition: opacity 0.2s ease;
  pointer-events: none;
  background-image:
    repeating-linear-gradient(45deg, rgba(71, 85, 105, 0.07) 0px, rgba(71, 85, 105, 0.07) 0.6px, transparent 0.6px, transparent 3px);
}
.pencil-sketch:hover {
  transform: translate(-0.5px, -0.5px);
  box-shadow:
    0px 1px 2px rgba(15, 32, 67, 0.12),
    2px 5px 8px rgba(15, 32, 67, 0.09),
    4px 10px 16px rgba(15, 32, 67, 0.06);
}
.pencil-sketch:hover::before {
  opacity: 0.6;
  background-image:
    repeating-linear-gradient(45deg, rgba(15, 32, 67, 0.08) 0px, rgba(15, 32, 67, 0.08) 0.7px, transparent 0.7px, transparent 2.5px),
    repeating-linear-gradient(-45deg, rgba(15, 32, 67, 0.04) 0px, rgba(15, 32, 67, 0.04) 0.7px, transparent 0.7px, transparent 2.5px);
}
.row-pencil {
  position: relative;
  transition: background-color 0.15s ease, box-shadow 0.15s ease;
}
.row-pencil::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.12s ease-in-out;
  pointer-events: none;
  background-image:
    repeating-linear-gradient(45deg, rgba(15, 32, 67, 0.05) 0px, rgba(15, 32, 67, 0.05) 0.6px, transparent 0.6px, transparent 2px);
}
.row-pencil:hover {
  background-color: rgba(243, 247, 254, 0.6);
  box-shadow:
    inset 3px 0 0 0 #0f172a,
    0px 2px 6px rgba(15, 32, 67, 0.03);
}
.row-pencil:hover::before {
  opacity: 1;
}
</style>
