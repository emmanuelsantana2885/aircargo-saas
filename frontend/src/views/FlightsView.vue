<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">
    <!-- Header -->
    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Flights Control</h1>
        <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Hub • Salidas / Llegadas en Curso</p>
      </div>
      <div class="flex items-center gap-2 text-[10px] font-mono font-bold text-slate-500">
        <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> 7 VUELOS ACTIVOS
      </div>
    </header>

    <!-- KPI Compactos -->
    <section class="grid grid-cols-2 md:grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="metric in flightMetrics" :key="metric.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 border-l-slate-950/90 shadow-pencil-marine flex flex-col justify-between min-h-[68px]">
        <div>
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono">{{ metric.label }}</h3>
          <div class="text-xl font-mono font-black text-slate-950 mt-0.5">{{ metric.value }}</div>
        </div>
        <div class="text-[8px] font-mono text-slate-400 flex justify-between">
          <span>{{ metric.sub }}</span>
          <span :class="metric.trendClass">{{ metric.trend }}</span>
        </div>
      </div>
    </section>

    <!-- Tabla Principal -->
    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col">
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2">Vuelo</div>
        <div class="col-span-2">Ruta</div>
        <div class="col-span-1">Aeronave</div>
        <div class="col-span-1 text-center">ETD / ETA</div>
        <div class="col-span-1 text-center">ULD / Peso</div>
        <div class="col-span-1 text-center">Estado</div>
        <div class="col-span-4 text-center">Progreso de Carga</div>
      </div>

      <div class="divide-y divide-slate-200 text-xs overflow-y-auto flex-1 scrollbar-none">
        <div v-for="(flight, i) in flights" :key="i"
          class="row-pencil grid grid-cols-12 items-center py-3 px-5 transition-all duration-150 cursor-pointer">
          
          <div class="col-span-2 font-mono font-black text-slate-950">{{ flight.flightNumber }}</div>
          <div class="col-span-2 text-slate-600 font-semibold">{{ flight.route }}</div>
          <div class="col-span-1 font-mono text-[10px]">{{ flight.aircraft }}</div>
          <div class="col-span-1 text-center font-mono text-[10px]">{{ flight.etd }}</div>
          <div class="col-span-1 text-center font-mono">{{ flight.ulds }} • {{ flight.weight }}k</div>
          
          <div class="col-span-1">
            <span class="inline-block px-3 py-0.5 text-[9px] font-bold rounded-full"
                  :class="getStatusClass(flight.status)">
              {{ flight.status }}
            </span>
          </div>

          <!-- Barra de Progreso -->
          <div class="col-span-4 px-4">
            <div class="h-2 bg-slate-100 rounded-full overflow-hidden">
              <div class="h-2 rounded-full transition-all"
                   :class="getProgressColor(flight.progress)"
                   :style="{ width: flight.progress + '%' }"></div>
            </div>
            <div class="text-[9px] font-mono text-right mt-1 text-slate-400">{{ flight.progress }}% CARGADO</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const flightMetrics = ref([
  { label: "Vuelos Hoy", value: "14", sub: "7 activos", trend: "+2", trendClass: "text-emerald-600" },
  { label: "En Tiempo", value: "92%", sub: "On Time", trend: "↑", trendClass: "text-emerald-600" },
  { label: "ULD Pendientes", value: "23", sub: "Por cargar", trend: "4", trendClass: "text-amber-600" },
  { label: "Peso Total", value: "187t", sub: "Movido hoy", trend: "+9%", trendClass: "text-emerald-600" },
  { label: "Delay Promedio", value: "11 min", sub: "Últimas 24h", trend: "-3", trendClass: "text-emerald-600" }
])

const flights = ref([
  { flightNumber: "AA-335", route: "SDQ → MIA", aircraft: "B767", etd: "22:45", ulds: 19, weight: "124", status: "LOADING", progress: 86 },
  { flightNumber: "DL-712", route: "SDQ → JFK", aircraft: "A321", etd: "23:10", ulds: 12, weight: "68", status: "READY", progress: 100 },
  { flightNumber: "UA-154", route: "SDQ → EWR", aircraft: "B757", etd: "01:20", ulds: 8, weight: "45", status: "LOADING", progress: 45 },
  { flightNumber: "FX-882", route: "SDQ → MEM", aircraft: "A300", etd: "02:05", ulds: 22, weight: "98", status: "TAXI", progress: 100 }
])

function getStatusClass(status) {
  if (status === 'READY') return 'bg-emerald-100 text-emerald-700'
  if (status === 'LOADING') return 'bg-amber-100 text-amber-700'
  if (status === 'TAXI') return 'bg-slate-800 text-white'
  return 'bg-slate-100 text-slate-600'
}

function getProgressColor(progress) {
  if (progress > 85) return 'bg-emerald-500'
  if (progress > 40) return 'bg-amber-500'
  return 'bg-blue-500'
}
</script>

<style scoped>
/* Reutilizamos los mismos estilos del Dashboard */
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }

.shadow-pencil-marine {
  box-shadow: 0 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04);
}

.chalk-sketch {
  position: relative;
  overflow: hidden;
  transition: transform 0.15s cubic-bezier(0.16,1,0.3,1), box-shadow 0.15s;
}
.chalk-sketch:hover {
  transform: translate(-0.5px, -0.5px);
}

.row-pencil {
  position: relative;
  transition: all 0.15s ease;
}
.row-pencil:hover {
  background-color: rgba(243, 247, 254, 0.65);
  box-shadow: inset 4px 0 0 rgba(15,23,42,0.9);
}
</style>
