<template>
  <div class="p-4 bg-slate-100 h-screen max-h-screen flex flex-col justify-between text-slate-900 font-mono antialiased overflow-hidden select-none">

    <header class="flex justify-between items-center border border-slate-300 bg-white p-3 rounded-t-lg shadow-pencil-marine shrink-0">
      <div class="flex items-center gap-6">
        <div class="flex flex-col gap-0.5">
          <span class="text-[14px] font-black text-slate-950 uppercase tracking-widest">Fecha</span>
          <input v-model="selectedDate" type="date" @change="onDateChange"
            class="bg-slate-100 border border-slate-300 rounded px-3 py-1.5 font-black text-slate-950 focus:outline-none text-base cursor-pointer w-[140px]" />
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[14px] font-black text-slate-950 uppercase tracking-widest">Flight Number</span>
          <select v-model="selectedFlightId" @change="syncFlightMetadata" class="bg-slate-100 border border-slate-300 rounded px-3 py-1.5 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-base cursor-pointer min-w-[180px]">
            <option value="" disabled>Seleccionar vuelo</option>
            <option v-for="flight in flightDatabase" :key="flight.id" :value="flight.id">
              UPS-{{ flight.flightNumber }} ({{ flight.origin }}→{{ flight.destination }})
            </option>
          </select>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col justify-center">
          <span class="text-[14px] font-black text-slate-950 uppercase tracking-widest">Aircraft-Tail</span>
          <span class="text-base font-black text-slate-950 uppercase tracking-wider">{{ activeFlightMeta.aircraftReg || '-' }}</span>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col justify-center">
          <span class="text-[14px] font-black text-slate-950 uppercase tracking-widest">Ruta</span>
          <span class="text-base font-black text-slate-950 uppercase tracking-widest">{{ (activeFlightMeta.origin || '') + '→' + (activeFlightMeta.destination || '-') }}</span>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button v-if="activeFlightMeta.id && activeFlightMeta.status !== 'DEPARTED' && activeFlightMeta.status !== 'ARRIVED' && activeFlightMeta.status !== 'CANCELLED'"
          @click="dispatchFlight"
          class="bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1 rounded text-[15px] font-black uppercase tracking-widest transition-all shadow-sm">
          ✈ Despachar
        </button>
        <button @click="triggerImport" class="bg-slate-100 hover:bg-slate-200 border border-slate-300 text-slate-950 px-3 py-1 rounded text-[15px] font-black uppercase tracking-widest transition-all">Import XLSX</button>
        <button @click="exportToXLSX" class="bg-slate-950 hover:bg-slate-900 text-white px-3 py-1 rounded text-[15px] font-black uppercase tracking-widest transition-all">Export XLSX</button>
      </div>
    </header>

    <!-- ULD Position Summary -->
    <section v-if="activeManifest.length > 0"
      class="flex items-center gap-3 py-1.5 px-3 bg-white border border-slate-400 rounded-lg mb-1 shadow-sm shrink-0 overflow-x-auto lp-scroll-x text-[15px] font-mono">
      <span class="font-black uppercase tracking-wider text-slate-950 text-[10.5px] shrink-0">POSITIONS</span>
      <div v-for="p in positionSummary" :key="p.pos"
        class="flex items-center gap-1.5 px-2 py-0.5 rounded whitespace-nowrap"
        :class="[p.isBelly ? 'bg-amber-50 border border-amber-200' : 'bg-slate-100 border border-slate-400']">
        <span class="font-black text-slate-950">{{ p.pos }}</span>
        <span class="text-slate-950">{{ p.count }} ULD</span>
        <span class="text-slate-950 text-[13px]">({{ p.pcs }} pcs)</span>
        <span v-if="p.isBelly" class="text-[13px] font-bold text-amber-600 bg-amber-100 px-1 rounded">BELLY</span>
      </div>
      <span class="text-slate-300 mx-1">|</span>
      <span class="text-slate-950 font-bold whitespace-nowrap">A/C: {{ aircraftType }}</span>
      <span class="text-slate-950 font-bold whitespace-nowrap">Total ULDs: {{ activeManifest.length }}</span>
    </section>

    <!-- Flight ULD Cards -->
    <section v-if="activeFlightMeta.id && (activeManifest.length > 0 || floatingUlds.length > 0)"
      class="floating-drop-zone flex gap-2 py-2 overflow-x-auto shrink-0 lp-scroll-x"
      :class="dragOverFloating ? 'ring-2 ring-amber-400 ring-offset-2 rounded-lg' : ''">
      <div v-for="(uldGroup, uIdx) in activeManifest" :key="uIdx"
        draggable="true"
        @dragstart="onDragStart(uldGroup.uldId, $event)"
        class="flex-shrink-0 bg-white border rounded-lg px-3 py-2 shadow-sm flex items-center gap-3 text-[16px]"
        :class="getCardBorderStyle(uldGroup.status)">
        <span class="font-black text-slate-950 uppercase tracking-tight">{{ uldGroup.uld }}</span>
        <span class="h-2 w-2 rounded-full" :class="getStatusDotColor(uldGroup.status)"></span>
        <span class="text-slate-950 font-bold uppercase text-[14px]">{{ uldGroup.status }}</span>
        <span class="text-slate-950 font-bold">{{ uldGroup.items.length }} MAWB</span>
        <span class="text-slate-950">{{ (uldGroup.weight || 0).toLocaleString() }} lb</span>
        <select :value="uldGroup.flightId" @change="onTransferRequest(uldGroup.uldId, uldGroup.uld, uldGroup.flightId, $event.target.value)"
          class="ml-1 bg-slate-100 border border-slate-400 rounded px-2 py-1 text-[14px] font-bold text-slate-950 focus:outline-none cursor-pointer">
          <option v-for="f in uldsStore.flights" :key="f.id" :value="f.id">
            UPS-{{ f.flightNumber }}
          </option>
        </select>
      </div>
      <div v-if="floatingUlds.length > 0" class="border-l border-slate-400 pl-2 flex gap-2">
        <div v-for="uld in floatingUlds" :key="uld.id"
          draggable="true"
          @dragstart="onDragStart(uld.id, $event)"
          class="flex-shrink-0 bg-amber-50 border border-amber-200 border-dashed rounded-lg px-3 py-2 shadow-sm flex items-center gap-2 text-[16px] cursor-grab active:cursor-grabbing select-none">
          <span class="font-black text-amber-800 uppercase tracking-tight">{{ uld.uldNumber || 'SIN-ULD' }}</span>
          <span class="text-amber-500 text-[14px] font-bold">SIN VUELO</span>
          <select :value="uld.flightId" @change="onTransferRequest(uld.id, uld.uldNumber, null, $event.target.value)"
            class="bg-white border border-amber-200 rounded px-2 py-1 text-[14px] font-bold text-slate-950 focus:outline-none cursor-pointer">
            <option value="" disabled>Asignar vuelo</option>
            <option v-for="f in uldsStore.flights" :key="f.id" :value="f.id">
              UPS-{{ f.flightNumber }}
            </option>
          </select>
        </div>
      </div>
    </section>

    <section @dragover.prevent="dragOver = true" @dragleave="onDragLeave" @drop="onDrop"
      class="flex-1 min-h-0 border border-slate-300 rounded-lg overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1 transition-shadow duration-150"
      :class="dragOver ? 'ring-2 ring-amber-400 ring-offset-2' : ''">
      <div class="overflow-x-auto flex flex-col flex-1 min-h-0 lp-scroll-x">
      <div class="bg-slate-950 text-white text-[14px] font-black uppercase tracking-wider lp-grid py-3.5 px-5 items-center shrink-0 border-b border-slate-600 whitespace-nowrap shadow-sm">
        <span class="flex items-center gap-1">
          <svg class="w-2.5 h-2.5 text-slate-950" viewBox="0 0 8 8" fill="none"><circle cx="2" cy="2" r="1" fill="currentColor"/><circle cx="6" cy="2" r="1" fill="currentColor"/><circle cx="2" cy="6" r="1" fill="currentColor"/><circle cx="6" cy="6" r="1" fill="currentColor"/></svg>
          ULD
        </span>
        <span class="text-center">PCS</span>
        <span class="text-center">%</span>
        <span class="text-center">GROSS</span>
        <span class="text-center">TARE</span>
        <span class="text-center">TYPE</span>
        <span class="text-center">SEAL</span>
        <span class="text-center">POS</span>
        <span class="text-center">DESC</span>
        <span class="text-center">MAWB</span>
        <span class="text-center">DEST</span>
      </div>

      <div v-if="activeManifest.length === 0"
        class="flex-1 flex flex-col items-center justify-center text-slate-950 text-base gap-2"
        :class="dragOver ? 'bg-amber-50' : ''">
        <span>Select a flight to view the load plan</span>
        <span v-if="floatingUlds.length > 0" class="text-[16px] text-slate-300">— o arrastra un ULD flotante aquí —</span>
      </div>

      <div v-else class="divide-y divide-slate-200 text-[14px] overflow-y-auto flex-1 min-h-0 scrollbar-none bg-slate-100/30">
        <div v-for="(uldGroup, uIdx) in activeManifest" :key="uIdx"
          class="bg-white lp-container-block"
          :class="getRowBgStyle(uldGroup.status)">
          <div v-if="uldGroup.items.length === 0" class="lp-grid py-2 px-5 items-center text-slate-950">
            <span
              @pointerdown="onTableUldPointerDown(uldGroup.uldId, $event)"
              class="font-semibold text-slate-950 truncate cursor-grab active:cursor-grabbing select-none">{{ uldGroup.uld }}</span>
            <span class="text-center">0</span>
            <span class="text-center">-</span>
            <span class="text-center">{{ (uldGroup.weight || 0).toLocaleString() }}</span>
            <span class="text-center">{{ (uldGroup.tara || 0).toLocaleString() }}</span>
            <span class="text-center">{{ uldGroup.config }}</span>
            <span class="text-center truncate">{{ uldGroup.sello || '-' }}</span>
            <span class="text-center">
              <input :value="uldGroup.pos || ''" @blur="e => updatePosition(uldGroup.uldId, e.target.value)"
                @keydown.enter="e => { e.target.blur(); updatePosition(uldGroup.uldId, e.target.value) }"
                class="w-full bg-transparent outline-none border-b border-transparent focus:border-slate-400 text-center text-[14px] font-mono" />
            </span>
            <span class="text-center text-slate-950 italic">—</span>
            <span class="text-center text-slate-950 italic">empty</span>
            <span class="text-center">-</span>
          </div>
          <div v-for="(item, iIdx) in uldGroup.items" :key="iIdx" class="lp-grid py-2 px-5 items-center border-b border-slate-300 last:border-b-0">
            <span v-if="iIdx === 0"
              @pointerdown="onTableUldPointerDown(uldGroup.uldId, $event)"
              class="font-semibold text-slate-950 truncate cursor-grab active:cursor-grabbing select-none">{{ uldGroup.uld }}</span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span class="text-center">{{ item.pcs }}</span>
            <span class="text-center">{{ item.volumePct ? item.volumePct + '%' : '-' }}</span>
            <span v-if="iIdx === 0" class="text-center">{{ (uldGroup.weight || 0).toLocaleString() }}</span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span v-if="iIdx === 0" class="text-center">{{ (uldGroup.tara || 0).toLocaleString() }}</span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span v-if="iIdx === 0" class="text-center">{{ uldGroup.config }}</span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span v-if="iIdx === 0" class="text-center truncate">{{ uldGroup.sello || '-' }}</span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span v-if="iIdx === 0" class="text-center">
              <input :value="uldGroup.pos || ''" @blur="e => updatePosition(uldGroup.uldId, e.target.value)"
                @keydown.enter="e => { e.target.blur(); updatePosition(uldGroup.uldId, e.target.value) }"
                class="w-full bg-transparent outline-none border-b border-transparent focus:border-slate-400 text-center text-[14px] font-mono" />
            </span>
            <span v-else class="text-slate-200 text-center">—</span>
            <span class="text-center font-mono truncate">{{ item.description }}</span>
            <span class="text-center font-mono truncate">{{ item.mawb }}</span>
            <span class="text-center">{{ item.destino }}</span>
          </div>
        </div>
      </div>
      </div>
    </section>

    <footer class="p-3 border border-slate-400 bg-white rounded-b-lg shrink-0 flex flex-col gap-1 text-[15px]">
      <!-- Undo toast -->
      <div class="flex justify-between items-center">
        <div class="flex gap-4">
          <span><strong>ULDs:</strong> {{ calculatedTotals.uldsCount }}</span>
          <span><strong>Total PCS:</strong> {{ calculatedTotals.pcs.toLocaleString() }}</span>
          <span><strong>Gross:</strong> {{ calculatedTotals.gross.toLocaleString() }} lbs</span>
          <span><strong>Tare:</strong> {{ calculatedTotals.tare.toLocaleString() }} lbs</span>
          <span><strong>Payload:</strong> {{ calculatedTotals.payloadPct }}%</span>
        </div>
        <div class="flex gap-2">
          <span class="inline-block w-2.5 h-2.5 rounded-sm bg-pink-100/60"></span> Left Behind
          <span class="inline-block w-2.5 h-2.5 rounded-sm bg-amber-100/40"></span> Incomplete
          <span class="inline-block w-2.5 h-2.5 rounded-sm bg-slate-100/80"></span> In Ramp
        </div>
      </div>
    </footer>
    <input type="file" ref="fileInput" @change="handleFileImport" accept=".xlsx, .xls" class="hidden" />

    <!-- Flight picker for ULDs without reassignment history -->
    <div v-if="pendingFlightPick"
      class="fixed bottom-16 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 bg-white border border-slate-300 shadow-lg rounded-lg px-4 py-2.5 text-[14px]">
      <span class="text-slate-900">
        ULD <strong>{{ pendingFlightPick.uldNumber }}</strong>
      </span>
      <template v-if="!showFlightPicker">
        <button @click="showFlightPicker = true"
          class="bg-slate-950 hover:bg-slate-900 text-white font-bold px-3 py-1 rounded text-[16px] uppercase tracking-wider transition-all whitespace-nowrap">
          Asignar a otro vuelo
        </button>
        <button @click="detachToFloating"
          class="bg-slate-100 hover:bg-slate-200 border border-slate-300 text-slate-950 font-bold px-3 py-1 rounded text-[16px] uppercase tracking-wider transition-all whitespace-nowrap">
          Dejar flotante
        </button>
        <button @click="cancelFlightPick"
          class="text-slate-950 hover:text-slate-950 font-bold text-base leading-none ml-1">&times;</button>
      </template>
      <template v-else>
        <select v-model="flightPickValue"
          class="bg-slate-100 border border-slate-400 rounded px-3 py-1.5 text-[16px] font-bold text-slate-950 focus:outline-none cursor-pointer">
          <option value="" selected disabled>Vuelo destino</option>
          <option v-for="f in uldsStore.flights" :key="f.id" :value="f.id">
            UPS-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }})
          </option>
        </select>
        <button @click="confirmFlightPick(flightPickValue)"
          class="bg-slate-950 hover:bg-slate-900 text-white font-bold px-3 py-1 rounded text-[16px] uppercase tracking-wider transition-all">
          Reasignar
        </button>
        <button @click="showFlightPicker = false"
          class="text-slate-950 hover:text-slate-950 font-bold text-base leading-none ml-1">&times;</button>
      </template>
    </div>
    <div v-if="showUndoToast && undoAction"
      class="fixed bottom-16 left-1/2 -translate-x-1/2 z-50 flex items-center gap-3 bg-amber-50 border border-amber-300 shadow-lg rounded-lg px-4 py-2.5 text-[14px]">
      <span class="text-amber-900">
        ULD <strong>{{ undoAction.uldNumber }}</strong> reasignado
      </span>
      <button @click="undoLastReassign"
        class="bg-amber-200 hover:bg-amber-300 text-amber-900 font-bold px-3 py-1 rounded text-[16px] uppercase tracking-wider transition-all">
        Deshacer
      </button>
      <button @click="showUndoToast = false"
        class="text-amber-400 hover:text-amber-600 font-bold text-base leading-none ml-1">&times;</button>
    </div>

    <!-- Transfer reason dialog -->
    <div v-if="pendingTransfer"
      class="fixed inset-0 bg-slate-950/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
      @click.self="cancelTransfer">
      <div class="bg-white rounded-xl border border-slate-400 shadow-2xl w-full max-w-md p-5">
        <h3 class="text-[14px] font-black uppercase tracking-wider text-slate-950 mb-3">
          Transferir ULD
        </h3>
        <div class="text-[16px] text-slate-950 mb-3">
          ULD <strong>{{ pendingTransfer.uldNumber }}</strong>
          <span v-if="fromFlightLabel" class="mx-1">→ {{ fromFlightLabel }}</span>
          <span class="mx-1">→</span>
          <strong>{{ toFlightLabel }}</strong>
        </div>
        <label class="text-[15px] font-bold font-mono uppercase tracking-wider text-slate-950 block mb-1">
          Motivo de la transferencia *
        </label>
        <textarea v-model="transferReason" rows="3" placeholder="Ej: Rebalanceo de carga, cambio de ruta..."
          class="w-full text-base font-mono px-3 py-2 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition resize-none mb-4"></textarea>
        <div class="flex justify-end gap-2">
          <button @click="cancelTransfer"
            class="text-[16px] px-3.5 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-slate-100 transition">
            Cancelar
          </button>
          <button @click="confirmTransfer" :disabled="!transferReason.trim()"
            class="flex items-center gap-1.5 text-[16px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-950 shadow-pencil-marine transition active:scale-95 disabled:opacity-50">
            Confirmar Transferencia
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUldsStore } from '../stores/ulds'
import { useAppStore } from '../stores/app'
import api from '../api/client'
import { uldsApi } from '../api/ulds'
import * as XLSX from 'xlsx'

