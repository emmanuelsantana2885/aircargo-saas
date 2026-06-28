<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex justify-between items-center border-b border-slate-400 pb-3 shrink-0">
      <div class="flex items-center gap-4">
        <div>
          <h1 class="text-xs font-black tracking-tight text-slate-950 uppercase font-mono">Bookings Hub</h1>
          <p class="text-xs font-mono text-slate-950 mt-0.5 uppercase tracking-widest font-bold">SDQ Control Desk</p>
        </div>
        <div class="h-8 w-[1px] bg-slate-400"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-xs font-black text-slate-950 uppercase tracking-widest">Vuelo</span>
          <select v-model="localFlightId" @change="onFlightChange"
            class="bg-slate-100 border border-slate-300 rounded px-3 py-1.5 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-sm cursor-pointer min-w-[160px]">
            <option value="" disabled>Seleccionar vuelo</option>
            <option v-for="flight in flightList" :key="flight.id" :value="flight.id">
              UPS-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }}) — {{ flight.flightDate }}
            </option>
          </select>
        </div>
        <div v-if="store.selectedFlight" class="flex gap-3 text-xs font-mono font-bold text-slate-950">
          <span>{{ store.selectedFlight.aircraftReg || '—' }}</span>
          <span>{{ store.selectedFlight.flightDate }}</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button @click="triggerImport"
          class="flex items-center gap-1.5 text-[16px] px-3 py-2 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine text-slate-950 hover:bg-slate-100">
          <span class="text-sm font-semibold leading-none">↑</span> Import XLSX
        </button>
        <button @click="openCreate"
          class="flex items-center gap-1.5 text-[16px] px-3 py-2 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine bg-slate-950 text-white hover:bg-slate-800">
          <span class="text-sm font-semibold leading-none">+</span> New Booking
        </button>
        <input type="file" ref="fileInput" @change="handleFileImport" accept=".xlsx,.xls" class="hidden" />
      </div>
    </header>

    <section class="grid grid-cols-1 md:grid-cols-4 gap-4 my-4 shrink-0">
      <div v-for="stat in computedStats" :key="stat.label"
        :class="stat.borderClass"
        class="pencil-sketch py-2.5 px-4 rounded bg-white border border-slate-400 shadow-pencil-marine transition-all flex flex-col justify-between min-h-[78px] cursor-pointer">
        <div class="relative z-10">
          <h3 class="text-[15px] font-black text-slate-950 uppercase tracking-wider font-mono">{{ stat.label }}</h3>
          <div class="flex items-baseline gap-1 mt-0.5">
            <div class="text-2xl font-mono font-black tracking-tight text-slate-950">{{ stat.value }}</div>
          </div>
        </div>
        <div class="pt-1 border-t border-slate-150 text-[15px] font-mono text-slate-950 relative z-10 truncate">
          {{ stat.sub }}
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-950 border-b border-slate-700 text-xs font-bold text-white uppercase tracking-wider grid grid-cols-12 py-3 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Booking ID</div>
        <div class="col-span-2 text-left">Vuelo / Fecha</div>
        <div class="col-span-2 text-left">Agente / Broker</div>
        <div class="col-span-2 text-left">Shipper <span class="text-slate-300 font-normal">(Recibo)</span></div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-1 text-right pr-2">Peso</div>
        <div class="col-span-1 text-center bg-slate-800 py-0.5 rounded border border-slate-600 text-white font-black tracking-wide">Estatus MAWB</div>
        <div class="col-span-1"></div>
      </div>

      <div v-if="store.loading && !store.bookings.length" class="flex-1 flex items-center justify-center">
        <span class="text-[16px] font-mono text-slate-950 animate-pulse">Cargando bookings...</span>
      </div>

      <div v-else-if="deduplicatedBookings.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[16px] font-mono text-slate-950 uppercase tracking-widest">{{ store.selectedFlightId ? 'No hay reservas para este vuelo' : 'No hay reservas. Crea una con el botón New Booking.' }}</p>
      </div>

      <div v-else class="divide-y divide-slate-400 text-xs text-slate-950 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="b in deduplicatedBookings" :key="b.id"
          class="row-pencil grid grid-cols-12 items-center py-3.5 px-5 transition-all duration-150 cursor-pointer">

          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 text-xs flex items-center gap-2">
            <span>{{ b.awbNumber || b.id?.slice(0, 8) || 'N/A' }}</span>
            <span v-if="b._dupCount > 1" class="inline-flex items-center justify-center min-w-[18px] h-[18px] px-1 rounded-full bg-amber-100 text-amber-700 text-[10px] font-bold" title="Reservas duplicadas agrupadas">{{ b._dupCount }}x</span>
          </div>

          <div class="col-span-2 font-mono font-bold text-[16px] text-slate-950 relative z-10 flex flex-col leading-tight">
            <span>{{ flightNumber(b.flightId) || '—' }}</span>
            <span v-if="b.flightId" class="text-[14px] text-slate-500 font-semibold">{{ flightDate(b.flightId) }}</span>
          </div>

          <div class="col-span-2 text-slate-950 font-semibold relative z-10 truncate pr-3">
            {{ b.clientName || '—' }}
          </div>

          <div class="col-span-2 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-xs flex flex-col leading-tight">
            <span>{{ bookingReceipt(b)?.shipperName || b.shipperName || '—' }}</span>
            <span v-if="bookingReceipt(b)" class="text-xs text-emerald-600 font-semibold">&#10003; Recibido</span>
          </div>

          <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
            <span v-if="bookingReceipt(b)">{{ bookingReceipt(b).pieceCount || '—' }}</span>
            <span v-else>{{ b.skids || b.units || '—' }}</span>
          </div>

          <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
            <template v-if="bookingReceipt(b)">
              {{ Number(bookingReceipt(b).chargeableWeightKg || bookingReceipt(b).actualWeightKg || 0).toLocaleString() }}<span class="text-xs text-slate-950 font-normal font-mono">k</span>
            </template>
            <template v-else>
              {{ b.reservedKg ? Number(b.reservedKg).toLocaleString() : '—' }}<span class="text-xs text-slate-950 font-normal font-mono">k</span>
            </template>
          </div>

          <div class="col-span-1 flex items-center justify-center gap-1.5 relative z-10">
            <div class="flex items-center gap-2 text-[15px] font-mono" :title="'MAWB: ' + getMawbStatus(b)">
              <span class="inline-block w-2.5 h-2.5" :class="getMawbStatusClass(b)"></span>
              <span :class="getMawbStatusTextClass(b)" class="font-bold text-xs uppercase tracking-wider">{{ getMawbStatus(b) }}</span>
              <span v-if="getMawbStatus(b) !== '—'" class="text-slate-300 text-xs">·</span>
            </div>
          </div>
          <div class="col-span-1 flex justify-end relative z-10">
            <button @click.stop="removeBooking(b)"
              class="text-slate-400 hover:text-red-600 transition-colors p-1"
              title="Eliminar booking">
              <IconTrash :size="15" :stroke-width="1.5" />
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- CREATE MODAL -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="closeModal">
      <div class="bg-white rounded-xl border border-slate-400 shadow-2xl w-full max-w-lg p-6">
        <div class="flex justify-between items-center mb-5 pb-3 border-b border-slate-400">
          <h2 class="text-xs font-black font-mono uppercase tracking-wider text-slate-950">Nuevo Booking</h2>
          <button @click="closeModal" class="text-slate-950 hover:text-slate-950"><IconX :size="16" :stroke-width="2" /></button>
        </div>
        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Cliente *</label>
              <input v-model="form.clientName" type="text" placeholder="Nombre del agente"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Contacto *</label>
              <input v-model="form.contactName" type="text" placeholder="Persona de contacto"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Shipper</label>
              <input v-model="form.shipperName" type="text" placeholder="Nombre del shipper"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Consignee (CNEE)</label>
              <input v-model="form.cnee" type="text" placeholder="Nombre del consignatario"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Número MAWB</label>
              <input v-model="form.awbNumber" type="text" placeholder="UPS-XXX-XXXX"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition uppercase" />
            </div>
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Peso Reservado (kg) *</label>
              <input v-model.number="form.reservedKg" type="number" step="0.001"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Destino</label>
              <input v-model="form.destination" type="text" maxlength="3" placeholder="MIA"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 uppercase transition" />
            </div>
            <div class="grid grid-cols-2 gap-2">
              <div>
                <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Skids</label>
                <input v-model.number="form.skids" type="number" min="0"
                  class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
              </div>
              <div>
                <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Unidades</label>
                <input v-model.number="form.units" type="number" min="0"
                  class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
              </div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Tipo de Commodity</label>
              <select v-model="form.commodityType"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition">
                <option v-for="c in commodityTypes" :key="c" :value="c">{{ c }}</option>
              </select>
            </div>
            <div>
              <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Prioridad</label>
              <input v-model.number="form.priority" type="number" min="0" max="10"
                class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition" />
            </div>
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Notas</label>
            <textarea v-model="form.notes" rows="2" placeholder="Instrucciones especiales..."
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 outline-none focus:border-slate-950 transition resize-none"></textarea>
          </div>
        </div>
        <div class="flex justify-end gap-2 mt-6 pt-4 border-t border-slate-400">
          <button @click="closeModal"
            class="text-[16px] px-3.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-slate-100 transition">
            Cancelar
          </button>
          <button @click="saveBooking" :disabled="saving"
            class="flex items-center gap-1.5 text-[16px] px-5 py-2 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition active:scale-95 disabled:opacity-50">
            <span>{{ saving ? 'Guardando...' : 'Crear Booking' }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- IMPORT PREVIEW MODAL -->
    <div v-if="showImportModal" class="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="closeImportModal">
      <div class="bg-white rounded-xl border border-slate-400 shadow-2xl w-full max-w-4xl max-h-[80vh] flex flex-col">
        <div class="flex justify-between items-center px-6 py-4 border-b border-slate-400 shrink-0">
          <div>
            <h2 class="text-xs font-black font-mono uppercase tracking-wider text-slate-950">Previsualización de Importación</h2>
            <p class="text-xs font-mono text-slate-950 mt-0.5">{{ parsedRows.length }} registros encontrados en el archivo</p>
          </div>
          <button @click="closeImportModal" class="text-slate-950 hover:text-slate-950"><IconX :size="16" :stroke-width="2" /></button>
        </div>

        <div class="overflow-auto flex-1 min-h-0">
          <table class="w-full text-xs font-mono">
            <thead class="bg-slate-100 sticky top-0 z-10">
              <tr class="text-xs font-black text-slate-950 uppercase tracking-wider">
                <th class="text-left px-5 py-3 border-b border-slate-400">#</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Cliente</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Contacto</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">Shipper</th>
                <th class="text-left px-5 py-3 border-b border-slate-400">CNEE</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">AWb</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Skids</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Uni</th>
                <th class="text-right px-4 py-3 border-b border-slate-400">Kg</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Dest</th>
                <th class="text-center px-4 py-3 border-b border-slate-400">Com</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-300">
              <tr v-for="(row, idx) in parsedRows" :key="idx" class="hover:bg-slate-100 transition-colors">
                <td class="px-5 py-3 text-slate-950">{{ idx + 1 }}</td>
                <td class="px-5 py-3 font-semibold text-slate-950">{{ row.clientName }}</td>
                <td class="px-5 py-3 text-slate-950">{{ row.contactName }}</td>
                <td class="px-5 py-3 text-slate-950 truncate max-w-[120px]">{{ row.shipperName }}</td>
                <td class="px-5 py-3 text-slate-950 truncate max-w-[120px]">{{ row.cnee }}</td>
                <td class="px-4 py-3 text-center text-slate-950 font-mono">{{ row.awbNumber || '—' }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-900">{{ row.skids || '—' }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-900">{{ row.units || '—' }}</td>
                <td class="px-4 py-3 text-right font-bold text-slate-900">{{ row.reservedKg.toLocaleString() }}</td>
                <td class="px-4 py-3 text-center font-bold text-slate-950">{{ row.destination }}</td>
                <td class="px-4 py-3 text-center"><span class="inline-block text-xs px-1.5 py-0.5 rounded bg-slate-100 text-slate-950 font-semibold">{{ row.commodityType }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="flex justify-between items-center px-6 py-4 border-t border-slate-400 bg-slate-100 rounded-b-xl shrink-0">
          <span class="text-xs font-mono text-slate-950">Se crearán {{ parsedRows.length }} bookings + MAWBs automáticamente</span>
          <div class="flex gap-2">
            <button @click="closeImportModal"
              class="text-[16px] px-3.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-white transition">
              Cancelar
            </button>
            <button @click="confirmImport" :disabled="importing"
              class="flex items-center gap-1.5 text-[16px] px-5 py-2 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition active:scale-95 disabled:opacity-50">
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
import { IconX, IconTrash } from '@tabler/icons-vue'
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

function normalizeAwb(raw) {
  let s = String(raw || '').replace(/[\s\-_\/]/g, '')
  if (/^\d{11}$/.test(s)) {
    s = s.slice(0, 3) + '-' + s.slice(3)
  }
  return s
}

function parseBookingsFromXLSX(data) {
  const rows = []
  for (let i = 2; i < data.length; i++) {
    const r = data[i]
    const clientName = String(r[0] || '').trim()
    if (!clientName || clientName === clientName.toUpperCase()) continue
    const contactName = String(r[1] || '').trim()
    const awbRaw = r[2]
    const awbNumber = normalizeAwb(awbRaw)
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

const visibleBookings = computed(() =>
  store.selectedFlightId
    ? store.bookings.filter(b => b.flightId === store.selectedFlightId)
    : store.bookings
)

const deduplicatedBookings = computed(() => {
  const groups = {}
  for (const b of visibleBookings.value) {
    const key = b.mawbId || b.awbNumber || b.id
    if (!groups[key]) {
      groups[key] = { booking: b, count: 1 }
    } else {
      groups[key].count++
      if ((Number(b.skids) || 0) > (Number(groups[key].booking.skids) || 0)) {
        groups[key].booking = b
      }
    }
  }
  return Object.values(groups).map(g => ({ ...g.booking, _dupCount: g.count }))
})

function flightNumber(flightId) {
  if (!flightId) return '—'
  const f = store.flights.find(f => f.id === flightId)
  return f ? `UPS-${f.flightNumber}` : flightId.slice(0, 8)
}

function flightDate(flightId) {
  if (!flightId) return ''
  const f = store.flights.find(f => f.id === flightId)
  return f ? f.flightDate : ''
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

function bookingMawb(b) {
  if (!b.mawbId) return null
  return store.mawbs.find(m => m.id === b.mawbId || m.flightId === b.flightId && m.awbNumber === b.awbNumber) || null
}

function bookingReceipt(b) {
  const m = bookingMawb(b)
  if (!m) return null
  return store.receipts.find(r => r.mawb?.id === m.id || r.mawbId === m.id) || null
}

function getMawbStatus(b) {
  const m = bookingMawb(b)
  if (!m) return '—'
  return m.status || 'BOOKED'
}

function getMawbStatusClass(b) {
  const s = getMawbStatus(b)
  if (s === 'BOOKED' || s === '—') return 'bg-amber-500'
  if (s === 'RECEIVED') return 'bg-blue-500'
  if (s === 'MANIFESTED') return 'bg-emerald-500'
  if (s === 'DEPARTED' || s === 'ARRIVED') return 'bg-slate-950'
  return 'bg-slate-300'
}

function getMawbStatusTextClass(b) {
  const s = getMawbStatus(b)
  if (s === '—') return 'text-slate-300'
  if (s === 'BOOKED') return 'text-amber-600'
  if (s === 'RECEIVED') return 'text-blue-600'
  if (s === 'MANIFESTED') return 'text-emerald-600'
  if (s === 'DEPARTED' || s === 'ARRIVED') return 'text-slate-950'
  return 'text-slate-400'
}

const computedStats = computed(() => {
  const b = deduplicatedBookings.value
  const receivedCount = b.filter(x => {
    const s = getMawbStatus(x)
    return s === 'RECEIVED' || s === 'MANIFESTED' || s === 'DEPARTED' || s === 'ARRIVED'
  }).length
  const pct = b.length > 0 ? Math.round(receivedCount / b.length * 100) : 0
  return [
    { label: "Total Reservas", value: b.length, sub: "MAWBs únicos en vista", borderClass: "border-l-slate-700" },
    { label: "Booked", value: b.filter(x => getMawbStatus(x) === 'BOOKED' || getMawbStatus(x) === '—').length, sub: "Pendientes", borderClass: "border-l-amber-500" },
    { label: "Received", value: receivedCount, sub: pct + "% del total", borderClass: "border-l-emerald-500" },
    { label: "Manifested", value: b.filter(x => getMawbStatus(x) === 'MANIFESTED' || getMawbStatus(x) === 'DEPARTED' || getMawbStatus(x) === 'ARRIVED').length, sub: "Facturado/Volado", borderClass: "border-l-rose-500" },
  ]
})

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
  let idx = -1
  let success = 0
  let errors = 0
  for (const row of parsedRows.value) {
    idx++
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
        const awbNumber = row.awbNumber || `406-${(Date.now() + idx).toString().slice(-8).padStart(8, '0')}`
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
        const mawbData = mawb.mawb || mawb
        if (mawb.weightWarning) {
          console.warn('⚠', mawb.weightWarning)
        }
        if (mawbData?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawbData.id })
        }
      }
      success++
    } catch (e) {
      const apiMsg = e.response?.data?.error || e.response?.data?.message || ''
      console.warn('Error importing row:', row.clientName, e.message, apiMsg)
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
      const awbNumber = form.value.awbNumber || `406-${Date.now().toString().slice(-8).padStart(8, '0')}`
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
        const mawbData = mawb.mawb || mawb
        if (mawb.weightWarning) {
          console.warn('⚠', mawb.weightWarning)
        }
        if (mawbData?.id) {
          await store.updateBooking(booking.id, { ...dto, mawbId: mawbData.id })
        }
      } catch (e2) {
        const apiMsg = e2.response?.data?.error || e2.response?.data?.message || ''
        console.warn('MAWB creation non-critical:', e2.message, apiMsg)
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

async function removeBooking(b) {
  const keys = b._dupCount > 1 ? store.bookings.filter(x => (x.mawbId === b.mawbId) || (!b.mawbId && x.awbNumber === b.awbNumber)).map(x => x.clientName).filter(Boolean) : []
  const msg = keys.length > 1
    ? `¿Eliminar ${keys.length} bookings agrupados (${keys.join(', ')})?`
    : `¿Eliminar booking de ${b.clientName || '—'} (${b.awbNumber || b.id?.slice(0, 8) || 'N/A'})?`
  if (!confirm(msg)) return
  try {
    if (b._dupCount > 1) {
      const group = store.bookings.filter(x => (x.mawbId === b.mawbId) || (!b.mawbId && x.awbNumber === b.awbNumber))
      await Promise.all(group.map(x => store.deleteBooking(x.id).catch(() => {})))
    } else {
      await store.deleteBooking(b.id)
    }
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.message || e.message
    alert('Error al eliminar: ' + msg)
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
  store.loadAllMawbs()
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
