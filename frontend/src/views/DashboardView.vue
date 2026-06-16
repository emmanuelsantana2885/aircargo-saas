<template>
  <!-- Contenedor maestro congelado al 100% del alto de la pantalla sin scroll general -->
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">
    <!-- ─── HEADER PRINCIPAL COMPACTO ─── -->
    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Operations Dashboard</h1>
        <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Hub // Global Analytics Control</p>
      </div>
      <div class="flex items-center gap-2 text-[10px] font-mono font-bold text-slate-500">
        <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> SYSTEM LIVE // REFRESH 30S
      </div>
    </header>

    <!-- ─── CARDS ULTRA-REDUCIDAS CON TEXTURA DE TIZA/LÁPIZ Y ACENTO AZUL MARINO ─── -->
    <section class="grid grid-cols-2 md:grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="metric in metrics" :key="metric.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 border-l-slate-950/90 shadow-pencil-marine transition-all flex flex-col justify-between min-h-[68px] cursor-pointer">
        <div class="relative z-10">
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ metric.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">
            {{ metric.value }}
          </div>
        </div>
        <div class="text-[8px] font-mono text-slate-400 relative z-10 truncate flex justify-between items-center">
          <span>{{ metric.sub }}</span>
          <span :class="metric.trendClass" class="font-bold">{{ metric.trend }}</span>
        </div>
      </div>
    </section>

    <!-- ─── MONITOR TABLEVIEW DE RENDIMIENTO ─── -->
    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
     
      <!-- Cabecera fija de la tabla -->
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Consolidado ULD</div>
        <div class="col-span-1 text-left">Vuelo</div>
        <div class="col-span-2 text-left">Ruta / Destino</div>
        <div class="col-span-1 text-left">Tipo Carga</div>
        <div class="col-span-1 text-center">ULDs</div>
        <div class="col-span-1 text-right pr-2">Peso Total</div>
        <div class="col-span-4 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Flujograma de Estado Operativo</div>
      </div>
     
      <!-- Contenedor de listado scrolleable -->
      <div class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="(row, i) in tableData" :key="i"
          class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer">
         
          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10">
            {{ row.id }}
          </div>
         
          <div class="col-span-1 font-mono font-bold text-[10px] text-slate-800 bg-white border border-slate-200 rounded px-1 py-0.5 w-max relative z-10 shadow-sm">
            {{ row.flight }}
          </div>
         
          <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">
            {{ row.route }}
          </div>
          <div class="col-span-1 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[10px]">
            {{ row.type }}
          </div>
         
          <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
            {{ row.ulds }}
          </div>
         
          <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
            {{ row.weight }}<span class="text-[9px] text-slate-400 font-normal font-mono">k</span>
          </div>
         
          <!-- SECCIÓN DEL FLUJOGRAMA OPERATIVO -->
          <div class="col-span-4 px-3 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
            <div class="flex items-center w-full justify-between relative px-4">
             
              <!-- Línea del Flujo -->
              <div class="absolute top-[5px] left-6 right-6 h-[3px] bg-slate-100 z-0 rounded-full overflow-hidden flex">
                <div class="h-full rounded-full transition-all duration-300"
                     :class="getLineProgressColor(row.status)"
                     :style="{ width: getProgressWidth(row.status) }">
                </div>
              </div>
             
              <!-- Nodos del Flujograma -->
              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="row.status === 'RECEIVING'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-amber-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="row.status === 'RECEIVING' ? 'bg-amber-500 border-amber-600 scale-125 shadow-[0_0_6px_#f59e0b]' : 'bg-slate-400 border-slate-500'"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="row.status === 'RECEIVING' ? 'text-amber-600' : 'text-slate-400'">REC</span>
              </div>
              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="row.status === 'PALLETIZED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-emerald-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="row.status === 'PALLETIZED' ? 'bg-emerald-500 border-emerald-600 scale-125 shadow-[0_0_6px_#10b981]' : (['MANIFESTED','DEPARTED'].includes(row.status) ? 'bg-slate-400 border-slate-500' : 'bg-slate-200 border-slate-300')"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="row.status === 'PALLETIZED' ? 'text-emerald-600' : (['MANIFESTED','DEPARTED'].includes(row.status) ? 'text-slate-500' : 'text-slate-300')">PLT</span>
              </div>
              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="row.status === 'MANIFESTED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-blue-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="row.status === 'MANIFESTED' ? 'bg-blue-500 border-blue-600 scale-125 shadow-[0_0_6px_#3b82f6]' : (['DEPARTED'].includes(row.status) ? 'bg-slate-400 border-slate-500' : 'bg-slate-200 border-slate-300')"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="row.status === 'MANIFESTED' ? 'text-blue-600' : (['DEPARTED'].includes(row.status) ? 'text-slate-500' : 'text-slate-300')">MNST</span>
              </div>
              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="row.status === 'DEPARTED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-slate-800 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="row.status === 'DEPARTED' ? 'bg-slate-950 border-slate-950 scale-125 shadow-[0_0_6px_#0f172a]' : 'bg-slate-200 border-slate-300'"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="row.status === 'DEPARTED' ? 'text-slate-950' : 'text-slate-300'">DEP</span>
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