const uldsStore = useUldsStore()
const appStore = useAppStore()
const route = useRoute()

const UPS_AIRLINE_ID = '00000000-0000-0000-0000-000000000001'

const selectedFlightId = computed({
  get: () => uldsStore.selectedFlightId,
  set: (val) => uldsStore.selectFlight(val)
})

const fileInput = ref(null)
const selectedDate = ref('')
const allUlds = ref([])
const loadPlan = ref(null)
const draggedUldId = ref(null)
const dragOver = ref(false)
const dragOverFloating = ref(false)

// Transfer state (with reason)
const pendingTransfer = ref(null) // { uldId, fromFlightId, toFlightId, uldNumber }
const transferReason = ref('')

const fromFlightLabel = computed(() => {
  if (!pendingTransfer.value?.fromFlightId) return null
  const f = uldsStore.flights.find(x => x.id === pendingTransfer.value.fromFlightId)
  return f ? `UPS-${f.flightNumber}` : null
})
const toFlightLabel = computed(() => {
  if (!pendingTransfer.value?.toFlightId) return ''
  const f = uldsStore.flights.find(x => x.id === pendingTransfer.value.toFlightId)
  return f ? `UPS-${f.flightNumber}` : '(sin vuelo)'
})

// Track previous flight per ULD for reverse drag (uldId → fromFlightId)
const reassignmentHistory = ref({})

