cat > /home/manolov/Projects/aircargo-saas/frontend/src/views/UldsView.vue << 'EOF'
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
              expandedUldId === uld.id ? 'row-selected' : '',
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

          <!-- ====================== FORMULARIO EXPANDIDO ====================== -->
          <div v-show="expandedUldId === uld.id" class="bg-slate-50 border-b border-slate-200 px-12 py-4 transition-all duration-300 overflow-hidden">
            <div class="bg-white border border-slate-300 rounded shadow-sm max-w-5xl p-6 font-mono text-sm relative">
             
              <div v-if="!uld.sentToLoadPlanning" class="mb-4 p-3 bg-amber-50 border border-amber-200 rounded text-amber-800 text-sm flex justify-between items-center">
                <span>⚠️ <strong>CONTRALORÍA DE RAMPA:</strong> Este contenedor se está editando en tiempo real en pista. No está reflejado en el plan maestro de vuelo.</span>
                <span class="font-black tracking-widest uppercase text-[9px] bg-amber-200 px-1.5 py-0.5 rounded">MODO BORRADOR ABIERTO</span>
              </div>

              <div class="flex justify-between items-center border-b border-slate-300 pb-3 mb-5">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-black text-slate-950 uppercase tracking-wider">ULD PALLET SHEET & MANIFEST</span>
                  <span class="text-[10px] text-slate-400 font-bold">// CONTROL ADICIÓN AUTOMÁTICA</span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="text-sm font-bold text-slate-400 uppercase">Volumen Ocupado:</span>
                  <input v-model.number="uld.volumePct" type="number" min="0" max="100" class="w-14 text-center bg-slate-100 border border-slate-300 rounded font-bold text-slate-950 focus:outline-none text-sm" />
                  <span class="text-sm font-bold text-slate-600">%</span>
                </div>
              </div>

              <!-- Campos superiores -->
              <div class="grid grid-cols-4 gap-5 mb-6">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Código Identificador</label>
                  <input v-model="uld.id" type="text" placeholder="PMC-XXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm font-bold text-slate-950 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Vuelo Destino</label>
                  <input v-model="uld.flight" type="text" placeholder="UPS-XXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm font-bold text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Fecha Ejecución</label>
                  <input v-model="uld.executionDate" type="date" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Sello de Seguridad No.</label>
                  <input v-model="uld.sealNumber" type="text" placeholder="SC-XXXXXXXX" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 font-bold" />
                </div>
              </div>

              <div class="grid grid-cols-4 gap-5 mb-6">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Configuración Contorno</label>
                  <input v-model="uld.config" type="text" placeholder="AAZ / AAY" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Tipo de Estructura</label>
                  <select v-model="uld.type" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400">
                    <option value="Contenedor Metálico">Contenedor Metálico</option>
                    <option value="Pallet / Red H6">Pallet / Red H6</option>
                  </select>
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Ruta Conexión</label>
                  <input v-model="uld.route" type="text" placeholder="SDQ -> MIA" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 uppercase" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Estado Operación</label>
                  <select v-model="uld.status" class="w-full bg-slate-50 border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 focus:outline-none focus:border-slate-400 font-bold">
                    <option value="RECEIVING">RECEIVING (Abierto)</option>
                    <option value="PALLETIZED">PALLETIZED (Armado)</option>
                    <option value="DEPARTED">DEPARTED (Despachado)</option>
                  </select>
                </div>
              </div>

              <!-- TABLA MAWB - TAMAÑO UNIFORME -->
              <div class="border border-slate-200 rounded overflow-hidden mb-6">
                <div class="bg-slate-950 text-white text-sm font-bold uppercase grid grid-cols-12 py-3 px-5 tracking-wide items-center">
                  <div class="col-span-4">NÚMERO DE GUÍA COMERCIAL (MAWB)</div>
                  <div class="col-span-3">DESCRIPCIÓN</div>
                  <div class="col-span-2 text-right">PIEZAS (PCS)</div>
                  <div class="col-span-1 text-center">%</div>
                  <div class="col-span-1 text-right">DESTINO</div>
                  <div class="col-span-1"></div>
                </div>
               
                <div class="divide-y divide-slate-100 max-h-[200px] overflow-y-auto scrollbar-none">
                  <div v-for="(mawb, mIdx) in uld.mawbs" :key="mIdx" class="grid grid-cols-12 items-center py-3 px-5 bg-white gap-4 text-sm">
                    <div class="col-span-4">
                      <input v-model="mawb.id" type="text" placeholder="406-XXXXXXXX"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1.5 bg-transparent font-bold text-slate-800" />
                    </div>
                    <div class="col-span-3">
                      <input v-model="mawb.description" type="text" placeholder="Dry Cargo / General Cargo"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1.5 bg-transparent font-medium text-slate-700" />
                    </div>
                    <div class="col-span-2">
                      <input v-model.number="mawb.pcs" type="number"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1.5 text-right bg-transparent font-bold" />
                    </div>
                    <div class="col-span-1 flex items-center justify-center gap-1">
                      <input v-model.number="mawb.percentage" type="number" min="0" max="100"
                        class="w-14 border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1.5 text-center bg-transparent font-bold text-rose-600" />
                      <span class="text-sm text-slate-400">%</span>
                    </div>
                    <div class="col-span-1">
                      <input v-model="mawb.dest" type="text"
                        class="w-full border-b border-slate-200 focus:outline-none focus:border-slate-950 py-1.5 text-right bg-transparent uppercase font-bold text-slate-600" />
                    </div>
                    <div class="col-span-1 text-center">
                      <button @click="removeMawbRow(uld, mIdx)"
                        class="text-rose-500 hover:text-rose-700 font-sans text-xl leading-none">✕</button>
                    </div>
                  </div>
                </div>
               
                <div class="p-3 bg-slate-50 border-t border-slate-100">
                  <button @click="addMawbRow(uld)"
                    class="w-full py-2.5 border border-dashed border-slate-300 rounded text-center text-slate-500 hover:text-slate-950 transition-colors font-black text-sm uppercase">
                    + VINCULAR REGISTRO DE GUÍA / MAWB
                  </button>
                </div>
              </div>

              <!-- Sección de Pesos -->
              <div class="grid grid-cols-3 border border-slate-200 rounded overflow-hidden bg-slate-50 text-center divide-x divide-slate-200 mb-6">
                <div class="p-4">
                  <span class="block text-[9px] font-black text-slate-400 uppercase">Peso Bruto Verificado (LBS)</span>
                  <input v-model.number="uld.grossWeight" type="number" class="w-28 text-center bg-transparent border-b border-slate-300 font-mono font-black text-slate-950 mt-2 focus:outline-none text-sm" />
                </div>
                <div class="p-4">
                  <span class="block text-[9px] font-black text-slate-400 uppercase">Tara del Contenedor (LBS)</span>
                  <input v-model.number="uld.tareWeight" type="number" class="w-28 text-center bg-transparent border-b border-slate-300 font-mono font-black text-slate-950 mt-2 focus:outline-none text-sm" />
                </div>
                <div class="p-4 bg-emerald-50/50 flex flex-col justify-center">
                  <span class="block text-[9px] font-black text-emerald-700 uppercase tracking-wider">Cálculo de Peso Neto</span>
                  <span class="block text-sm font-black text-emerald-600 mt-1">{{ ((uld.grossWeight || 0) - (uld.tareWeight || 0)).toLocaleString() }} lbs</span>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4 border-t border-slate-200 pt-5">
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Manifestado y Llenado Por (Crew)</label>
                  <input v-model="uld.filledBy" type="text" placeholder="Operadores de rampa asignados" class="w-full border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 bg-slate-50 focus:outline-none font-bold" />
                </div>
                <div>
                  <label class="block text-[9px] font-black text-slate-400 uppercase mb-1">Control de Báscula (Quién Pesó)</label>
                  <input v-model="uld.weighedBy" type="text" placeholder="Firma de conformidad de pesaje" class="w-full border border-slate-200 rounded px-3 py-2 text-sm text-slate-800 bg-slate-50 focus:outline-none font-bold" />
                </div>
              </div>

              <div class="border-t border-slate-200 pt-5 flex justify-end gap-2 bg-slate-50/50 -mx-6 -mb-6 p-6 rounded-b">
                <button v-if="!uld.sentToLoadPlanning" @click="dispatchToLoadPlanning(uld)"
                  class="bg-emerald-600 hover:bg-emerald-700 text-white font-mono font-black uppercase text-sm tracking-widest px-6 py-2.5 rounded shadow-md transition-all flex items-center gap-2">
                  🚀 Enviar a Load Planning
                </button>
                <div v-else class="flex items-center gap-1.5 text-emerald-600 font-bold text-sm uppercase font-mono tracking-wider bg-emerald-100/80 px-4 py-2 rounded border border-emerald-300">
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
/* ... (el script se mantiene igual) ... */
const uldStats = ref([ /* ... */ ])
const expandedUldId = ref(null)
const uldsList = ref([ /* ... */ ])
// Todas las funciones permanecen iguales
</script>

<style scoped>
/* ... estilos anteriores ... */
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
</style>
EOF