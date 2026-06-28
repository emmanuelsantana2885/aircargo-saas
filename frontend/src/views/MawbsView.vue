<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex flex-wrap justify-between items-end gap-2 border-b border-slate-200 pb-3 shrink-0">
      <div class="flex items-end gap-3">
        <div>
          <h1 class="text-xs font-black tracking-tight text-slate-950 uppercase font-mono">CrossReport — MAWBs</h1>
          <p class="text-xs font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">Matriz MAWB x Vuelo / Distribucion de Piezas</p>
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
              class="w-64 px-3 py-1.5 text-[16px] font-mono border border-slate-400 rounded bg-white focus:outline-none focus:border-slate-950 placeholder:text-slate-400" />
            <span class="text-[14px] font-mono text-slate-950 min-w-[30px]">{{ filterTypeLabel }}</span>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span class="text-[15px] font-mono text-slate-950 whitespace-nowrap">Filas: {{ filteredRows.length }} / {{ matrixRows.length }} | Cols: {{ flightColumns.length }}</span>
        <div class="flex items-center gap-2 border-l border-slate-200 pl-2">
          <div class="relative">
            <button @click="showPeriodMenu = !showPeriodMenu"
              class="p-1.5 rounded hover:bg-slate-100 transition-colors text-[16px] font-mono flex items-center gap-1" title="Periodo de timeline">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
              <span class="font-bold">{{ periodLabel }}</span>
            </button>
            <div v-if="showPeriodMenu" class="absolute top-full left-0 mt-1 bg-white border border-slate-400 rounded-lg shadow-xl z-50 py-1 min-w-[150px]">
              <button v-for="p in periodOptions" :key="p.value" @click="setTimelinePeriod(p.value)"
                class="w-full text-left px-3 py-2 text-[16px] font-mono hover:bg-slate-100 transition-colors"
                :class="timelinePeriod === p.value ? 'bg-slate-200 font-bold text-slate-950' : 'text-slate-950'">
                {{ p.label }}
              </button>
            </div>
          </div>
          <div class="flex items-center overflow-x-auto scrollbar-none" style="max-width:480px">
            <div v-if="timelineSegments.length" class="relative w-full py-1 select-none" style="min-width:280px">
              <!-- Barra de fondo -->
              <div class="relative h-6 flex items-center mx-1">
                <div class="absolute left-0 right-0 h-1.5 bg-slate-100 rounded-full"></div>
                <!-- Rango activo -->
                <div v-if="rangeStartSeg && rangeEndSeg"
                  class="absolute h-1.5 rounded-full bg-slate-950/20"
                  :style="rangeBarStyle"></div>
                <!-- Segmentos -->
                <div v-for="(seg, si) in timelineSegments" :key="seg.value"
                  class="flex-1 flex flex-col items-center relative z-10 cursor-pointer group"
                  @click="toggleRangeSegment(seg.value)"
                  :title="seg.label + ' (' + seg.count + ' cols)'">
                  <span class="text-xs font-mono mb-0.5 leading-none transition-colors font-bold"
                    :class="isInRange(seg.value) ? 'text-slate-950' : 'text-slate-400 group-hover:text-slate-600'">
                    {{ seg.short }}
                  </span>
                  <div class="w-full h-1.5 flex items-center justify-center">
                    <div class="h-1 w-full rounded-sm transition-all duration-150"
                      :class="isInRange(seg.value)
                        ? 'bg-slate-950'
                        : 'bg-transparent group-hover:bg-slate-200'"></div>
                  </div>
                </div>
              </div>
              <!-- Handle izquierdo + derecho -->
              <div v-if="rangeStartSeg && timelineSegments.length > 1"
                class="absolute top-0 left-0 right-0 h-6 pointer-events-none">
                <div class="relative w-full h-full">
                  <div class="absolute top-0 -translate-x-1/2 pointer-events-auto cursor-col-resize group"
                    :style="{ left: rangeLeftPct + '%' }"
                    @pointerdown.prevent="startRangeDrag('left', $event)">
                    <div class="w-3 h-5 bg-slate-950 rounded-sm shadow-sm flex items-center justify-center">
                      <div class="w-0.5 h-3 bg-white/60 rounded-full"></div>
                    </div>
                  </div>
                  <div class="absolute top-0 translate-x-1/2 pointer-events-auto cursor-col-resize group"
                    :style="{ left: rangeRightPct + '%' }"
                    @pointerdown.prevent="startRangeDrag('right', $event)">
                    <div class="w-3 h-5 bg-slate-950 rounded-sm shadow-sm flex items-center justify-center">
                      <div class="w-0.5 h-3 bg-white/60 rounded-full"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <span v-else class="text-xs text-slate-300 font-mono px-2 whitespace-nowrap">Sin fechas</span>
          </div>
          <button v-if="rangeStartSeg" @click="clearTimeline"
            class="text-slate-400 hover:text-slate-950 text-[13px] px-1 font-bold">✕</button>
        </div>
        <select v-model="localFlightId" @change="onFlightChange"
          class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-[13px] cursor-pointer min-w-[140px]">
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
          <h3 class="text-[10.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-lg font-mono font-black tracking-tight text-slate-950 mt-0">{{ stat.value }}</div>
        </div>
        <div class="text-xs font-mono text-slate-400 truncate"><span>{{ stat.sub }}</span></div>
      </div>
    </section>

    <section ref="tableSectionRef" class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden bg-white flex flex-col mb-1.5">
      <div v-if="loadingMatrix" class="flex-1 flex items-center justify-center">
        <span class="text-[16px] font-mono text-slate-950 animate-pulse">Construyendo matriz...</span>
      </div>
      <div v-else-if="!filteredRows.length" class="flex-1 flex items-center justify-center">
        <p class="text-[16px] font-mono text-slate-950 uppercase tracking-widest">Sin resultados</p>
      </div>
      <template v-else>
        <div ref="scrollContainer" class="overflow-auto scrollbar-none flex-1" @scroll="onScroll">
          <table class="w-full border-collapse text-xs font-mono">
            <thead class="sticky top-0 z-20">
              <tr class="bg-slate-900 text-white text-xs">
                <th :style="{ left: stickyOffsets[0] + 'px', width: '180px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-left px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700 whitespace-nowrap">MAWB</th>
                <th :style="{ left: stickyOffsets[1] + 'px', width: '200px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-left px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700 whitespace-nowrap">Shipper / Consignee</th>
                <th :style="{ left: stickyOffsets[2] + 'px', width: '90px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-right px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700 whitespace-nowrap">Pcs Reserved</th>
                <th :style="{ left: stickyOffsets[3] + 'px', width: '90px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-right px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700 whitespace-nowrap">Pcs Received</th>
                <th :style="{ left: stickyOffsets[4] + 'px', width: '110px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-right px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700">Kg</th>
                <th :style="{ left: stickyOffsets[5] + 'px', width: '80px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-left px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700">Dest</th>
                <th :style="{ left: stickyOffsets[6] + 'px', width: '80px', zIndex: 30 }"
                  class="sticky bg-slate-900 text-right px-2 py-2.5 font-black uppercase tracking-wider border-r border-slate-700">Pcs&#8599;</th>
                <th v-for="(f, fi) in flightColumns" :key="f.id"
                  class="px-2 py-2.5 text-center font-black min-w-[100px] border-x border-slate-700/40 cursor-pointer transition-colors"
                  :class="[highlightFlightId === f.id ? 'bg-amber-500 text-slate-950' : 'hover:bg-slate-800/70']"
                  @mouseenter="hoverFlightCol = f.id" @mouseleave="hoverFlightCol = null"
                  @click="scrollToFlight(f.id)">