// Undo state
const undoAction = ref(null)
const showUndoToast = ref(false)
let undoTimeout = null

// Pending flight picker for ULDs without reassignment history
const pendingFlightPick = ref(null) // { uldId, uldNumber }
const flightPickValue = ref('')
const showFlightPicker = ref(false)

const flightDatabase = computed(() => {
  if (!selectedDate.value) return uldsStore.flights
  return uldsStore.flights.filter(f => {
    if (!f.flightDate) return true
    const fd = new Date(f.flightDate).toISOString().split('T')[0]
    return fd === selectedDate.value
  })
})

const activeFlightMeta = computed(() => uldsStore.selectedFlight || {})

const aircraftType = computed(() => {
  const reg = activeFlightMeta.value.aircraftReg || ''
  const upper = reg.toUpperCase()
  if (upper.includes('B767') || upper.includes('767')) return 'B767'
  if (upper.includes('B757') || upper.includes('757')) return 'B757'
  if (upper.includes('A300') || upper.includes('A30')) return 'A300'
  if (upper.includes('B747') || upper.includes('747')) return 'B747'
  if (upper.includes('B777') || upper.includes('777')) return 'B777'
  return reg || '—'
})

const bellyPositions = computed(() => {
  const ac = aircraftType.value
  if (ac === 'B757') return ['31', '34']
  if (ac === 'B767' || ac === 'A300') return ['AB']
  return ['AB']
})

