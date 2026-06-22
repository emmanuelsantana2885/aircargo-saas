<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex flex-wrap justify-between items-end gap-2 border-b border-slate-200 pb-3 shrink-0">
      <div class="flex items-end gap-3">
        <div>
          <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">ULD Management Hub</h1>
          <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Operations // Ground Handling & Pallet Sheets</p>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Vuelo</span>
          <select v-model="selectedFlightId" @change="onFlightChange"
            class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-xs cursor-pointer min-w-[140px]">
            <option value="">Seleccionar vuelo</option>
            <option v-for="f in uldsStore.flights" :key="f.id" :value="f.id">
              UPS-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }})
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Fecha</span>
          <input v-model="filterDate" type="date"
            class="text-[11px] font-mono px-2 py-1 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
        </div>
      </div>
      <div class="flex items-center gap-2">
        <span v-if="pendingReceiptCount > 0"
          class="text-[9px] font-mono font-bold text-amber-600 bg-amber-50 border border-amber-200 px-2 py-1 rounded">
          &#9888; {{ pendingReceiptCount }} MAWB(s) sin recibo
        </span>
        <button @click="createNewBlankUld"
          class="bg-slate-950 text-white font-mono text-[11px] font-black uppercase tracking-wider px-4 py-2 rounded border border-slate-900 hover:bg-slate-800 transition-all flex items-center gap-2 shadow-pencil-marine">
          <span class="text-xs font-sans">&#65291;</span> Crear ULD
        </button>
      </div>
    </header>

    <section class="grid grid-cols-2 md:grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="stat in uldStats" :key="stat.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 border-l-slate-950/90 shadow-pencil-marine flex flex-col justify-between min-h-[68px]">
        <div class="relative z-10">
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">{{ stat.value }}</div>
        </div>
        <div class="text-[8px] font-mono text-slate-400 relative z-10 truncate flex justify-between items-center">
          <span>{{ stat.sub }}</span>
          <span :class="stat.trendClass" class="font-bold">{{ stat.trend }}</span>
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Código ULD</div>
        <div class="col-span-1 text-left">Vuelo</div>
        <div class="col-span-2 text-left">Ruta / Destino</div>
        <div class="col-span-2 text-left">Contor / Tipo</div>
        <div class="col-span-1 text-center">% Vol</div>
        <div class="col-span-1 text-center">MAWBs</div>
        <div class="col-span-1 text-right pr-2">Peso Bruto</div>
        <div class="col-span-2 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Flujo Rampa</div>
      </div>

      <div v-if="uldsStore.loading && !localUlds.length" class="flex-1 flex items-center justify-center">
        <span class="text-[10px] font-mono text-slate-400 animate-pulse">Cargando ULDs...</span>
      </div>

      <div v-else-if="localUlds.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">No hay ULDs — crea uno nuevo</p>
      </div>

      <div v-else class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="uld in localUlds" :key="uld.uid" class="flex flex-col">
          <div @click="toggleUldExpansion(uld.uid)"
            class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer"
            :class="[expandedUldId === uld.uid ? 'bg-slate-50/90 ring-1 ring-inset ring-slate-200' : '']">
            <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 flex items-center gap-1.5">
              <span class="text-[8px] text-slate-400 transition-transform duration-200" :class="{ 'rotate-90 text-slate-950': expandedUldId === uld.uid }">&#9654;</span>
              {{ uld.uldNumber || 'NUEVO-ULD' }}
            </div>
            <div class="col-span-1 font-mono font-bold text-[10px] text-slate-800 bg-white border border-slate-200 rounded px-1 py-0.5 w-max relative z-10 shadow-sm">
              {{ uld.flightLabel || 'TBD' }}
            </div>
            <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">{{ uld.route || 'Sin Ruta' }}</div>
            <div class="col-span-2 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[10px]">
              {{ uld.config || '---' }} <span class="text-slate-400 font-normal">// {{ uld.uldType || 'Falta Tipo' }}</span>
            </div>
            <div class="col-span-1 text-center font-mono font-black relative z-10" :class="uld.volumePct >= 90 ? 'text-emerald-600' : 'text-slate-950'">{{ uld.volumePct }}%</div>
            <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10 flex items-center justify-center gap-1">
              {{ (uld.mawbs || []).length }}
              <span v-if="uldHasPendingReceipt(uld)" class="text-amber-500 text-[8px]" title="Contiene MAWB(s) sin recibo de bodega">&#9888;</span>
            </div>
            <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
              {{ Number(uld.grossWeightLbs || 0).toLocaleString() }}<span class="text-[9px] text-slate-400 font-normal font-mono"> lb</span>
            </div>
            <div class="col-span-2 px-2 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
              <div class="flex items-center w-full justify-between relative px-2">
                <div class="absolute top-[5px] left-3 right-3 h-[3px] bg-slate-100 z-0 rounded-full flex">
                  <div class="h-full rounded-full transition-all duration-300" :class="getLineProgressColor(uld.status)" :style="{ width: getProgressWidth(uld.status) }"></div>
                </div>
                <div v-for="step in statusFlowSteps" :key="step" class="flex flex-col items-center z-10 relative">
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300" :class="getStatusDotClass(uld.status, step)"></span>
                  <span class="text-[7px] font-mono mt-1 font-black" :class="uld.status === step ? 'text-slate-950' : 'text-slate-300'">{{ step.slice(0, 3) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- FORM -->
          <div v-show="expandedUldId === uld.uid" class="bg-slate-50 border-b border-slate-200 px-12 py-4 transition-all duration-300 overflow-hidden">
            <div class="bg-white border border-slate-300 rounded shadow-sm max-w-5xl p-6 font-mono text-sm relative">
              <div class="flex justify-between items-center border-b border-slate-300 pb-3 mb-5">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-black text-slate-950 uppercase tracking-wider">ULD PALLET SHEET & MANIFEST</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-sm font-bold text-slate-400 uppercase">Volumen:</span>
                  <input v-model.number="uld.volumePct" type="number" min="0" max="100" class="w-14 text-center bg-slate-100 border border-slate-300 rounded font-bold text-slate-950 focus:outline-none text-sm" />
                  <span class="text-sm font-bold text-slate-600">%</span>
                </div>
              </div>

              <div class="grid grid-cols-4 gap-5 mb-6">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Código ULD *</label>
                  <input v-model="uld.uldNumber" type="text" placeholder="PMC-XXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm font-bold text-slate-950 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Config / Tipo</label>
                  <select v-model="uld.uldType" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm font-bold text-slate-800 focus:outline-none focus:border-slate-400">
                    <option v-for="t in uldTypes" :key="t" :value="t">{{ t }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Posición</label>
                  <input v-model="uld.position" type="text" placeholder="1L" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Sello Seguridad</label>
                  <input v-model="uld.sealNumber" type="text" placeholder="SC-XXXXXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 font-bold" />
                </div>
              </div>

              <!-- MAWB TABLE -->
              <div class="border border-slate-200 rounded overflow-hidden mb-6">
                <div class="bg-slate-950 text-white text-sm font-bold uppercase grid grid-cols-12 py-3 px-5 tracking-wide items-center gap-2">
                  <div class="col-span-3">MAWB</div>
                  <div class="col-span-2">DESCRIPCIÓN</div>
                  <div class="col-span-1 text-right">PCS ASIG</div>
                  <div class="col-span-1 text-right">PCS REC</div>
                  <div class="col-span-1 text-center">%</div>
                  <div class="col-span-1 text-right">DEST</div>
                  <div class="col-span-2 text-center">RECIBO</div>
                  <div class="col-span-1"></div>
                </div>
                <div class="divide-y divide-slate-100 max-h-[240px] overflow-y-auto scrollbar-none">
                  <div v-for="(mawb, mIdx) in uld.mawbs" :key="mIdx" class="grid grid-cols-12 items-center py-2 px-5 bg-white gap-2 text-sm">
                    <div class="col-span-3">
                      <select v-model="mawb.awbNumber" @change="onMawbSelect(uld, mIdx)"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 bg-transparent font-bold tracking-tight text-slate-800 text-xs">
                        <option value="" disabled>Seleccionar MAWB</option>
                        <option v-for="m in availableMawbs" :key="m.id" :value="m.awbNumber">
                          {{ m.awbNumber }} — {{ m.shipperName || m.consigneeName }} [{{ m.commodityType }}]
                        </option>
                      </select>
                    </div>
                    <div class="col-span-2">
                      <input v-model="mawb.commodityType" type="text" :placeholder="mawb.commodityHint || 'Dry Cargo'"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 bg-transparent font-medium text-slate-700 text-xs" />
                    </div>
                    <div class="col-span-1 flex items-center gap-1">
                      <input v-model.number="mawb.pieces" type="number" min="0"
                        class="w-full border-b py-1 text-right bg-transparent font-bold text-xs"
                        :class="mawb.receivedPieces != null && mawb.pieces > mawb.receivedPieces ? 'border-rose-400 text-rose-600 bg-rose-50' : 'border-slate-200 focus:border-slate-950'" />
                      <span v-if="mawb.receivedPieces != null && mawb.pieces > mawb.receivedPieces" class="text-rose-500 text-[9px]" title="Excede piezas recibidas">&#9888;</span>
                    </div>
                    <div class="col-span-1 text-right font-mono text-xs flex items-center justify-end gap-1"
                      :class="mawb.receivedPieces != null && mawb.pieces > mawb.receivedPieces ? 'text-rose-600' : mawb.receivedPieces != null ? 'text-emerald-600' : 'text-slate-400'">
                      <template v-if="mawb.receivedPieces != null">{{ mawb.receivedPieces }}</template>
                      <span v-else>&mdash;</span>
                    </div>
                    <div class="col-span-1 text-center flex items-center justify-center gap-1">
                      <input v-model.number="mawb.piecesPct" type="number" min="0" max="100"
                        class="w-10 border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 text-center bg-transparent font-bold text-rose-600 text-xs" />
                      <span class="text-xs text-slate-400">%</span>
                    </div>
                    <div class="col-span-1 text-right">
                      <input v-model="mawb.destination" type="text" maxlength="3"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1 text-right bg-transparent uppercase font-bold text-slate-600 text-xs" />
                    </div>
                    <div class="col-span-2 flex justify-center items-center gap-1 text-[9px] font-mono">
                      <span v-if="mawb.hasReceipt"
                        class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded font-bold text-emerald-700 bg-emerald-50 border border-emerald-200">
                        ✓ Recibido
                      </span>
                      <span v-else
                        class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded font-bold text-amber-700 bg-amber-50 border border-amber-200 cursor-help"
                        title="Esta MAWB no tiene recibo de bodega. Se recomienda emitir el recibo antes del despacho.">
                        ⚠ Pend.
                      </span>
                    </div>
                    <div class="col-span-1 text-center">
                      <button @click="removeMawbRow(uld, mIdx)" class="text-rose-400 hover:text-rose-600 text-xs">✕</button>
                    </div>
                  </div>
                </div>
                <div class="p-2 bg-slate-50 border-t border-slate-100 flex justify-between items-center text-[10px] text-slate-500">
                  <button @click="addMawbRow(uld)"
                    class="py-1.5 px-3 border border-dashed border-slate-300 rounded text-center hover:text-slate-950 transition-colors font-bold text-xs uppercase">
                    + MAWB
                  </button>
                  <div class="flex items-center gap-2">
                    <span v-if="uldPieceMismatchCount(uld)" class="text-rose-600 font-bold flex items-center gap-1">
                      &#9888; {{ uldPieceMismatchCount(uld) }} MAWB(s) exceden PCS recibidos
                    </span>
                    <span class="font-mono">PCS: {{ totalUldPieces(uld) }} / {{ totalUldReceivedPieces(uld) }} rec</span>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-4 gap-5 mb-6">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Tara (lbs)</label>
                  <input v-model.number="uld.tareLbs" type="number" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Peso Bruto (lbs)</label>
                  <input v-model.number="uld.grossWeightLbs" type="number" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Estado</label>
                    <select v-model="uld.status" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 font-bold">
                      <option value="OPEN">OPEN (Abierto)</option>
                      <option value="BUILT">BUILT (Armado)</option>
                      <option value="SEALED">SEALED (Precintado)</option>
                      <option value="LOADED">LOADED (Cargado)</option>
                      <option value="LEFT_BEHIND">LEFT BEHIND (Dejado)</option>
                    </select>
                </div>
                <div class="bg-emerald-50/50 flex flex-col justify-center rounded px-3 py-2 border border-emerald-100">
                  <span class="text-[9px] font-black text-emerald-700 uppercase tracking-wider">Peso Neto</span>
                  <span class="text-sm font-black text-emerald-600">{{ ((uld.grossWeightLbs || 0) - (uld.tareLbs || 0)).toLocaleString() }} lbs</span>
                </div>
              </div>

              <div class="grid grid-cols-3 gap-4 border-t border-slate-200 pt-5">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Ubicación / Puerta</label>
                  <input v-model="uld.door" type="text" placeholder="Puerta 4 / Patio" class="w-full border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 bg-slate-50 focus:outline-none uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Manifestado Por</label>
                  <input v-model="uld.filledBy" type="text" placeholder="Operadores de rampa" class="w-full border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 bg-slate-50 focus:outline-none font-bold" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Notas</label>
                  <input v-model="uld.notes" type="text" placeholder="Notas adicionales" class="w-full border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 bg-slate-50 focus:outline-none" />
                </div>
              </div>

              <div class="border-t border-slate-200 pt-5 flex justify-end gap-2 bg-slate-50/50 -mx-6 -mb-6 p-6 rounded-b">
                <div class="flex items-center gap-2 mr-auto">
                  <span class="text-[9px] font-black text-slate-400 uppercase tracking-widest">Vuelo</span>
                  <select v-model="uld.saveFlightId"
                    class="bg-white border border-slate-300 rounded px-2 py-1.5 font-bold text-slate-900 focus:outline-none uppercase text-xs min-w-[160px]">
                    <option value="" disabled>Seleccionar vuelo</option>
                    <option v-for="f in uldsStore.flights" :key="f.id" :value="f.id">
                      UPS-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }})
                    </option>
                  </select>
                </div>
                <button @click="saveUld(uld)"
                  class="bg-emerald-600 hover:bg-emerald-700 text-white font-mono font-black uppercase text-sm tracking-widest px-6 py-2.5 rounded shadow-md transition-all flex items-center gap-2">
                  {{ uld.backendId ? 'Actualizar ULD' : '🚀 Enviar a Load Planning' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUldsStore } from '../stores/ulds'
import { useAppStore } from '../stores/app'

const uldsStore = useUldsStore()
const appStore = useAppStore()

const uldTypes = ['PMC','PAH','PAG','PAJ','AAY','AAZ','AAD','PIP','BULK','AMP','AMJ']
const statusFlowSteps = ['OPEN', 'BUILT', 'SEALED', 'LOADED']

const specialItems = [
  { id: 'spc-sdq-sdf', awbNumber: 'SDQ/SDF', shipperName: 'Ruta Doméstica SDQ→SDF', consigneeName: 'Ruta Doméstica SDQ→SDF', commodityType: 'DOMESTIC', pieces: 0, destination: 'SDF', isSpecial: true },
  { id: 'spc-sdq-mia', awbNumber: 'SDQ/MIA', shipperName: 'Ruta Doméstica SDQ→MIA', consigneeName: 'Ruta Doméstica SDQ→MIA', commodityType: 'DOMESTIC', pieces: 0, destination: 'MIA', isSpecial: true },
  { id: 'spc-wwef', awbNumber: 'WWEF', shipperName: 'Worldwide Express Freight', consigneeName: 'WWEF', commodityType: 'EXPRESS', pieces: 0, destination: 'MIA', isSpecial: true },
  { id: 'spc-fcc', awbNumber: 'FCC', shipperName: 'Full Container Load', consigneeName: 'FCC Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-empty-uld', awbNumber: 'EMPTY ULD', shipperName: 'Empty ULD', consigneeName: 'Empty ULD Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-empty-bags', awbNumber: 'EMPTY BAGS', shipperName: 'Empty Bags', consigneeName: 'Empty Bags Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
  { id: 'spc-nets', awbNumber: 'NETS', shipperName: 'Cargo Nets', consigneeName: 'Cargo Nets Equipment', commodityType: 'EQUIPMENT', pieces: 0, destination: '', isSpecial: true },
]

const expandedUldId = ref(null)
const selectedFlightId = ref(null)
const filterDate = ref('')

// Local ULD list derived from backend + new unsaved
const localUlds = ref([])

const availableMawbs = computed(() => [...specialItems, ...appStore.mawbs])

const pendingReceiptCount = computed(() => {
  const allMawbs = localUlds.value.flatMap(u => u.mawbs || [])
  const uniqueAwbs = [...new Set(allMawbs.filter(m => m.awbNumber).map(m => m.awbNumber))]
  return uniqueAwbs.filter(awb => !mawbHasReceipt(awb)).length
})

function mawbReceiptInfo(awbNumber) {
  const m = appStore.mawbs.find(x => x.awbNumber === awbNumber)
  const receipt = appStore.receipts.find(r => r.mawb?.id === m?.id || r.mawbId === m?.id)
  return {
    hasReceipt: !!receipt,
    receivedPieces: receipt ? (receipt.pieceCount || 0) : 0,
  }
}

function mawbHasReceipt(awbNumber) {
  return mawbReceiptInfo(awbNumber).hasReceipt
}

function uldHasPendingReceipt(uld) {
  return (uld.mawbs || []).some(m => m.awbNumber && !mawbHasReceipt(m.awbNumber))
}

function totalUldPieces(uld) {
  return (uld.mawbs || []).reduce((s, m) => s + (m.pieces || 0), 0)
}

function totalUldReceivedPieces(uld) {
  return (uld.mawbs || []).reduce((s, m) => s + ((m.receivedPieces != null ? m.receivedPieces : m.pieces) || 0), 0)
}

function uldPieceMismatchCount(uld) {
  return (uld.mawbs || []).filter(m => m.receivedPieces != null && m.pieces > m.receivedPieces).length
}

const uldStats = computed(() => {
  const all = localUlds.value
  const totalWt = all.reduce((s, u) => s + (u.grossWeightLbs || 0), 0)
  return [
    { label: "ULDs en Rampa", value: all.length + " Activos", sub: "Registrados en sistema", trend: "OK", trendClass: "text-slate-600" },
    { label: "Peso Total", value: totalWt.toLocaleString() + " lbs", sub: "Suma bruta verificada", trend: "", trendClass: "" },
    { label: "Ocupación Media", value: all.length ? Math.round(all.reduce((s, u) => s + (u.volumePct || 0), 0) / all.length) + "%" : "—", sub: "Eficiencia de cubicaje", trend: "", trendClass: "" },
    { label: "Abiertos (OPEN)", value: all.filter(u => u.status === 'OPEN').length, sub: "En edición", trend: "Borradores", trendClass: "text-amber-600" },
    { label: "Armados (BUILT)", value: all.filter(u => u.status === 'BUILT' || u.status === 'SEALED' || u.status === 'LOADED').length, sub: "En plan de carga", trend: "OK", trendClass: "text-emerald-600" },
    { label: "Dejados (LB)", value: all.filter(u => u.status === 'LEFT_BEHIND').length, sub: "No despachados", trend: "Revisar", trendClass: "text-rose-600" },
  ]
})

function rebuildLocalList() {
  const backend = (uldsStore.activeUlds || []).map(u => ({
    uid: u.id,
    backendId: u.id,
    uldNumber: u.uldNumber,
    flightId: u.flightId,
    flightLabel: uldsStore.selectedFlight?.flightNumber || '',
    route: (uldsStore.selectedFlight?.origin || '') + ' -> ' + (uldsStore.selectedFlight?.destination || ''),
    uldType: u.uldType,
    config: u.config,
    position: u.position,
    sealNumber: u.sealNumber,
    tareLbs: u.tareLbs || u.tareWeight || 0,
    grossWeightLbs: u.grossWeightLbs || u.grossWeight || 0,
    status: u.status || 'OPEN',
    filledBy: '',
    notes: u.notes || '',
    door: u.door || '',
    saveFlightId: u.flightId || selectedFlightId.value,
    awbs: (u.awbs || []),
    volumePct: 0,
    mawbs: (u.awbs || []).map(m => ({
      awbNumber: m.mawbLabel || '',
      commodityType: m.description || 'DRY_CARGO',
      commodityHint: m.description || '',
      pieces: m.pieces || 0,
      piecesPct: m.piecesPct || 0,
      destination: m.destination || '-',
      mawbId: m.mawbId || null,
      ...mawbReceiptInfo(m.mawbLabel || ''),
    })),
  }))
  // Calculate volumePct as sum of piecesPct across all MAWBs, capped at 100
  backend.forEach(uld => {
    const total = (uld.mawbs || []).reduce((s, m) => s + (m.piecesPct || 0), 0)
    uld.volumePct = Math.min(total, 100)
  })
  // Merge with existing unsaved local ULDS
  const unsaved = localUlds.value.filter(u => !u.backendId)
  localUlds.value = [...unsaved, ...backend]
}

async function onFlightChange() {
  if (selectedFlightId.value) {
    localUlds.value = localUlds.value.filter(u => u.backendId)
    uldsStore.selectFlight(selectedFlightId.value)
    // No recargar MAWBs — se cargan todas globalmente
  }
}

function createNewBlankUld() {
  const flight = uldsStore.selectedFlight
  localUlds.value.unshift({
    uid: 'new-' + Date.now(),
    backendId: null,
    uldNumber: '',
    flightId: flight?.id || selectedFlightId.value,
    saveFlightId: flight?.id || selectedFlightId.value,
    flightLabel: flight?.flightNumber || '',
    route: (flight?.origin || 'SDQ') + ' -> ' + (flight?.destination || 'MIA'),
    uldType: 'PMC',
    config: '',
    position: '',
    sealNumber: '',
    tareLbs: 140,
    grossWeightLbs: 0,
    status: 'OPEN',
    volumePct: 0,
    filledBy: '',
    notes: '',
    door: '',
    mawbs: [],
  })
  expandedUldId.value = localUlds.value[0].uid
}

async function saveUld(uld) {
  if (!uld.uldNumber) {
    alert('Código ULD requerido')
    return
  }
  const flightId = uld.saveFlightId || selectedFlightId.value
  if (!flightId) {
    alert('Selecciona un vuelo para asignar este ULD')
    return
  }
  if (uldPieceMismatchCount(uld) > 0) {
    if (!confirm(`${uldPieceMismatchCount(uld)} MAWB(s) tienen más piezas asignadas que las recibidas en bodega. ¿Continuar de todas formas?`)) return
  }
  try {
    uld.flightId = flightId
    uld.notes = [uld.door ? `Ubicación: ${uld.door}` : '', uld.filledBy ? `Llenado por: ${uld.filledBy}` : '', uld.notes].filter(Boolean).join(' | ')
    const result = await uldsStore.dispatchUld(uld, flightId)
    uld.backendId = result?.id || uld.backendId
    // Delete existing ULD-AWB links before recreating
    if (uld.backendId) {
      const existing = await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.getByUld(uld.backendId))
      for (const link of (existing.data || [])) {
        await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.delete(link.id))
      }
    }
    // Create ULD-AWB links for each MAWB
    for (const m of (uld.mawbs || [])) {
      if (m.awbNumber && result?.id) {
        const matchingMawb = appStore.mawbs.find(x => x.awbNumber === m.awbNumber)
        await import('../api/uldAwbs').then(mod => mod.uldAwbsApi.create({
          uldId: result.id,
          mawbId: matchingMawb?.id || null,
          mawbLabel: m.awbNumber,
          description: m.commodityType || 'DRY_CARGO',
          destination: m.destination || uldsStore.selectedFlight?.destination,
          pieces: m.pieces || 0,
          piecesPct: m.piecesPct || 0,
        }))
      }
    }
    expandedUldId.value = null
    if (selectedFlightId.value) {
      await uldsStore.loadUldsForFlight(selectedFlightId.value)
    }
    await appStore.loadAllMawbs()
  } catch (e) {
    alert('Error: ' + (e.response?.data?.message || e.message))
  }
}