<div class="text-xs leading-tight">UPS-{{ f.flightNumber }}</div>
                   <div class="text-xs font-bold opacity-90 tracking-wide">{{ formatDate(f.flightDate) }}</div>
                   <div v-if="flightTotals[f.id]" class="text-xs font-normal opacity-60 mt-0.5">{{ flightTotals[f.id] }} pcs</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, ri) in filteredRows" :key="row.mawbId"
                class="border-b border-slate-100 transition-colors"
                :class="[rowBgClass(row), hoverFlightCol ? 'group' : '']">
                <td :style="{ left: stickyOffsets[0] + 'px', width: '180px', zIndex: 10 }"
                  class="sticky px-2 py-2.5 font-bold border-r border-slate-300 cursor-pointer transition-colors duration-150 whitespace-nowrap"
                  :class="mawbStatusClass(row)"
                  :title="statusTitle(row)"
                  @click="goToReceipt(row)">
                  <div class="flex flex-col leading-tight">
<span class="underline decoration-dotted underline-offset-2 truncate text-xs">{{ row.awbNumber || '—' }}</span>
                     <span class="text-xs font-normal opacity-70 mt-0.5">
                      <template v-if="row.status === 'BOOKED'">Reservado</template>
                      <template v-else-if="row.status === 'RECEIVED'">Recibido</template>
                      <template v-else-if="row.status === 'MANIFESTED'">Montado</template>
                      <template v-else-if="row.status === 'DEPARTED'">Despachado</template>
                      <template v-else>{{ row.status || '' }}</template>
                    </span>
                  </div>
                </td>
                <td :style="{ left: stickyOffsets[1] + 'px', width: '200px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-slate-800 border-r border-slate-300 whitespace-nowrap"
                  :title="(row.shipperName || '?') + ' / ' + (row.consigneeName || '?')">
                  <div class="flex flex-col leading-tight">
<span class="truncate text-xs">{{ row.shipperName || '—' }}</span>
                     <span class="truncate text-xs text-slate-950">/ {{ row.consigneeName || '—' }}</span>
                  </div>
                </td>
                <td :style="{ left: stickyOffsets[2] + 'px', width: '90px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-right font-bold border-r border-slate-300 whitespace-nowrap"
                  :class="row.pieceDiff !== 0 ? 'text-orange-600 bg-orange-50' : 'text-slate-950'">
                  {{ row.reservedPieces || '—' }}