const positionSummary = computed(() => {
  const posMap = {}
  for (const uld of activeManifest.value) {
    const pos = (uld.pos || '').trim().toUpperCase() || '—'
    if (!posMap[pos]) posMap[pos] = { pos, count: 0, pcs: 0 }
    posMap[pos].count++
    posMap[pos].pcs += uld.items.reduce((s, i) => s + (i.pcs || 0), 0)
  }
  const bellySet = new Set(bellyPositions.value.map(p => p.toUpperCase()))
  return Object.values(posMap).map(p => ({
    ...p,
    isBelly: bellySet.has(p.pos) || p.pos === 'AB' || p.pos === '31' || p.pos === '34'
  })).sort((a, b) => {
    const aIsBelly = a.isBelly ? 1 : 0
    const bIsBelly = b.isBelly ? 1 : 0
    return bIsBelly - aIsBelly
  })
})

const activeManifest = computed(() => {
  const source = loadPlan.value?.ulds || uldsStore.activeUlds
  return source.map(uld => ({
    uldId: uld.id,
    flightId: uld.flightId || '',
    uld: uld.uldNumber || uld.id || 'SIN-ULD',
    pos: uld.position || null,
    config: uld.uldType || '-',
    sello: uld.sealNumber || '',
    weight: uld.grossWeightLbs || uld.grossWeight || 0,
    tara: uld.tareLbs || uld.tareWeight || 0,
    status: uld.status || 'IN_RAMP',
    items: (uld.awbs || uld.mawbs || uld.items || []).map(m => ({
      mawb: m.mawbLabel || m.awbNumber || m.id || '',
      pcs: m.pieces || 0,
      volumePct: m.piecesPct || m.percentage || null,
      description: m.description || m.commodityType || 'DRY CARGO',
      destino: m.destination || '-'
    }))
  }))
})

