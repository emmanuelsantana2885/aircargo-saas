<template>
  <div class="p-4 bg-slate-50 h-screen max-h-screen flex flex-col justify-between text-slate-900 font-mono antialiased overflow-hidden select-none">
    
    <header class="flex justify-between items-center border border-slate-300 bg-white p-3 rounded-t-lg shadow-pencil-marine shrink-0">
      <div class="flex items-center gap-6">
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Flight Number</span>
          <select v-model="selectedFlightId" @change="syncFlightMetadata" class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-xs cursor-pointer">
            <option v-for="flight in flightDatabase" :key="flight.id" :value="flight.id">
              {{ flight.number }}
            </option>
          </select>
        </div>

        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col justify-center">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Aircraft-Tail</span>
          <span class="text-xs font-black text-slate-950 uppercase tracking-wider">{{ activeFlightMeta.tail }}</span>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col justify-center">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Ruta</span>
          <span class="text-xs font-black text-slate-700 uppercase tracking-widest">{{ activeFlightMeta.route }}</span>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col justify-center">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Date</span>
          <span class="text-xs font-black text-slate-500 font-sans">{{ activeFlightMeta.date }}</span>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <button @click="triggerImport" class="bg-slate-100 hover:bg-slate-200 border border-slate-300 text-slate-800 px-3 py-1 rounded text-[9px] font-black uppercase tracking-widest transition-all">Import XLSX</button>
        <button @click="exportToXLSX" class="bg-slate-950 hover:bg-slate-900 text-white px-3 py-1 rounded text-[9px] font-black uppercase tracking-widest transition-all">Export XLSX</button>
      </div>
    </header>

    <section class="grid grid-cols-1 md:grid-cols-5 gap-3 my-2.5 shrink-0 bg-white border border-slate-200 p-2.5 rounded shadow-pencil-marine">
      <div class="space-y-0.5">
        <span class="block text-[8px] font-black text-slate-400 uppercase tracking-widest">Total ULDs</span>
        <div class="text-base font-black text-slate-950 uppercase">{{ calculatedTotals.uldsCount }} ULD(s)</div>
      </div>
      <div class="space-y-0.5 border-l border-slate-200 pl-3">
        <span class="block text-[8px] font-black text-slate-400 uppercase tracking-widest">Total Pieces</span>
        <div class="text-base font-black text-slate-950">{{ calculatedTotals.pcs }} PCS</div>
      </div>
      <div class="space-y-0.5 border-l border-slate-200 pl-3">
        <span class="block text-[8px] font-black text-slate-400 uppercase tracking-widest">Gross Weight</span>
        <div class="text-base font-black text-slate-950">{{ calculatedTotals.gross.toLocaleString() }} lbs</div>
      </div>
      <div class="space-y-0.5 border-l border-slate-200 pl-3">
        <span class="block text-[8px] font-black text-slate-400 uppercase tracking-widest">Tare Weight</span>
        <div class="text-base font-black text-slate-400">{{ calculatedTotals.tare.toLocaleString() }} lbs</div>
      </div>
      <div class="space-y-0.5 border-l border-slate-200 pl-3 flex flex-col justify-center">
        <div class="flex justify-between text-[8px] font-black text-slate-400 uppercase tracking-widest mb-0.5">
          <span>Net Payload</span>
          <span class="text-emerald-600 font-black">{{ (calculatedTotals.gross - calculatedTotals.tare).toLocaleString() }} lbs</span>
        </div>
        <div class="h-1.5 bg-slate-200 rounded-full overflow-hidden">
          <div class="h-full bg-slate-950" :style="{ width: calculatedTotals.payloadPct + '%' }"></div>
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded-lg overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1">
      
      <div class="bg-slate-100 text-slate-700 text-[8.5px] font-black uppercase tracking-widest grid grid-cols-11 py-2.5 px-4 items-center shrink-0 border-b border-slate-300 whitespace-nowrap">
        <div class="col-span-1 text-left">ULD ID</div>
        <div class="col-span-1 text-right pr-4">PIECES</div>
        <div class="col-span-1 text-right pr-4">%</div>
        <div class="col-span-1 text-right pr-4">PESO TOTAL</div>
        <div class="col-span-1 text-right pr-4">TARA</div>
        <div class="col-span-1 text-center">CONFIG.</div>
        <div class="col-span-1 text-left px-2">SELLO #</div>
        <div class="col-span-1 text-center">POSICIÓN</div>
        <div class="col-span-1 text-left px-2">DESCRIPCIÓN</div>
        <div class="col-span-1 text-left px-1">No GUIA</div>
        <div class="col-span-1 text-center">DEST</div>
      </div>
      
      <div class="divide-y divide-slate-200 text-[11px] overflow-y-auto flex-1 min-h-0 scrollbar-none bg-slate-100/30">
        
        <div v-for="(uldGroup, uIdx) in activeManifest" :key="uIdx" 
             class="grid grid-cols-11 relative bg-white lp-container-block"
             :class="getRowBgStyle(uldGroup.status)">
          
          <div class="col-span-1 font-black text-blue-600 pl-4 py-2 border-r border-slate-200 flex flex-col justify-center bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            <span class="tracking-tight">{{ uldGroup.uld }}</span>
            <div class="flex gap-0.5 mt-1 font-mono text-[8px] font-black scale-90 origin-left">
              <span class="px-1 py-px bg-slate-200 text-slate-600 rounded-[2px] border border-slate-300">E</span>
              <span class="px-1 py-px bg-amber-500 text-white rounded-[2px]">F</span>
              <span class="px-1 py-px" :class="uldGroup.items.length > 1 ? 'bg-purple-600 text-white rounded-[2px]' : 'bg-slate-100 text-slate-400 border border-slate-200'">C</span>
              <span class="px-1 py-px bg-slate-100 text-slate-400 border border-slate-200 rounded-[2px]">W</span>
              <span v-if="uldGroup.pos" class="px-1 py-px bg-emerald-600 text-white rounded-[2px]">S</span>
            </div>
          </div>

          <div class="col-span-2 grid grid-cols-2 bg-transparent divide-x divide-slate-100/50" 
               :style="{ gridColumn: '2 / span 2', gridRow: `1 / span ${uldGroup.items.length}` }">
            <template v-for="(item, mIdx) in uldGroup.items" :key="'g1-'+mIdx">
              <div class="text-right font-black text-slate-950 pr-4 py-2 flex items-center justify-end h-[42px] border-b border-slate-100 last:border-b-0">{{ item.pcs }}</div>
              <div class="text-right font-bold text-slate-500 pr-4 py-2 flex items-center justify-end h-[42px] border-b border-slate-100 last:border-b-0">{{ item.volumePct ? item.volumePct + '%' : '-' }}</div>
            </template>
          </div>

          <div class="col-span-1 text-right font-black text-slate-950 pr-4 py-2 border-r border-slate-200 flex items-center justify-end bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            {{ uldGroup.weight ? uldGroup.weight.toLocaleString() : '0' }}
          </div>
          
          <div class="col-span-1 text-right font-bold text-slate-400 pr-4 py-2 border-r border-slate-200 flex items-center justify-end bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            {{ uldGroup.tara || '0' }}
          </div>
          
          <div class="col-span-1 text-center font-black text-slate-500 py-2 border-r border-slate-200 flex items-center justify-center bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            {{ uldGroup.config || '-' }}
          </div>
          
          <div class="col-span-1 px-2 text-[10px] text-slate-500 truncate py-2 border-r border-slate-200 flex items-center bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            {{ uldGroup.sello || '---' }}
          </div>

          <div class="col-span-1 text-center border-r border-slate-200 flex items-center justify-center px-1 bg-inherit z-10"
               :style="{ gridRow: `1 / span ${uldGroup.items.length}` }">
            <span v-if="uldGroup.pos" 
                  class="px-2.5 py-1 rounded text-[10px] font-black border transition-all"
                  :class="uldGroup.status === 'COMPLETED' ? 'bg-emerald-100 border-emerald-400 text-emerald-950' : 'bg-slate-100 border-slate-300 text-slate-700'">
              {{ uldGroup.pos }}
            </span>
            <span v-else class="text-slate-300 font-black italic text-[9px]">W/O</span>
          </div>

          <div class="col-span-3 grid grid-cols-3 bg-transparent divide-x divide-slate-100/50"
               :style="{ gridColumn: '9 / span 3', gridRow: `1 / span ${uldGroup.items.length}` }">
            <template v-for="(item, mIdx) in uldGroup.items" :key="'g2-'+mIdx">
              <div class="px-2 text-[10px] text-slate-600 truncate uppercase tracking-tight font-sans font-black flex items-center h-[42px] border-b border-slate-100 last:border-b-0">{{ item.description || 'DRY CARGO' }}</div>
              <div class="font-black text-slate-900 truncate tracking-tight px-1.5 flex items-center h-[42px] border-b border-slate-100 last:border-b-0">{{ item.mawb }}</div>
              <div class="font-black text-slate-700 uppercase tracking-widest text-center flex items-center justify-center h-[42px] border-b border-slate-100 last:border-b-0">{{ item.destino || '-' }}</div>
            </template>
          </div>

        </div>
      </div>

      <div class="bg-amber-50/60 border-t border-slate-300 grid grid-cols-11 py-2 px-4 items-center text-[9px] font-black shrink-0 text-slate-900 uppercase tracking-wider whitespace-nowrap">
        <div class="col-span-3 text-slate-400 tracking-widest">RESUMEN CONSOLIDADO MANIFIESTO V8</div>
        <div class="col-span-2 text-right pr-4">PCS: {{ calculatedTotals.pcs }}</div>
        <div class="col-span-3 text-right pr-4">BRUTO: {{ calculatedTotals.gross.toLocaleString() }} lbs</div>
        <div class="col-span-3 text-right text-emerald-600 font-black pr-4">
          NETO TOTAL: {{ (calculatedTotals.gross - calculatedTotals.tare).toLocaleString() }} lbs
        </div>
      </div>
    </section>

    <footer class="p-2 border border-slate-200 bg-white rounded-b-lg shrink-0 flex flex-col gap-1 text-[9px]">
      <div class="grid grid-cols-2 md:grid-cols-5 gap-2 font-black whitespace-nowrap uppercase tracking-widest text-[8px] text-slate-500">
        <div class="flex items-center gap-1.5"><div class="w-2.5 h-2.5 bg-amber-100 border border-amber-300 rounded-sm"></div><span>Incompleto</span></div>
        <div class="flex items-center gap-1.5"><div class="w-2.5 h-2.5 bg-white border border-slate-300 rounded-sm"></div><span>Completado / Cerrado</span></div>
        <div class="flex items-center gap-1.5"><div class="w-2.5 h-2.5 bg-emerald-500 rounded-sm"></div><span>Matrícula Asignada</span></div>
        <div class="flex items-center gap-1.5"><div class="w-2.5 h-2.5 bg-slate-100 border border-slate-300 rounded-sm"></div><span>En Rampa</span></div>
        <div class="flex items-center gap-1.5"><div class="w-2.5 h-2.5 bg-pink-100 border border-pink-300 rounded-sm"></div><span>Left Behind</span></div>
      </div>
    </footer>

    <input type="file" ref="fileInput" @change="handleFileImport" accept=".xlsx, .xls" class="hidden" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedFlightId = ref('1')