function toggleUldExpansion(uid) {
  expandedUldId.value = expandedUldId.value === uid ? null : uid
}

function addMawbRow(uld) {
  const selectedFlight = uldsStore.selectedFlight
  uld.mawbs.push({ awbNumber: '', commodityType: 'DRY_CARGO', commodityHint: '', pieces: 0, piecesPct: 0, destination: selectedFlight?.destination || 'MIA', mawbId: null, hasReceipt: false, receivedPieces: 0 })
}

function removeMawbRow(uld, index) {
  uld.mawbs.splice(index, 1)
}

function onMawbSelect(uld, mIdx) {
  const selected = availableMawbs.value.find(m => m.awbNumber === uld.mawbs[mIdx].awbNumber)
  if (selected) {
    uld.mawbs[mIdx].commodityType = selected.commodityType || 'DRY_CARGO'
    uld.mawbs[mIdx].commodityHint = selected.commodityType || ''
    uld.mawbs[mIdx].pieces = selected.pieces || 0
    uld.mawbs[mIdx].destination = selected.destination || uldsStore.selectedFlight?.destination || 'MIA'
    uld.mawbs[mIdx].mawbId = selected.isSpecial ? null : selected.id
    if (!selected.isSpecial) {
      const info = mawbReceiptInfo(selected.awbNumber)
      uld.mawbs[mIdx].hasReceipt = info.hasReceipt
      uld.mawbs[mIdx].receivedPieces = info.receivedPieces
    }
  }
}