const floatingUlds = computed(() => {
  return allUlds.value.filter(u => !u.flightId)
})

const calculatedTotals = computed(() => {
  let uldsCount = activeManifest.value.length
  let pcs = 0, gross = 0, tare = 0

  activeManifest.value.forEach(uld => {
    gross += uld.weight || 0
    tare += uld.tara || 0
    uld.items.forEach(item => { pcs += item.pcs || 0 })
  })

  return {
    uldsCount,
    pcs,
    gross,
    tare,
    payloadPct: uldsCount ? Math.round(((gross - tare) / Math.max(gross, 1)) * 100) : 0
  }
})

function getRowBgStyle(status) {
  switch (status) {
    case 'LEFT_BEHIND': return 'bg-pink-100/60'
    case 'INCOMPLETE': return 'bg-amber-100/40'
    case 'IN_RAMP': return 'bg-slate-100/80'
    case 'COMPLETED':
    default: return 'bg-white'
  }
}

function getCardBorderStyle(status) {
  switch (status) {
    case 'LEFT_BEHIND': return 'border-pink-300'
    case 'BUILT': return 'border-amber-300'
    case 'SEALED': return 'border-emerald-300'
    case 'LOADED': return 'border-slate-950/30'
    case 'OPEN':
    default: return 'border-slate-400'
  }
}

function getStatusDotColor(status) {
  switch (status) {
    case 'LEFT_BEHIND': return 'bg-pink-500'
    case 'BUILT': return 'bg-amber-500'
    case 'SEALED': return 'bg-emerald-500'
    case 'LOADED': return 'bg-slate-950'
    case 'OPEN':
    default: return 'bg-slate-300'
  }
}