<span v-if="row.pieceDiff > 0" class="text-xs text-orange-500 ml-0.5" title="Recibido excede reservado">&#9650;</span>
                   <span v-else-if="row.pieceDiff < 0" class="text-xs text-orange-500 ml-0.5" title="Recibido menor que reservado">&#9660;</span>
                </td>
                <td :style="{ left: stickyOffsets[3] + 'px', width: '90px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-right font-bold border-r border-slate-300 whitespace-nowrap"
                  :class="row.pieceDiff !== 0 ? 'text-orange-600 bg-orange-50' : 'text-slate-950'">
                  {{ row.receivedPieces || '—' }}
                  <span v-if="row.receivedPieces > 0 && row.pieceDiff !== 0" class="text-xs text-orange-400 ml-0.5">&#9888;</span>
                </td>
                <td :style="{ left: stickyOffsets[4] + 'px', width: '110px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-right font-bold text-slate-950 border-r border-slate-300">{{ row.totalWeightKg ? Number(row.totalWeightKg).toLocaleString() : '—' }}</td>
                <td :style="{ left: stickyOffsets[5] + 'px', width: '80px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-left text-slate-950 uppercase text-xs border-r border-slate-300">{{ row.destination || '—' }}</td>
                <td :style="{ left: stickyOffsets[6] + 'px', width: '80px', zIndex: 10 }"
                  class="sticky bg-white px-2 py-2.5 text-right font-bold border-r border-slate-300"
                  :class="row.hasDispatchedExcess ? 'text-rose-600' : 'text-slate-950'">
                  {{ row.pcsDispatched || '—' }}
                  <span v-if="row.hasDispatchedExcess" class="text-xs text-rose-500 ml-0.5" title="Despachado excede recibido">&#9888;</span>
                </td>
                <td v-for="(f, fi) in flightColumns" :key="f.id"
                  class="px-2 py-2.5 text-center border-x border-slate-200 font-bold transition-all duration-200"
                  :class="cellClasses(row, f, fi)"
                  :title="uldTooltip(row, f)">
                  <span v-if="getPieces(row, f)" class="chalk-text relative text-xs">
                    <span class="flex flex-col items-center leading-tight">
                      <span>{{ getPieces(row, f) }}</span>
                      <span class="text-xs font-normal opacity-60">{{ row.uldCountByFlight[f.id] || 0 }} ULDs</span>
                    </span>
                    <svg width="16" height="16" viewBox="0 0 14 14" class="inline-block shrink-0 ml-0.5">
                      <circle cx="7" cy="7" r="5.5" fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="1.5"/>
                      <circle cx="7" cy="7" r="5.5" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="1.5"
                        :stroke-dasharray="arcCircum"
                        :stroke-dashoffset="arcOffset(row, f)"
                        transform="rotate(-90 7 7)"/>
                    </svg>
                  </span>
                  <span v-else class="text-slate-200">&middot;</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="bg-slate-100 border-t border-slate-300 px-4 py-1.5 text-[14px] text-slate-950 font-mono flex justify-between items-center shrink-0">
          <span>Piezas: {{ totalReserved }} res. / {{ totalReceived }} rec. / {{ totalDispatched }} desp. / {{ totalTracked }} track</span>
          <span class="flex items-center gap-3">
            <span class="flex items-center gap-1"><span class="w-2 h-2 rounded-sm chalk-swatch"></span> con piezas</span>
            <span>Celdas ocupadas: {{ activeCells }} | Vuelos: {{ flightColumns.length }} | MAWBs: {{ filteredRows.length }}</span>
          </span>
        </div>
        <div class="h-4 cursor-row-resize flex items-center justify-center hover:bg-slate-200 transition-colors shrink-0 group border-t border-slate-300"
          @mousedown.prevent="startResize">
          <div class="w-10 h-0.5 rounded-full bg-slate-400 group-hover:bg-slate-500 group-hover:h-1 transition-all duration-150"></div>
        </div>
      </template>
    </section>

    <teleport to="body">
      <transition name="fade">
        <div v-if="showMiniMap" class="fixed bottom-6 right-6 z-50 bg-white/95 border border-slate-400 rounded-lg shadow-2xl p-3 w-56 h-44 overflow-hidden">
          <div class="text-[8px] font-mono font-bold text-slate-950 uppercase tracking-wider mb-1">Minimapa</div>
          <div class="grid" :style="miniMapGridStyle">
            <div v-for="(cell, mi) in miniMapCells" :key="mi"
              class="rounded-[1px]"
              :class="cell.active ? 'chalk-bg' : 'bg-slate-100'"
              :title="cell.label"></div>
          </div>
          <div class="absolute inset-0 border-2 border-emerald-500/30 rounded-lg pointer-events-none" :style="miniMapViewportStyle"></div>
        </div>
      </transition>
    </teleport>

    <button @click="showMiniMap = !showMiniMap"
      class="fixed bottom-4 left-4 z-50 w-8 h-8 rounded-full bg-slate-800 text-white text-[13px] font-mono shadow-lg hover:bg-slate-950 transition flex items-center justify-center"
      title="Minimapa de matriz">
      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import api from '../api/client'