const fileInput = ref(null)

const flightDatabase = ref([
  { id: '1', number: 'UPS0335', tail: 'N341UP', route: 'SDQ / MIA', date: '15-Jun-26' },
  { id: '2', number: 'UPS0403', tail: 'N284UP', route: 'SDQ / MIA', date: '16-Jun-26' }
])

const loadPlanningManifests = ref({
  '1': [
    {
      uld: 'PMC-95193', pos: 'P1', config: 'AAZ', sello: '', weight: 1680, tara: 270, status: 'COMPLETED',
      items: [{ mawb: '406-05387675', pcs: 4, volumePct: 100, description: 'DRY CARGO', destino: 'MIA' }]
    },
    {
      uld: 'PMH-37460', pos: '13', config: 'AAY', sello: '', weight: 1780, tara: 300, status: 'INCOMPLETE',
      items: [
        { mawb: '406-05387675', pcs: 2, volumePct: 50, description: 'DRY CARGO', destino: 'MIA' },
        { mawb: '406-04957013', pcs: 2, volumePct: 50, description: 'DRY CARGO', destino: 'MIA' }
      ]
    },
    {
      uld: 'PAH-36932', pos: '12R', config: 'AAY', sello: '', weight: 1695, tara: 300, status: 'INCOMPLETE',
      items: [
        { mawb: '406-03904121', pcs: 2, volumePct: 100, description: 'DRY CARGO', destino: 'MIA' },
        { mawb: '406-05387675', pcs: 2, volumePct: 50, description: 'DRY CARGO', destino: 'MIA' }
      ]
    },
    {
      uld: 'AAD-9745', pos: '10L', config: 'AAD', sello: '2172373', weight: 2135, tara: 630, status: 'IN_RAMP',
      items: [
        { mawb: '406-03904121', pcs: 2, volumePct: 50, description: 'DRY CARGO', destino: 'MIA' },
        { mawb: '406-04957013', pcs: 1, volumePct: 25, description: 'DRY CARGO', destino: 'MIA' },
        { mawb: '406-05387675', pcs: 1, volumePct: 25, description: 'DRY CARGO', destino: 'MIA' }
      ]
    }
  ],
  '2': [
    {
      uld: 'PMC-93812', pos: 'P4', config: 'AAZ', sello: '2172400', weight: 2820, tara: 270, status: 'COMPLETED',
      items: [{ mawb: '406-04956954', pcs: 4, volumePct: 100, description: 'DRY CARGO', destino: 'MIA' }]
    }
  ]
})