onMounted(async () => {
  await uldsStore.loadFlights()
  if (uldsStore.flights.length) {
    const flightIdFromQuery = route.query.flightId
    if (flightIdFromQuery && uldsStore.flights.find(f => f.id === flightIdFromQuery)) {
      selectedDate.value = ''
      selectedFlightId.value = flightIdFromQuery
      await Promise.all([
        uldsStore.loadUldsForFlight(selectedFlightId.value),
        fetchLoadPlan(selectedFlightId.value)
      ])
    } else {
      const today = new Date().toISOString().split('T')[0]
      selectedDate.value = today
      const matching = uldsStore.flights.filter(f => {
        if (!f.flightDate) return true
        return new Date(f.flightDate).toISOString().split('T')[0] === today
      })
      if (matching.length) {
        selectedFlightId.value = matching[0].id
        await Promise.all([
          uldsStore.loadUldsForFlight(selectedFlightId.value),
          fetchLoadPlan(selectedFlightId.value)
        ])
      }
    }
  }
  await loadAllUlds()
})

async function loadAllUlds() {
  try {
    const res = await api.get('/ulds', { params: { airlineId: UPS_AIRLINE_ID } })
    allUlds.value = res.data
  } catch (e) {
    console.error('Error loading all ULDs:', e)
  }
}

async function fetchLoadPlan(flightId) {
  if (!flightId) { loadPlan.value = null; return }
  try {
    const res = await api.get(`/load-planning/flight/${flightId}`)
    loadPlan.value = res.data
  } catch (e) {
    loadPlan.value = null
    if (e.response?.status !== 404) {
      console.error('Error fetching load plan:', e)
    }
  }
}

async function onDateChange() {
  if (flightDatabase.value.length) {
    selectedFlightId.value = flightDatabase.value[0].id
    await Promise.all([
      uldsStore.loadUldsForFlight(selectedFlightId.value),
      fetchLoadPlan(selectedFlightId.value)
    ])
  }
}

function syncFlightMetadata() {
  if (selectedFlightId.value) {
    uldsStore.loadUldsForFlight(selectedFlightId.value)
    fetchLoadPlan(selectedFlightId.value)
  }
}

