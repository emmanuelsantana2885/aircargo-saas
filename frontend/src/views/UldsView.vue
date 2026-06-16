<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">ULD Management Hub</h1>
        <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Operations // Ground Handling & Pallet Sheets</p>
      </div>
      
      <div class="flex items-center gap-4">
        <button @click="createNewBlankUld" 
          class="bg-slate-950 text-white font-mono text-[11px] font-black uppercase tracking-wider px-4 py-2 rounded border border-slate-900 hover:bg-slate-800 transition-all flex items-center gap-2 shadow-pencil-marine">
          <span class="text-xs font-sans">&#65291;</span> Crear ULD / Pallet Sheet
        </button>
        
        <div class="h-6 w-[1px] bg-slate-200"></div>

        <div class="flex items-center gap-3 font-mono text-[10px] font-bold">
          <span class="text-slate-400">VINCULADO: <strong class="text-slate-950 bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">LoadPlanningView.vue</strong></span>
          <div class="flex items-center gap-1.5 text-slate-500">
            <span class="h-2 w-2 rounded-full bg-blue-600 animate-pulse"></span> OPERATIONAL SYSTEM LIVE
          </div>
        </div>
      </div>
    </header>

    <section class="grid grid-cols-2 md:grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="stat in uldStats" :key="stat.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 border-l-slate-950/90 shadow-pencil-marine flex flex-col justify-between min-h-[68px]">
        <div class="relative z-10">
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">
            {{ stat.value }}
          </div>
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
      
      <div class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="uld in uldsList" :key="uld.id" class="flex flex-col">
          
          <div @click="toggleUldExpansion(uld.id)"
            class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer"
            :class="[
              expandedUldId === uld.id ? 'bg-slate-50/90 ring-1 ring-inset ring-slate-200' : '',
              !uld.sentToLoadPlanning ? 'bg-filling-pattern shadow-[inset_6px_0_0_0_#f59e0b]' : ''
            ]">
            
            <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 flex items-center gap-1.5">
              <span class="text-[8px] text-slate-400 transition-transform duration-200" :class="{ 'rotate-90 text-slate-950': expandedUldId === uld.id }">&#9654;</span>
              {{ uld.id || 'NUEVO-ULD' }}
              <span v-if="!uld.sentToLoadPlanning" class="text-[7.5px] bg-amber-500/10 text-amber-700 border border-amber-300 font-bold px-1 rounded tracking-tight animate-pulse shrink-0 font-sans">LLENANDO</span>
            </div>
            
            <div class="col-span-1 font-mono font-bold text-[10px] text-slate-800 bg-white border border-slate-200 rounded px-1 py-0.5 w-max relative z-10 shadow-sm">
              {{ uld.flight || 'TBD' }}
            </div>
            
            <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">
              {{ uld.route || 'Sin Ruta' }}
            </div>

            <div class="col-span-2 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[10px]">
              {{ uld.config || '---' }} <span class="text-slate-400 font-normal">// {{ uld.type || 'Falta Tipo' }}</span>
            </div>

            <div class="col-span-1 text-center font-mono font-black relative z-10" :class="uld.volumePct >= 90 ? 'text-emerald-600' : 'text-slate-950'">
              {{ uld.volumePct }}%
            </div>
            
            <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
              {{ uld.mawbs.length }}
            </div>
            
            <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
              {{ Number(uld.grossWeight || 0).toLocaleString() }}<span class="text-[9px] text-slate-400 font-normal font-mono"> lb</span>
            </div>
            
            <div class="col-span-2 px-2 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
              <div class="flex items-center w-full justify-between relative px-2">
                <div class="absolute top-[5px] left-3 right-3 h-[3px] bg-slate-100 z-0 rounded-full flex">
                  <div class="h-full rounded-full transition-all duration-300" :class="getLineProgressColor(uld.status)" :style="{ width: getProgressWidth(uld.status) }"></div>
                </div>
                
                <div v-for="step in ['RECEIVING', 'PALLETIZED', 'DEPARTED']" :key="step" class="flex flex-col items-center z-10 relative">
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300" :class="getStatusDotClass(uld.status, step)"></span>
                  <span class="text-[7px] font-mono mt-1 font-black" :class="uld.status === step ? 'text-slate-950' : 'text-slate-300'">{{ step.slice(0,3) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-show="expandedUldId === uld.id" class="bg-slate-50 border-b border-slate-200 px-12 py-4 transition-all duration-300 overflow-hidden">
            <div class="bg-white border border-slate-300 rounded shadow-sm max-w-4xl p-4 font-mono text-[11px] relative">
              
              <div v-if="!uld.sentToLoadPlanning" class="mb-3 p-2 bg-amber-50 border border-amber-200 rounded text-amber-800 text-[10px] flex justify-between items-center">
                <span>⚠️ <strong>CONTRALORÍA DE RAMPA:</strong> Este contenedor se está editando en tiempo real en pista. No está reflejado en el plan maestro de vuelo.</span>
                <span class="font-black tracking-widest uppercase text-[8px] bg-amber-200 px-1.5 py-0.5 rounded">MODO BORRADOR ABIERTO</span>
              </div>

              <div class="flex justify-between items-center border-b border-slate-300 pb-2 mb-3">
                <div class="flex items-center gap-2">
                  <span class="text-xs font-black text-slate-950 uppercase tracking-wider">ULD PALLET SHEET & MANIFEST</span>
                  <span class="text-[9px] text-slate-400 font-bold">// CONTROL ADICIÓN AUTOMÁTICA</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-[9px] font-bold text-slate-400 uppercase">Volumen Ocupado:</span>
                  <input v-model.number="uld.volumePct" type="number" min="0" max="100" class="w-12 text-center bg-slate-100 border border-slate-300 rounded font-bold text-slate-950 focus:outline-none" />
                  <span class="text-xs font-bold text-slate-600">%</span>
                </div>
              </div>

              <div class="grid grid-cols-4 gap-3 mb-4">
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Código Identificador</label>
                  <input v-model="uld.id" type="text" placeholder="PMC-XXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 font-bold text-slate-950 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Vuelo Destino</label>
                  <input v-model="uld.flight" type="text" placeholder="UPS-XXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 font-bold text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Fecha Ejecución</label>
                  <input v-model="uld.executionDate" type="date" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Sello de Seguridad No.</label>
                  <input v-model="uld.sealNumber" type="text" placeholder="SC-XXXXXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400 font-bold" />
                </div>
              </div>

              <div class="grid grid-cols-4 gap-3 mb-4">
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Configuración Contorno</label>
                  <input v-model="uld.config" type="text" placeholder="AAZ / AAY" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Tipo de Estructura</label>
                  <select v-model="uld.type" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400">
                    <option value="Contenedor">Contenedor Metálico</option>
                    <option value="Pallet">Pallet / Red H6</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Ruta Conexión</label>
                  <input v-model="uld.route" type="text" placeholder="SDQ -> MIA" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-1">Estado Operación</label>
                  <select v-model="uld.status" class="w-full bg-slate-50 border border-slate-200 rounded px-2 py-1 text-slate-800 focus:outline-none focus:border-slate-400 font-bold">
                    <option value="RECEIVING">RECEIVING (Abierto)</option>
                    <option value="PALLETIZED">PALLETIZED (Armado)</option>
                    <option value="DEPARTED">DEPARTED (Despachado)</option>
                  </select>
                </div>
              </div>

              <div class="border border-slate-200 rounded overflow-hidden mb-3">
                <div class="bg-slate-950 text-white text-[8.5px] font-bold uppercase grid grid-cols-12 py-1 px-3 tracking-wide items-center">
                  <div class="col-span-5">Número de Guía Comercial (MAWB)</div>
                  <div class="col-span-3 text-right">Piezas (PCS)</div>
                  <div class="col-span-3 text-right">Destino (Airport)</div>
                  <div class="col-span-1 text-center"></div>
                </div>
                
                <div class="divide-y divide-slate-100 max-h-[110px] overflow-y-auto scrollbar-none">
                  <div v-for="(mawb, mIdx) in uld.mawbs" :key="mIdx" class="grid grid-cols-12 items-center py-1 px-3 bg-white gap-2">
                    <div class="col-span-5">
                      <input v-model="mawb.id" type="text" placeholder="406-XXXXXXXX" class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-0.5 bg-transparent font-bold tracking-tight text-slate-800" />
                    </div>
                    <div class="col-span-3">
                      <input v-model.number="mawb.pcs" type="number" class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-0.5 text-right bg-transparent font-bold" />
                    </div>
                    <div class="col-span-3">
                      <input v-model="mawb.dest" type="text" class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-0.5 text-right bg-transparent uppercase font-bold text-slate-600" />
                    </div>
                    <div class="col-span-1 text-center">
                      <button @click="removeMawbRow(uld, mIdx)" class="text-rose-500 hover:text-rose-700 font-sans text-xs font-bold">✕</button>
                    </div>
                  </div>
                </div>
                
                <div class="p-1 bg-slate-50 border-t border-slate-100">
                  <button @click="addMawbRow(uld)" class="w-full py-1 border border-dashed border-slate-300 rounded text-center text-slate-500 hover:text-slate-950 transition-colors font-black text-[9px] uppercase">+ Vincular Registro de Guía / MAWB</button>
                </div>
              </div>

              <div class="grid grid-cols-3 border border-slate-200 rounded overflow-hidden bg-slate-50 text-center divide-x divide-slate-200 mb-4">
                <div class="p-2">
                  <span class="block text-[8px] font-black text-slate-400 uppercase">Peso Bruto Verificado (lbs)</span>
                  <input v-model.number="uld.grossWeight" type="number" class="w-24 text-center bg-transparent border-b border-slate-300 font-mono font-black text-slate-950 mt-1 focus:outline-none text-xs" />
                </div>
                <div class="p-2">
                  <span class="block text-[8px] font-black text-slate-400 uppercase">Tara del Contenedor (lbs)</span>
                  <input v-model.number="uld.tareWeight" type="number" class="w-24 text-center bg-transparent border-b border-slate-300 font-mono font-black text-slate-950 mt-1 focus:outline-none text-xs" />
                </div>
                <div class="p-2 bg-emerald-50/50 flex flex-col justify-center">
                  <span class="block text-[7.5px] font-black text-emerald-700 uppercase tracking-wider">Cálculo de Peso Neto</span>
                  <span class="block text-xs font-black text-emerald-600 mt-0.5">{{ ((uld.grossWeight || 0) - (uld.tareWeight || 0)).toLocaleString() }} lbs</span>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4 border-t border-slate-200 pt-3 pb-4 text-[10px]">
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-0.5">Manifestado y Llenado Por (Crew)</label>
                  <input v-model="uld.filledBy" type="text" placeholder="Operadores de rampa asignados" class="w-full border border-slate-200 rounded px-2 py-1 text-slate-800 bg-slate-50 focus:outline-none font-bold" />
                </div>
                <div>
                  <label class="block text-[8px] font-black text-slate-400 uppercase mb-0.5">Control de Báscula (Quién Pesó)</label>
                  <input v-model="uld.weighedBy" type="text" placeholder="Firma de conformidad de pesaje" class="w-full border border-slate-200 rounded px-2 py-1 text-slate-800 bg-slate-50 focus:outline-none font-bold" />
                </div>
              </div>

              <div class="border-t border-slate-200 pt-3 flex justify-end gap-2 bg-slate-50/50 -mx-4 -mb-4 p-4 rounded-b">
                <button v-if="!uld.sentToLoadPlanning" @click="dispatchToLoadPlanning(uld)"
                  class="bg-emerald-600 hover:bg-emerald-700 text-white font-mono font-black uppercase text-[10px] tracking-widest px-6 py-2 rounded shadow-md transition-all flex items-center gap-2">
                  🚀 Enviar a Load Planning
                </button>
                <div v-else class="flex items-center gap-1.5 text-emerald-600 font-bold text-[10px] uppercase font-mono tracking-wider bg-emerald-100/80 px-4 py-1.5 rounded border border-emerald-300">
                  ✓ Consolidado Exitosamente en LoadPlanningView
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const uldStats = ref([
  { label: "ULDs en Rampa", value: "14 Activos", sub: "Listos en zona estéril", trend: "Normal", trendClass: "text-slate-600" },
  { label: "Ocupación Media", value: "78.4%", sub: "Eficiencia de cubicaje", trend: "+3.1%", trendClass: "text-emerald-600" },
  { label: "Carga Paletizada", value: "42,150 lbs", sub: "Registrado en Load Sheets", trend: "+8.2%", trendClass: "text-emerald-600" },
  { label: "Borradores Abiertos", value: "Activos", sub: "Contenedores en llenado", trend: "Balanza", trendClass: "text-amber-600" },
  { label: "Alertas de Sello", value: "0 Errores", sub: "Validación de precintos", trend: "100%", trendClass: "text-emerald-600" }
])

const expandedUldId = ref(null)

const uldsList = ref([
  { 
    id: "AAY-55589", flight: "UPS-0403", route: "SDQ -> MIA", config: "AAY", type: "Contenedor", status: "PALLETIZED", volumePct: 60,
    executionDate: "2026-06-11", sealNumber: "SC-21723741", grossWeight: 5105, tareWeight: 516, 
    filledBy: "M. Manolov", weighedBy: "R. Sandoval", sentToLoadPlanning: true,
    mawbs: [{ id: "406-03904143", pcs: 4, dest: "MIA" }, { id: "406-01748154", pcs: 80, dest: "MIA" }]
  },
  { 
    id: "PMC-96167", flight: "UPS-0335", route: "SDQ -> MIA", config: "AAZ", type: "Pallet", status: "PALLETIZED", volumePct: 100,
    executionDate: "2026-06-10", sealNumber: "SC-99081234", grossWeight: 1230, tareWeight: 270, 
    filledBy: "A. De la Cruz", weighedBy: "T. Rosario", sentToLoadPlanning: true,
    mawbs: [{ id: "406-05387701", pcs: 4, dest: "MIA" }]
  },
  { 
    id: "PMC-96139", flight: "UPS-0335", route: "SDQ -> MIA", config: "AAZ", type: "Pallet", status: "RECEIVING", volumePct: 45,
    executionDate: "2026-06-15", sealNumber: "", grossWeight: 1430, tareWeight: 270, 
    filledBy: "", weighedBy: "", sentToLoadPlanning: false, // <<--- Sombreado activo por estar llenándose
    mawbs: [{ id: "406-05387705", pcs: 4, dest: "MIA" }]
  }
])

// ─── ACCIÓN: CREAR REGISTRO NUEVO VACÍO ───
function createNewBlankUld() {
  const newId = `ULD-NEW-${Math.floor(1000 + Math.random() * 9000)}`
  const today = new Date().toISOString().split('T')[0]
  
  uldsList.value.unshift({
    id: '',
    flight: '',
    route: 'SDQ -> ',
    config: '',
    type: 'Contenedor',
    status: 'RECEIVING',
    volumePct: 0,
    executionDate: today,
    sealNumber: '',
    grossWeight: 0,
    tareWeight: 140,
    filledBy: '',
    weighedBy: '',
    sentToLoadPlanning: false, // Entra por defecto como borrador / llenándose
    mawbs: []
  })
  
  // Expandir automáticamente para rellenar los datos
  expandedUldId.value = ''
}

// ─── ACCIÓN INTERNA DEL FORMULARIO: DESPACHAR AL LOAD PLANNING MASTER ───
function dispatchToLoadPlanning(uld) {
  if (!uld.id) {
    alert("Código ULD Requerido antes de despachar a Load Planning.")
    return
  }
  uld.sentToLoadPlanning = true
  uld.status = 'PALLETIZED'
  alert(`Éxito: ${uld.id} consolidado e inyectado en LoadPlanningView.vue de forma persistente.`)
}

function toggleUldExpansion(id) {
  expandedUldId.value = expandedUldId.value === id ? null : id
}

function addMawbRow(uld) {
  uld.mawbs.push({ id: '', pcs: 0, dest: uld.route.split('->')[1]?.trim() || '' })
}

function removeMawbRow(uld, index) {
  uld.mawbs.splice(index, 1)
}

function getProgressWidth(status) {
  if (status === 'RECEIVING') return '0%'
  if (status === 'PALLETIZED') return '50%'
  if (status === 'DEPARTED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'PALLETIZED') return 'bg-emerald-500'
  if (status === 'DEPARTED') return 'bg-slate-950'
  return 'bg-slate-200'
}

function getStatusDotClass(currentStatus, step) {
  if (currentStatus === step) {
    if (step === 'RECEIVING') return 'bg-amber-500 border-amber-600 scale-125'
    if (step === 'PALLETIZED') return 'bg-emerald-500 border-emerald-600 scale-125'
    if (step === 'DEPARTED') return 'bg-slate-950 border-slate-950 scale-125'
  }
  const order = ['RECEIVING', 'PALLETIZED', 'DEPARTED']
  if (order.indexOf(currentStatus) >= order.indexOf(step)) {
    return 'bg-slate-400 border-slate-500'
  }
  return 'bg-slate-200 border-slate-300'
}
</script>

<style scoped>
/* Scroll Interno Invisible puro */
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }

input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* ─── ENTORNO TEXTURA TIZA / TRAMADOS GRÁFICOS ─── */
.shadow-pencil-marine {
  box-shadow: 
    0px 1px 2px rgba(15, 32, 67, 0.08),
    1px 3px 6px rgba(15, 32, 67, 0.06),
    3px 6px 12px rgba(15, 32, 67, 0.04);
}

.chalk-sketch {
  position: relative;
  overflow: hidden;
}
.chalk-sketch::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.2;
  pointer-events: none;
  background-image: 
    repeating-linear-gradient(30deg, rgba(71, 85, 105, 0.06) 0px, rgba(71, 85, 105, 0.06) 0.8px, transparent 0.8px, transparent 4px),
    repeating-linear-gradient(-30deg, rgba(71, 85, 105, 0.03) 0px, rgba(71, 85, 105, 0.03) 0.8px, transparent 0.8px, transparent 4px);
}

/* ─── TRAMADO INDUSTRIAL DE ADVERTENCIA PARA CONTENEDOR EN PROCESO DE LLENADO ─── */
.bg-filling-pattern {
  background-image: linear-gradient(45deg, rgba(245, 158, 11, 0.02) 25%, transparent 25%, transparent 50%, rgba(245, 158, 11, 0.02) 50%, rgba(245, 158, 11, 0.02) 75%, transparent 75%, transparent);
  background-size: 16px 16px;
  background-color: rgba(251, 191, 36, 0.03);
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
  background-color: rgba(243, 247, 254, 0.75);
  /* Acento de Grafito Azul Marino Industrial al 90% estricto de opacidad */
  box-shadow: 
    inset 4px 0 0 0 rgba(15, 23, 42, 0.90), 
    0px 2px 6px rgba(15, 32, 67, 0.04);
}

.row-pencil:hover::before {
  opacity: 1;
}
</style>