const router = useRouter()
const store = useAppStore()

const localFlightId = ref(store.selectedFlightId || '')
const loadingMatrix = ref(false)
const highlightFlightId = ref(null)
const showFilter = ref(false)
const filterText = ref('')
const hoverFlightCol = ref(null)
const showMiniMap = ref(false)
const scrollContainer = ref(null)
const tableSectionRef = ref(null)
const scrollPos = ref({ top: 0, left: 0 })
const timelinePeriod = ref('month')
const activeSegment = ref('')
const rangeStartSeg = ref('')
const rangeEndSeg = ref('')
const showPeriodMenu = ref(false)
const periodOptions = [
  { value: 'year', label: 'Year' },
  { value: 'quarter', label: 'Quarter' },
  { value: 'month', label: 'Month' },
  { value: 'week', label: 'Week' },
  { value: 'day', label: 'Day' },
]

const periodLabel = computed(() => {
  const p = periodOptions.find(x => x.value === timelinePeriod.value)
  return p ? p.label : timelinePeriod.value
})

const matrixRows = shallowRef([])
const flightColumns = shallowRef([])
const stickyOffsets = ref([0, 180, 380, 470, 560, 670, 750])

const filterTypeLabel = computed(() => {
  const t = filterText.value
  if (!t) return ''
  if (t.includes('+')) return 'multi'
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

  // Multi-shipper mode: split by +, OR match across shipper/consignee/awb
  if (t.includes('+')) {
    let searchStr = t
    if (searchStr.startsWith('*') && searchStr.endsWith('*'))
      searchStr = searchStr.slice(1, -1)
    const segments = searchStr.split('+').map(s => s.trim().toLowerCase()).filter(Boolean)
    if (!segments.length) return rows
    return rows.filter(row => {
      const shipper = (row.shipperName || '').toLowerCase()
      const consignee = (row.consigneeName || '').toLowerCase()
      const awb = (row.awbNumber || '').toLowerCase()
      return segments.some(seg =>
        shipper.includes(seg) || consignee.includes(seg) || awb.includes(seg)
      )
    })
  }

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

const flightTotals = computed(() => {
  const totals = {}
  for (const row of filteredRows.value) {
    for (const f of flightColumns.value) {
      const pcs = row.cells[f.id] || 0
      totals[f.id] = (totals[f.id] || 0) + pcs
    }
  }
  return totals
})

const miniMapCells = computed(() => {
  const cells = []
  const rows = filteredRows.value
  const cols = flightColumns.value
  for (let ri = 0; ri < Math.min(rows.length, 30); ri++) {
    const row = rows[ri]
    for (let ci = 0; ci < Math.min(cols.length, 30); ci++) {
      const col = cols[ci]
      const pcs = row.cells[col.id] || 0
      cells.push({ active: pcs > 0, label: `${row.awbNumber} / UPS-${col.flightNumber}: ${pcs} pcs` })
    }
  }
  return cells
})

const miniMapGridStyle = computed(() => {
  const cols = Math.min(flightColumns.value.length, 30)
  return { gridTemplateColumns: `repeat(${cols}, 1fr)`, gap: '1px' }
})

const miniMapViewportStyle = computed(() => {
  const el = scrollContainer.value
  if (!el) return {}
  const sh = el.scrollHeight || 1
  const sw = el.scrollWidth || 1
  const vh = el.clientHeight
  const vw = el.clientWidth
  void scrollPos.value
  const topPct = (el.scrollTop / sh) * 100
  const leftPct = (el.scrollLeft / sw) * 100
  const hPct = (vh / sh) * 100
  const wPct = (vw / sw) * 100
  return { top: `${topPct}%`, left: `${leftPct}%`, width: `${wPct}%`, height: `${hPct}%` }
})

function onScroll() {
  if (!showMiniMap.value) return
  scrollPos.value = { top: scrollContainer.value?.scrollTop || 0, left: scrollContainer.value?.scrollLeft || 0 }
}

async function buildMatrix() {
  loadingMatrix.value = true
  try {
    const [uldRes, linkRes] = await Promise.all([
      api.get('/ulds', { params: { airlineId: '00000000-0000-0000-0000-000000000001' } }),
      api.get('/uld-awbs'),
    ])
    const ulds = uldRes.data
    const links = linkRes.data

    const uldFlightMap = {}
    const uldNumberMap = {}
    for (const u of ulds) {
      uldFlightMap[u.id] = u.flightId
      uldNumberMap[u.id] = u.uldNumber || u.id.slice(0, 8)
    }

    const receivedByMawb = {}
    for (const r of store.receipts) {
      receivedByMawb[r.mawbId] = (receivedByMawb[r.mawbId] || 0) + (r.pieceCount || 0)
    }

    const bookingByMawb = {}
    for (const b of store.bookings) {
      if (b.mawbId) {
        if (!bookingByMawb[b.mawbId]) bookingByMawb[b.mawbId] = []
        bookingByMawb[b.mawbId].push(b)
      }
    }

    const mawbPcsByFlight = new Map()
    const mawbUldsByFlight = new Map()
    for (const link of links) {
      const mawbId = link.mawbId
      const flightId = uldFlightMap[link.uldId]
      const pcs = link.pieces || 0
      if (!mawbId || !flightId) continue
      if (!mawbPcsByFlight.has(mawbId)) mawbPcsByFlight.set(mawbId, new Map())
      const fm = mawbPcsByFlight.get(mawbId)
      fm.set(flightId, (fm.get(flightId) || 0) + pcs)
      if (!mawbUldsByFlight.has(mawbId)) mawbUldsByFlight.set(mawbId, new Map())
      const um = mawbUldsByFlight.get(mawbId)
      if (!um.has(flightId)) um.set(flightId, [])
      um.get(flightId).push({ uldNumber: uldNumberMap[link.uldId] || 'ULD?', pieces: pcs })
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

    let flights = store.flights.filter(f => flightIds.has(f.id))
    if (rangeStartSeg.value || rangeEndSeg.value) {
      const keys = timelineSegments.value.filter(s => isInRange(s.value)).map(s => s.value)
      const dates = []
      for (const k of keys) {
        const dr = getPeriodDateRange(timelinePeriod.value, k)
        if (dr.start) dates.push(dr)
      }
      if (dates.length) {
        const globalStart = dates.reduce((a, d) => d.start < a ? d.start : a, dates[0].start)
        const globalEnd = dates.reduce((a, d) => d.end > a ? d.end : a, dates[0].end)
        flights = flights.filter(f => {
          if (!f.flightDate) return false
          return f.flightDate >= globalStart && f.flightDate <= globalEnd
        })
      }
    }
    flights.sort((a, b) => (a.flightDate || '').localeCompare(b.flightDate || '') || (a.flightNumber || '').localeCompare(b.flightNumber || ''))

    const rows = filteredMawbs.map(m => {
      const fm = mawbPcsByFlight.get(m.id) || new Map()
      const um = mawbUldsByFlight.get(m.id) || new Map()
      const cells = {}
      const uldCountByFlight = {}
      const breakdown = {}
      let pcsDispatched = 0
      for (const f of flights) {
        const pcs = fm.get(f.id) || 0
        cells[f.id] = pcs
        pcsDispatched += pcs
        uldCountByFlight[f.id] = (um.get(f.id) || []).length
        breakdown[f.id] = um.get(f.id) || []
      }
      const receivedPieces = receivedByMawb[m.id] || 0
      const bookingList = bookingByMawb[m.id]
      const reservedPieces = bookingList && bookingList.length > 0
        ? Math.max(...bookingList.map(b => b.skids || 0))
        : (m.pieces || 0)
      return {
        mawbId: m.id,
        awbNumber: m.awbNumber,
        shipperName: m.shipperName,
        consigneeName: m.consigneeName,
        destination: m.destination,
        totalPieces: pcsDispatched || m.pieces || 0,
        totalWeightKg: m.reportedWeightKg || m.chargeableWeightKg || null,
        status: m.status,
        reservedPieces,
        receivedPieces,
        pieceDiff: receivedPieces - reservedPieces,
        hasDispatchedExcess: receivedPieces > 0 && pcsDispatched > receivedPieces,
        pcsDispatched,
        cells,
        uldCountByFlight,
        breakdownByFlight: breakdown,
      }
    })

    matrixRows.value = rows
    flightColumns.value = flights
    await nextTick()
    computeStickyOffsets()
  } catch (e) {
    console.error('Matrix error:', e)
  } finally {
    loadingMatrix.value = false
  }
}

function computeStickyOffsets() {
  const el = scrollContainer.value
  if (!el) return
  const ths = el.querySelectorAll('thead tr th')
  if (!ths.length) return
  const offsets = []
  let cum = 0
  for (let i = 0; i < ths.length && i <= 6; i++) {
    offsets.push(cum)
    cum += ths[i].offsetWidth
  }
  while (offsets.length <= 6) offsets.push(offsets[offsets.length - 1] || 0)
  stickyOffsets.value = offsets
}

function getPeriodKey(dateStr, period) {
  const d = new Date(dateStr)
  switch (period) {
    case 'year': return String(d.getFullYear())
    case 'quarter': return `${d.getFullYear()}-Q${Math.floor(d.getMonth() / 3) + 1}`
    case 'month': {
      const ms = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
      return `${d.getFullYear()}-${ms[d.getMonth()]}`
    }
    case 'week': {
      const start = new Date(d.getTime())
      start.setHours(0,0,0,0)
      start.setDate(start.getDate() + 3 - ((start.getDay() + 6) % 7))
      const week = Math.round(((start.getTime() - new Date(start.getFullYear(),0,4).getTime()) / 86400000 + (new Date(start.getFullYear(),0,4).getDay() + 6) % 7) / 7)
      return `${d.getFullYear()}-W${String(week).padStart(2,'0')}`
    }
    case 'day': return dateStr
    default: return dateStr
  }
}

function getPeriodDateRange(period, key) {
  if (period === 'year') {
    return { start: `${key}-01-01`, end: `${key}-12-31` }
  }
  if (period === 'quarter') {
    const [, y, q] = key.match(/(\d+)-Q(\d)/)
    const sm = (parseInt(q) - 1) * 3 + 1
    const em = parseInt(q) * 3
    const lastDay = new Date(parseInt(y), em, 0).getDate()
    return { start: `${y}-${String(sm).padStart(2,'0')}-01`, end: `${y}-${String(em).padStart(2,'0')}-${String(lastDay).padStart(2,'0')}` }
  }
  if (period === 'month') {
    const [, y, m] = key.match(/(\d+)-(\w+)/)
    const ms = { Jan:'01',Feb:'02',Mar:'03',Apr:'04',May:'05',Jun:'06',Jul:'07',Aug:'08',Sep:'09',Oct:'10',Nov:'11',Dec:'12' }
    const mi = ms[m] || '01'
    const lastDay = new Date(parseInt(y), parseInt(mi), 0).getDate()
    return { start: `${y}-${mi}-01`, end: `${y}-${mi}-${String(lastDay).padStart(2,'0')}` }
  }
  if (period === 'week') {
    const [, y, w] = key.match(/(\d+)-W(\d+)/)
    const jan4 = new Date(parseInt(y), 0, 4)
    const start = new Date(jan4.getTime())
    start.setDate(jan4.getDate() - ((jan4.getDay() + 6) % 7) + (parseInt(w) - 1) * 7)
    const end = new Date(start.getTime())
    end.setDate(start.getDate() + 6)
    const fmt = d => d.toISOString().slice(0, 10)
    return { start: fmt(start), end: fmt(end) }
  }
  if (period === 'day') {
    return { start: key, end: key }
  }
  return { start: '', end: '' }
}

const timelineSegments = computed(() => {
  const flights = flightColumns.value
  if (!flights.length) return []
  const counts = new Map()
  for (const f of flights) {
    if (!f.flightDate) continue
    const k = getPeriodKey(f.flightDate, timelinePeriod.value)
    counts.set(k, (counts.get(k) || 0) + 1)
  }
  const sorted = [...counts.keys()].sort()
  return sorted.map(k => {
    let short = k
    if (timelinePeriod.value === 'quarter') short = k.replace(/^\d+-Q/, 'Q')
    else if (timelinePeriod.value === 'month') short = k.replace(/^\d+-/, '')
    else if (timelinePeriod.value === 'week') short = k.replace(/^\d+-W/, 'W')
    return { value: k, short, label: k, count: counts.get(k) || 0 }
  })
})

const segIndex = computed(() => {
  const m = {}
  timelineSegments.value.forEach((s, i) => { m[s.value] = i })
  return m
})

function isInRange(value) {
  if (!rangeStartSeg.value && !rangeEndSeg.value) return true
  const idx = segIndex.value[value]
  if (idx === undefined) return false
  const si = rangeStartSeg.value ? (segIndex.value[rangeStartSeg.value] ?? 0) : 0
  const ei = rangeEndSeg.value ? (segIndex.value[rangeEndSeg.value] ?? timelineSegments.value.length - 1) : timelineSegments.value.length - 1
  return idx >= Math.min(si, ei) && idx <= Math.max(si, ei)
}

const rangeLeftPct = computed(() => {
  if (!timelineSegments.value.length) return 0
  const start = rangeStartSeg.value ? (segIndex.value[rangeStartSeg.value] ?? 0) : 0
  return (start / timelineSegments.value.length) * 100
})

const rangeRightPct = computed(() => {
  if (!timelineSegments.value.length) return 100
  const end = rangeEndSeg.value ? (segIndex.value[rangeEndSeg.value] ?? timelineSegments.value.length - 1) : timelineSegments.value.length - 1
  return ((end + 1) / timelineSegments.value.length) * 100
})

const rangeBarStyle = computed(() => {
  return { left: rangeLeftPct.value + '%', width: (rangeRightPct.value - rangeLeftPct.value) + '%' }
})

let rangeDragging = null

function setTimelinePeriod(period) {
  timelinePeriod.value = period
  rangeStartSeg.value = ''
  rangeEndSeg.value = ''
  showPeriodMenu.value = false
  buildMatrix()
}

function toggleRangeSegment(value) {
  const segs = timelineSegments.value
  if (!segs.length) return
  if (!rangeStartSeg.value || (rangeStartSeg.value && rangeEndSeg.value)) {
    rangeStartSeg.value = value
    rangeEndSeg.value = ''
  } else {
    const si = segIndex.value[rangeStartSeg.value]
    const ei = segIndex.value[value]
    if (ei < si) {
      rangeEndSeg.value = rangeStartSeg.value
      rangeStartSeg.value = value
    } else {
      rangeEndSeg.value = value
    }
  }
  buildMatrix()
}

function startRangeDrag(side, e) {
  rangeDragging = side
  const onMove = (ev) => {
    if (!rangeDragging || !timelineSegments.value.length) return
    const bar = e.currentTarget?.parentElement?.parentElement
    const container = bar?.querySelector?.('.h-6') || bar?.closest?.('[style*="min-width"]')
    if (!container) return
    const rect = container.getBoundingClientRect()
    const pct = Math.max(0, Math.min(100, ((ev.clientX - rect.left) / rect.width) * 100))
    const idx = Math.round((pct / 100) * (timelineSegments.value.length - 1))
    const seg = timelineSegments.value[Math.max(0, Math.min(idx, timelineSegments.value.length - 1))]
    if (!seg) return
    if (rangeDragging === 'left') {
      rangeStartSeg.value = seg.value
    } else {
      rangeEndSeg.value = seg.value
    }
  }
  const onUp = () => {
    rangeDragging = null
    document.removeEventListener('pointermove', onMove)
    document.removeEventListener('pointerup', onUp)
    buildMatrix()
  }
  document.addEventListener('pointermove', onMove)
  document.addEventListener('pointerup', onUp)
}

function clearTimeline() {
  rangeStartSeg.value = ''
  rangeEndSeg.value = ''
  buildMatrix()
}

function getPieces(row, flight) { return row.cells[flight.id] || 0 }

function cellClasses(row, flight, fi) {
  const pcs = getPieces(row, flight)
  if (!pcs) return ''
  const base = 'relative overflow-hidden'
  const pop = `pop-delay-${fi % 10}`
  const colGlow = hoverFlightCol.value === flight.id ? 'ring-1 ring-inset ring-white/20' : ''
  return `${base} chalk-bg chalk-text ${pop} ${colGlow}`
}

const arcCircum = 2 * Math.PI * 5.5

function arcOffset(row, flight) {
  const pcs = getPieces(row, flight)
  const total = row.totalPieces || 1
  const pct = Math.min(pcs / total, 1)
  return arcCircum * (1 - pct)
}

function rowBgClass(row) {
  return 'hover:bg-slate-50'
}

function mawbStatusClass(row) {
  const base = 'sticky bg-white px-2 py-2.5 font-bold border-r border-slate-300 truncate max-w-[180px] cursor-pointer transition-colors duration-150'
  if (row.hasDispatchedExcess) return `${base} text-rose-700 bg-rose-50/80 hover:bg-rose-100 border-l-4 border-l-rose-500`
  const s = row.status
  if (!s || s === 'BOOKED') return `${base} text-slate-600 bg-slate-50 hover:bg-slate-100 border-l-4 border-l-slate-400`
  if (s === 'RECEIVED') return `${base} text-blue-700 bg-blue-50/60 hover:bg-blue-100 border-l-4 border-l-blue-500`
  if (s === 'MANIFESTED') return `${base} text-amber-700 bg-amber-50/60 hover:bg-amber-100 border-l-4 border-l-amber-500`
  if (s === 'DEPARTED') return `${base} text-emerald-700 bg-emerald-50/60 hover:bg-emerald-100 border-l-4 border-l-emerald-500`
  return `${base} hover:bg-slate-50`
}

function statusTitle(row) {
  if (row.hasDispatchedExcess) return '&#9888; Despachado excede recibido — revisar ULDs'
  const s = row.status
  if (!s || s === 'BOOKED') return 'Reservado — clic para ir al recibo'
  if (s === 'RECEIVED') return 'Recibido — clic para ir al recibo'
  if (s === 'MANIFESTED') return 'Montado en load planning — clic para ir al recibo'
  if (s === 'DEPARTED') return 'Despachado — clic para ir al recibo'
  return 'Clic para ir al recibo'
}

function uldTooltip(row, flight) {
  const pcs = getPieces(row, flight)
  const uldCount = row.uldCountByFlight?.[flight.id] || 0
  if (!pcs) return ''
  const pcsLabel = `UP${pcs !== 1 ? 'S' : ''}`
  const uldLabel = `ULD${uldCount !== 1 ? 'S' : ''}`
  return `${pcs} ${pcsLabel} REPARTIDAS ENTRE ${uldCount} ${uldLabel}`
}

function formatDate(d) {
  if (!d) return '—'
  const parts = d.split('-')
  return parts.length === 3 ? parts[1] + '/' + parts[2] : d
}

function goToReceipt(row) {
  router.push({ name: 'receipts', query: { mawbId: row.mawbId } })
}

function startResize(e) {
  const section = tableSectionRef.value
  if (!section) return
  const startY = e.clientY
  const startHeight = section.offsetHeight
  const parent = section.parentElement
  const maxH = parent ? parent.clientHeight - 20 : 800
  const onMove = (ev) => {
    const delta = ev.clientY - startY
    const newH = Math.min(maxH, Math.max(180, startHeight + delta))
    section.style.flex = 'none'
    section.style.height = newH + 'px'
  }
  const onUp = () => {
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
  }
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
}

function scrollToFlight(flightId) {
  const el = scrollContainer.value
  if (!el) return
  const th = el.querySelector('thead tr th')
  if (!th) return
  const headerCells = Array.from(el.querySelectorAll('thead tr th'))
  const idx = headerCells.findIndex(c => {
    const text = c.textContent
    return text && text.includes(flightId.slice(0, 8))
  })
  if (idx >= 7) {
    const prevCells = headerCells.slice(7, idx)
    let offset = stickyOffsets.value[7] || stickyOffsets.value[6] || 550
    for (const c of prevCells) offset += c.offsetWidth || 84
    el.scrollTo({ left: offset - 20, behavior: 'smooth' })
  }
}

const mawbStats = computed(() => {
  const rows = filteredRows.value
  const totalReserved = rows.reduce((s, r) => s + (r.reservedPieces || 0), 0)
  const totalReceived = rows.reduce((s, r) => s + (r.receivedPieces || 0), 0)
  const totalDisp = rows.reduce((s, r) => s + (r.pcsDispatched || 0), 0)
  const tracked = rows.reduce((s, r) => s + Object.values(r.cells).reduce((a, b) => a + b, 0), 0)
  const departed = rows.filter(r => r.status === 'DEPARTED').length
  const overDisp = rows.filter(r => r.hasDispatchedExcess).length
  const diffCount = rows.filter(r => r.pieceDiff !== 0).length
  return [
    { label: "MAWBs", value: rows.length, sub: diffCount > 0 ? `${diffCount} con diferencias` : 'sin dif.', border: diffCount ? "border-l-orange-400" : "border-l-slate-700" },
    { label: "Reservadas", value: totalReserved, sub: `${totalReceived} rec.`, border: "border-l-slate-500" },
    { label: "Pzas Track", value: tracked, sub: `de ${totalReceived}`, border: "border-l-amber-500" },
    { label: "Despachadas", value: totalDisp, sub: `${overDisp > 0 ? overDisp + ' exceden' : 'sin exceso'}`, border: overDisp ? "border-l-rose-500" : "border-l-emerald-500" },
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
const totalReserved = computed(() =>
  filteredRows.value.reduce((s, r) => s + (r.reservedPieces || 0), 0)
)
const totalReceived = computed(() =>
  filteredRows.value.reduce((s, r) => s + (r.receivedPieces || 0), 0)
)
const totalDispatched = computed(() =>
  filteredRows.value.reduce((s, r) => s + (r.pcsDispatched || 0), 0)
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
  await store.loadBookings(localFlightId.value || undefined)
  highlightFlightId.value = localFlightId.value || null
  await buildMatrix()
}

onMounted(async () => {
  if (!store.flights.length) await store.loadFlights()
  await store.loadReceipts()
  await store.loadBookings()
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
thead th { position: sticky; top: 0; z-index: 20; }
tbody td { white-space: nowrap; }

.chalk-bg {
  background-color: #1e293b;
  background-image:
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 1px,
      rgba(255,255,255,0.03) 1px,
      rgba(255,255,255,0.03) 2px
    ),
    radial-gradient(ellipse at 30% 40%, rgba(255,255,255,0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 60%, rgba(0,0,0,0.08) 0%, transparent 50%);
  transition: background-color 0.2s;
}
.chalk-bg:hover {
  background-color: #334155;
}

.chalk-text {
  color: rgba(255,255,255,0.92);
  text-shadow: 0 0 4px rgba(255,255,255,0.15), 0 0 8px rgba(255,255,255,0.05);
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.chalk-swatch {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background-color: #1e293b;
  background-image: repeating-linear-gradient(0deg, transparent, transparent 1px, rgba(255,255,255,0.03) 1px, rgba(255,255,255,0.03) 2px);
}

.pop-delay-0 { animation: popIn 0.3s ease-out both; }
.pop-delay-1 { animation: popIn 0.3s ease-out 0.03s both; }
.pop-delay-2 { animation: popIn 0.3s ease-out 0.06s both; }
.pop-delay-3 { animation: popIn 0.3s ease-out 0.09s both; }
.pop-delay-4 { animation: popIn 0.3s ease-out 0.12s both; }
.pop-delay-5 { animation: popIn 0.3s ease-out 0.15s both; }
.pop-delay-6 { animation: popIn 0.3s ease-out 0.18s both; }
.pop-delay-7 { animation: popIn 0.3s ease-out 0.21s both; }
.pop-delay-8 { animation: popIn 0.3s ease-out 0.24s both; }
.pop-delay-9 { animation: popIn 0.3s ease-out 0.27s both; }

@keyframes popIn {
  0% { transform: scale(0.4); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