const metrics = ref([
  { label: "Volumen Recibido", value: "48,250 kg", sub: "Hoy vs Ayer", trend: "+12.4%", trendClass: "text-emerald-600" },
  { label: "ULDs Configurados", value: "34 / 40", sub: "Plan de carga", trend: "85%", trendClass: "text-slate-600" },
  { label: "Manifiestos Emitidos", value: "18 MAWBs", sub: "Listos para salida", trend: "OK", trendClass: "text-emerald-600" },
  { label: "Discrepancia Peso", value: "4 Alertas", sub: "Revisión en báscula", trend: "Crítico", trendClass: "text-rose-600" },
  { label: "Hold de Aduana", value: "2 Guías", sub: "Falta documentación", trend: "-5%", trendClass: "text-amber-600" }
])

const tableData = ref([
  { id: "ULD-SDQ-992", flight: "AA-335", route: "SDQ -> MIA", type: "MED/PHARMA", ulds: 8, weight: "12,450", status: "PALLETIZED" },
  { id: "ULD-SDQ-411", flight: "DL-712", route: "SDQ -> JFK", type: "PERISHABLE", ulds: 5, weight: "8,310", status: "RECEIVING" },
  { id: "ULD-SDQ-104", flight: "UA-154", route: "SDQ -> EWR", type: "TEXTILES", ulds: 12, weight: "22,150", status: "MANIFESTED" },
  { id: "ULD-SDQ-882", flight: "FX-882", route: "SDQ -> MEM", type: "E-COMMERCE", ulds: 6, weight: "9,620", status: "DEPARTED" },
  { id: "ULD-SDQ-305", flight: "AM-221", route: "SDQ -> MEX", type: "GENERAL", ulds: 4, weight: "6,840", status: "PALLETIZED" },
  { id: "ULD-SDQ-016", flight: "UPS-403", route: "SDQ -> SJU", type: "COURIER", ulds: 7, weight: "11,210", status: "RECEIVING" }
])

function getProgressWidth(status) {
  if (status === 'RECEIVING') return '0%'
  if (status === 'PALLETIZED') return '33.33%'
  if (status === 'MANIFESTED') return '66.66%'
  if (status === 'DEPARTED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'PALLETIZED') return 'bg-emerald-500'
  if (status === 'MANIFESTED') return 'bg-blue-500'
  if (status === 'DEPARTED') return 'bg-slate-950'
  return 'bg-slate-200'
}
</script>

<style scoped>
/* ─── ELIMINAR BARRA DE SCROLL ─── */
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }

/* ─── SOMBREADO TÉCNICO ─── */
.shadow-pencil-marine {
  box-shadow:
    0px 1px 2px rgba(15, 32, 67, 0.08),
    1px 3px 6px rgba(15, 32, 67, 0.06),
    3px 6px 12px rgba(15, 32, 67, 0.04);
}

/* ─── TEXTURA TIZA ─── */
.chalk-sketch {
  position: relative;
  overflow: hidden;
  transition: transform 0.15s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.15s cubic-bezier(0.16, 1, 0.3, 1);
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
.chalk-sketch:hover {
  transform: translate(-0.5px, -0.5px);
  box-shadow:
    0px 1px 2px rgba(15, 32, 67, 0.12),
    2px 5px 8px rgba(15, 32, 67, 0.09),
    4px 10px 16px rgba(15, 32, 67, 0.06);
}
.chalk-sketch:hover::before {
  opacity: 0.65;
}

/* ─── FILAS CON EFECTO ─── */
.row-pencil {
  position: relative;
  transition: background-color 0.15s ease, box-shadow 0.15s ease;
}
.row-pencil::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0;
  pointer-events: none;
  background-image:
    repeating-linear-gradient(45deg, rgba(15, 32, 67, 0.05) 0px, rgba(15, 32, 67, 0.05) 0.6px, transparent 0.6px, transparent 2px);
}
.row-pencil:hover {
  background-color: rgba(243, 247, 254, 0.65);
  box-shadow:
    inset 4px 0 0 0 rgba(15, 23, 42, 0.90),
    0px 2px 6px rgba(15, 32, 67, 0.03);
}
.row-pencil:hover::before {
  opacity: 1;
}
</style>