async function reassignFlight(uldGroup, silent = false) {
  if (!uldGroup.uldId || !uldGroup.flightId) return
  const fromFlightId = allUlds.value.find(u => u.id === uldGroup.uldId)?.flightId || null
  const uldNumber = allUlds.value.find(u => u.id === uldGroup.uldId)?.uldNumber || uldGroup.uldId
  try {
    await api.patch(`/ulds/${uldGroup.uldId}/flight`, { flightId: uldGroup.flightId })
    // Always store the origin flight in history for reverse drag
    if (fromFlightId) {
      reassignmentHistory.value[uldGroup.uldId] = fromFlightId
    }
    if (!silent) {
      if (undoTimeout) clearTimeout(undoTimeout)
      undoAction.value = { uldId: uldGroup.uldId, fromFlightId, toFlightId: uldGroup.flightId, uldNumber }
      showUndoToast.value = true
      undoTimeout = setTimeout(() => { showUndoToast.value = false; undoAction.value = null }, 6000)
    }
    await loadAllUlds()
    if (selectedFlightId.value) {
      await Promise.all([
        uldsStore.loadUldsForFlight(selectedFlightId.value),
        fetchLoadPlan(selectedFlightId.value)
      ])
    }
  } catch (e) {
    alert('Error reasignando vuelo: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

async function updatePosition(uldId, newPos) {
  if (!uldId) return
  const trimmed = (newPos || '').trim()
  try {
    await uldsApi.update(uldId, { position: trimmed || null })
    await loadAllUlds()
    if (selectedFlightId.value) {
      await Promise.all([
        uldsStore.loadUldsForFlight(selectedFlightId.value),
        fetchLoadPlan(selectedFlightId.value)
      ])
    }
  } catch (e) {
    alert('Error actualizando posición: ' + (e.response?.data?.message || e.message))
  }
}

function onTransferRequest(uldId, uldNumber, fromFlightId, toFlightId) {
  if (!toFlightId || toFlightId === fromFlightId) return
  pendingTransfer.value = { uldId, fromFlightId, toFlightId, uldNumber }
  transferReason.value = ''
}

function cancelTransfer() {
  pendingTransfer.value = null
  transferReason.value = ''
}

async function confirmTransfer() {
  if (!pendingTransfer.value || !transferReason.value.trim()) return
  const { uldId, toFlightId, uldNumber } = pendingTransfer.value
  const reason = transferReason.value.trim()
  pendingTransfer.value = null
  transferReason.value = ''
  try {
    await api.post(`/ulds/${uldId}/transfer`, {
      destinationFlightId: toFlightId,
      reason
    })
    await loadAllUlds()
    if (selectedFlightId.value) {
      await Promise.all([
        uldsStore.loadUldsForFlight(selectedFlightId.value),
        fetchLoadPlan(selectedFlightId.value)
      ])
    }
  } catch (e) {
    alert('Error transfiriendo ULD: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

async function dispatchFlight() {
  if (!selectedFlightId.value) return
  const ulds = uldsStore.activeUlds || []
  const pendingAwbs = new Set()
  for (const u of ulds) {
    for (const m of (u.awbs || [])) {
      const awbNum = m.mawbLabel || ''
      if (!awbNum) continue
      const hasReceipt = appStore.receipts.some(r => {
        const mawb = r.mawb || {}
        return mawb.awbNumber === awbNum
      })
      if (!hasReceipt) pendingAwbs.add(awbNum)
    }
  }
  if (pendingAwbs.size > 0) {
    alert(`No se puede despachar: ${pendingAwbs.size} MAWB(s) no tienen recibo de bodega:\n${[...pendingAwbs].join('\n')}\n\nComplete los recibos pendientes antes de despachar.`)
    return
  }
  if (!confirm('¿Despachar el vuelo? Se marcarán todos los ULDs como LOADED y no se podrá modificar.')) return
  try {
    await api.post(`/load-planning/flight/${selectedFlightId.value}/close`)
    alert('Vuelo despachado correctamente.')
    await Promise.all([
      uldsStore.loadUldsForFlight(selectedFlightId.value),
      fetchLoadPlan(selectedFlightId.value),
      uldsStore.loadFlights()
    ])
  } catch (e) {
    alert('Error despachando vuelo: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

async function undoLastReassign() {
  if (!undoAction.value) return
  const action = undoAction.value
  undoAction.value = null
  showUndoToast.value = false
  if (undoTimeout) clearTimeout(undoTimeout)
  if (action.fromFlightId) {
    await reassignFlight({ uldId: action.uldId, flightId: action.fromFlightId }, true)
  }
}

function cancelFlightPick() { pendingFlightPick.value = null; showFlightPicker.value = false }

async function detachToFloating() {
  if (!pendingFlightPick.value) return
  const { uldId } = pendingFlightPick.value
  const uldNumber = pendingFlightPick.value.uldNumber
  pendingFlightPick.value = null
  showFlightPicker.value = false
  try {
    await api.patch(`/ulds/${uldId}/flight`, { flightId: null })
    await loadAllUlds()
    if (selectedFlightId.value) {
      await Promise.all([
        uldsStore.loadUldsForFlight(selectedFlightId.value),
        fetchLoadPlan(selectedFlightId.value)
      ])
    }
  } catch (e) {
    alert('Error desasignando vuelo: ' + (e.response?.data?.error || e.response?.data?.message || e.message))
  }
}

async function confirmFlightPick(flightId) {
  if (!pendingFlightPick.value || !flightId) return
  const { uldId } = pendingFlightPick.value
  pendingFlightPick.value = null
  showFlightPicker.value = false
  await reassignFlight({ uldId, flightId }, false)
}

function onDragStart(uldId, e) {
  draggedUldId.value = uldId
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', uldId)
  }
}

function onDragLeave(e) {
  const section = e.currentTarget
  if (!section.contains(e.relatedTarget)) {
    dragOver.value = false
  }
}

async function onDrop(e) {
  dragOver.value = false
  const uldId = draggedUldId.value || (e.dataTransfer?.getData('text/plain'))
  draggedUldId.value = null
  if (!uldId || !selectedFlightId.value) return
  await reassignFlight({ uldId, flightId: selectedFlightId.value })
}

// Pointer-based drag from table rows to floating cards section
let pointerDragData = null
const RING_CLASSES = ['ring-4', 'ring-amber-300', 'ring-offset-2', 'rounded-lg']

function onTableUldPointerDown(uldId, e) {
  pointerDragData = { uldId, startX: e.clientX, startY: e.clientY, moved: false }
  document.addEventListener('pointermove', onTableUldPointerMove)
  document.addEventListener('pointerup', onTableUldPointerUp)
}

function onTableUldPointerMove(e) {
  if (!pointerDragData) return
  const dx = e.clientX - pointerDragData.startX
  const dy = e.clientY - pointerDragData.startY
  if (Math.abs(dx) > 4 || Math.abs(dy) > 4) {
    pointerDragData.moved = true
  }
  if (!pointerDragData.moved) return
  // Check if cursor is over the floating cards section
  const floatingSection = document.querySelector('.floating-drop-zone')
  if (floatingSection) {
    const rect = floatingSection.getBoundingClientRect()
    const over = (
      e.clientX >= rect.left && e.clientX <= rect.right &&
      e.clientY >= rect.top && e.clientY <= rect.bottom
    )
    dragOverFloating.value = over
    // Direct DOM classList for immediate visual feedback (Vue async batching may skip intermediate states)
    if (over) {
      floatingSection.classList.add(...RING_CLASSES)
    } else {
      floatingSection.classList.remove(...RING_CLASSES)
    }
  }
}

async function onTableUldPointerUp(e) {
  document.removeEventListener('pointermove', onTableUldPointerMove)
  document.removeEventListener('pointerup', onTableUldPointerUp)
  // Delay ring removal to next frame so user can see the feedback
  const floatingSection = document.querySelector('.floating-drop-zone')
  if (floatingSection) {
    requestAnimationFrame(() => floatingSection.classList.remove(...RING_CLASSES))
  }
  const data = pointerDragData
  pointerDragData = null
  if (!data || !data.moved) {
    dragOverFloating.value = false
    return
  }
  const wasOverFloating = dragOverFloating.value
  dragOverFloating.value = false
  if (wasOverFloating && data.uldId) {
    const fromFlightId = reassignmentHistory.value[data.uldId]
    if (fromFlightId) {
      await reassignFlight({ uldId: data.uldId, flightId: fromFlightId }, true)
      delete reassignmentHistory.value[data.uldId]
    } else {
      const uldNumber = allUlds.value.find(u => u.id === data.uldId)?.uldNumber || data.uldId
      pendingFlightPick.value = { uldId: data.uldId, uldNumber }
      flightPickValue.value = ''
      showFlightPicker.value = false
    }
  }
}

function triggerImport() { fileInput.value?.click() }
function handleFileImport(e) { console.log('Procesando archivo:', e.target.files[0]?.name) }
function exportToXLSX() {
  const flight = activeFlightMeta.value
  if (!flight || !activeManifest.value.length) {
    alert('No hay datos para exportar')
    return
  }
  const rows = [
    ['LOAD PLAN — ' + (flight.flightNumber || '')],
    ['Vuelo: UPS-' + (flight.flightNumber || ''), 'Fecha: ' + (flight.flightDate || ''), 'Ruta: ' + (flight.origin || '') + '→' + (flight.destination || ''), 'Aeronave: ' + (flight.aircraftReg || '')],
    [],
    ['ULD', 'PCS', '%', 'GROSS', 'TARE', 'TYPE', 'SEAL', 'POS', 'DESC', 'MAWB', 'DEST']
  ]
  for (const uldGroup of activeManifest.value) {
    if (uldGroup.items.length === 0) {
      rows.push([
        uldGroup.uld, 0, '-',
        uldGroup.weight || 0, uldGroup.tara || 0,
        uldGroup.config, uldGroup.sello || '-', uldGroup.pos || '-',
        '—', 'empty', '-'
      ])
    } else {
      for (let i = 0; i < uldGroup.items.length; i++) {
        const item = uldGroup.items[i]
        rows.push([
          i === 0 ? uldGroup.uld : '',
          item.pcs || 0,
          item.volumePct ? item.volumePct + '%' : '-',
          i === 0 ? (uldGroup.weight || 0) : '',
          i === 0 ? (uldGroup.tara || 0) : '',
          i === 0 ? uldGroup.config : '',
          i === 0 ? (uldGroup.sello || '-') : '',
          i === 0 ? (uldGroup.pos || '-') : '',
          item.description || '',
          item.mawb || '',
          item.destino || '-'
        ])
      }
    }
  }
  rows.push([])
  const totals = calculatedTotals.value
  rows.push(['Totales', '', '', '', '', '', '', '', '', '', ''])
  rows.push(['ULDs: ' + totals.uldsCount, 'PCS: ' + totals.pcs.toLocaleString(), 'Gross: ' + totals.gross.toLocaleString() + ' lb', 'Tare: ' + totals.tare.toLocaleString() + ' lb', '', '', '', '', '', '', ''])
  const ws = XLSX.utils.aoa_to_sheet(rows)
  ws['!cols'] = [
    { wch: 14 }, { wch: 8 }, { wch: 6 }, { wch: 10 }, { wch: 10 },
    { wch: 8 }, { wch: 12 }, { wch: 6 }, { wch: 20 }, { wch: 18 }, { wch: 6 }
  ]
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Load Plan')
  const flightNum = flight.flightNumber || selectedFlightId.value
  XLSX.writeFile(wb, `LOAD_PLAN_UPS-${flightNum}.xlsx`)
}

watch(() => uldsStore.activeUlds, (newUlds) => {
  console.log('LoadPlanning actualizado con', newUlds?.length || 0, 'ULDs')
}, { deep: true })
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15, 32, 67, 0.05), 1px 3px 6px rgba(15, 32, 67, 0.04); }

.lp-container-block:hover {
  background-color: rgba(241, 245, 249, 0.5) !important;
}

.lp-grid {
  display: grid;
  grid-template-columns: 10fr 5fr 4fr 6fr 4fr 4fr 12fr 4fr 14fr 14fr 4fr;
  gap: 4px;
  min-width: 960px;
}
.lp-grid > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Visible thin scrollbar for horizontal scrolling */
.lp-scroll-x::-webkit-scrollbar {
  height: 12px;
  display: block;
}
.lp-scroll-x::-webkit-scrollbar-track {
  background: #e2e8f0;
  border-radius: 6px;
}
.lp-scroll-x::-webkit-scrollbar-thumb {
  background: #1e3a5f;
  border-radius: 6px;
}
.lp-scroll-x::-webkit-scrollbar-thumb:hover {
  background: #0f2440;
}
.lp-scroll-x {
  scrollbar-width: auto;
  scrollbar-color: #1e3a5f #e2e8f0;
}
</style>