const activeFlightMeta = computed(() => {
  return flightDatabase.value.find(f => f.id === selectedFlightId.value) || flightDatabase.value[0]
})

const activeManifest = computed(() => {
  return loadPlanningManifests.value[selectedFlightId.value] || []
})

const calculatedTotals = computed(() => {
  let uldsCount = activeManifest.value.length
  let pcs = 0, gross = 0, tare = 0

  activeManifest.value.forEach(uld => {
    gross += uld.weight || 0
    tare += uld.tara || 0
    uld.items.forEach(item => { pcs += item.pcs || 0 })
  })

  return { uldsCount, pcs, gross, tare, payloadPct: uldsCount ? 82 : 0 }
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

function syncFlightMetadata() {
  console.log('Filtro modificado para el vuelo:', activeFlightMeta.value.number)
}

function triggerImport() { fileInput.value.click() }
function handleFileImport(e) { console.log('Procesando archivo:', e.target.files[0]?.name) }
function exportToXLSX() { console.log('Exportando manifiesto de rampa unificado...') }
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15, 32, 67, 0.05), 1px 3px 6px rgba(15, 32, 67, 0.04); }

/* Efecto Hover unificado sobre el bloque completo del ULD */
.lp-container-block:hover {
  background-color: rgba(241, 245, 249, 0.5) !important;
}
</style>