function getProgressWidth(status) {
  if (status === 'LEFT_BEHIND') return '100%'
  if (status === 'OPEN') return '0%'
  if (status === 'BUILT') return '33%'
  if (status === 'SEALED') return '66%'
  if (status === 'LOADED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'LEFT_BEHIND') return 'bg-rose-500'
  if (status === 'BUILT') return 'bg-amber-500'
  if (status === 'SEALED') return 'bg-emerald-500'
  if (status === 'LOADED') return 'bg-slate-950'
  return 'bg-slate-200'
}

function getStatusDotClass(currentStatus, step) {
  if (currentStatus === step) {
    if (step === 'LEFT_BEHIND') return 'bg-rose-500 border-rose-600 scale-125'
    if (step === 'OPEN') return 'bg-amber-500 border-amber-600 scale-125'
    if (step === 'BUILT') return 'bg-amber-600 border-amber-700 scale-125'
    if (step === 'SEALED') return 'bg-emerald-500 border-emerald-600 scale-125'
    if (step === 'LOADED') return 'bg-slate-950 border-slate-950 scale-125'
  }
  const order = ['OPEN', 'BUILT', 'SEALED', 'LOADED']
  if (currentStatus === 'LEFT_BEHIND') return 'bg-slate-200 border-slate-300'
  if (order.indexOf(currentStatus) >= order.indexOf(step)) return 'bg-slate-400 border-slate-500'
  return 'bg-slate-200 border-slate-300'
}

onMounted(async () => {
  await uldsStore.loadFlights()
  await appStore.loadAllMawbs()
  await appStore.loadReceipts()
  if (uldsStore.flights.length) {
    selectedFlightId.value = uldsStore.flights[0].id
    await onFlightChange()
  }
})

watch(() => uldsStore.activeUlds, () => rebuildLocalList(), { deep: true })
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04); }
.chalk-sketch { position: relative; overflow: hidden; }
.chalk-sketch::before { content: ""; position: absolute; inset: 0; opacity: 0.2; pointer-events: none; background-image: repeating-linear-gradient(30deg, rgba(71,85,105,0.06) 0px, rgba(71,85,105,0.06) 0.8px, transparent 0.8px, transparent 4px), repeating-linear-gradient(-30deg, rgba(71,85,105,0.03) 0px, rgba(71,85,105,0.03) 0.8px, transparent 0.8px, transparent 4px); }
.row-pencil { position: relative; }
.row-pencil::before { content: ""; position: absolute; inset: 0; opacity: 0; transition: opacity 0.12s; pointer-events: none; background-image: repeating-linear-gradient(45deg, rgba(15,32,67,0.05) 0px, rgba(15,32,67,0.05) 0.6px, transparent 0.6px, transparent 2px); }
.row-pencil:hover { background-color: rgba(243,247,254,0.6); box-shadow: inset 3px 0 0 0 #0f172a; }
.row-pencil:hover::before { opacity: 1; }
</style>
