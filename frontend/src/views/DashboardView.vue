<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Operations Dashboard</h1>
        <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Hub // Global Analytics Control</p>
      </div>
      <div class="flex items-center gap-2 text-[10px] font-mono font-bold text-slate-500">
        <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> SYSTEM LIVE
      </div>
    </header>

    <section class="grid grid-cols-2 md:grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="metric in computedMetrics" :key="metric.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 border-l-slate-950/90 shadow-pencil-marine transition-all flex flex-col justify-between min-h-[68px] cursor-pointer">
        <div class="relative z-10">
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ metric.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">{{ metric.value }}</div>
        </div>
        <div class="text-[8px] font-mono text-slate-400 relative z-10 truncate flex justify-between items-center">
          <span>{{ metric.sub }}</span>
          <span :class="metric.trendClass" class="font-bold">{{ metric.trend }}</span>
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Vuelo</div>
        <div class="col-span-2 text-left">Ruta</div>
        <div class="col-span-1 text-center">Estado</div>
        <div class="col-span-1 text-center">Fecha</div>
        <div class="col-span-1 text-center">Bookings</div>
        <div class="col-span-1 text-center">MAWBs</div>
        <div class="col-span-1 text-center">ULDs</div>
        <div class="col-span-3 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Flujograma Operativo</div>
      </div>

      <div v-if="appStore.loading && !appStore.flights.length" class="flex-1 flex items-center justify-center">
        <span class="text-[10px] font-mono text-slate-400 animate-pulse">Cargando datos...</span>
      </div>

      <div v-else-if="appStore.flights.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Crea un vuelo desde FlightsView</p>
      </div>

      <div v-else class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="f in appStore.flights" :key="f.id"
          class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer"
          @click="selectFlight(f)">
          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10">
            UPS-{{ f.flightNumber }}
          </div>
          <div class="col-span-2 font-semibold text-slate-700 relative z-10">
            {{ f.origin }} <span class="text-slate-300 mx-1">→</span> {{ f.destination }}
          </div>
          <div class="col-span-1 text-center relative z-10">
            <span :class="getStatusDot(f.status)" class="inline-block w-1.5 h-1.5 rounded-full mr-1"></span>
            <span class="font-mono text-[10px] text-slate-600">{{ f.status }}</span>
          </div>
          <div class="col-span-1 text-center font-mono text-[10px] text-slate-600 relative z-10">{{ f.flightDate }}</div>
          <div class="col-span-1 text-center font-mono font-black text-slate-950 relative z-10">{{ flightBookings(f.id).length }}</div>
          <div class="col-span-1 text-center font-mono font-black text-slate-950 relative z-10">{{ flightMawbs(f.id).length }}</div>
          <div class="col-span-1 text-center font-mono font-black text-slate-950 relative z-10">{{ flightUlds(f.id).length }}</div>
          <div class="col-span-3 px-2 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
            <div class="flex items-center w-full justify-between relative px-2">
              <div class="absolute top-[5px] left-3 right-3 h-[3px] bg-slate-100 z-0 rounded-full flex">
                <div class="h-full rounded-full transition-all duration-300" :class="getLineColor(f.status)" :style="{ width: getProgressWidth(f.status) }"></div>
              </div>
              <div v-for="s in ['SCHEDULED','BOARDING','DEPARTED','ARRIVED']" :key="s" class="flex flex-col items-center z-10 relative">
                <span class="h-2 w-2 rounded-full border-2 transition-all duration-300" :class="getDotClass(f.status, s)"></span>
                <span class="text-[7px] font-mono mt-1 font-black" :class="f.status === s ? 'text-slate-950' : 'text-slate-300'">{{ s.slice(0, 4) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'

const appStore = useAppStore()
const router = useRouter()

const computedMetrics = computed(() => {
  const f = appStore.flights
  const b = appStore.bookings
  const m = appStore.mawbs
  return [
    { label: "Vuelos Activos", value: f.length, sub: "Registrados en sistema", trend: "", trendClass: "" },
    { label: "Bookings", value: b.length, sub: "Reservas de carga", trend: b.filter(x => !x.isConfirmed).length + " pendientes", trendClass: "text-amber-600" },
    { label: "MAWBs", value: m.length, sub: "Guias aéreas", trend: m.filter(x => x.status === 'MANIFESTED').length + " manifestados", trendClass: "text-emerald-600" },
    { label: "ULDs", value: appStore.ulds.length, sub: "Dispositivos de carga", trend: appStore.ulds.filter(u => u.status === 'LOADED').length + " cargados", trendClass: "text-slate-600" },
    { label: "Peso Total (kg)", value: m.reduce((s, x) => s + (x.reportedWeightKg ? Number(x.reportedWeightKg) : 0), 0).toLocaleString(), sub: "Suma MAWBs", trend: "", trendClass: "" },
  ]
})

function flightBookings(flightId) {
  return appStore.bookings.filter(b => b.flightId === flightId)
}

function flightMawbs(flightId) {
  return appStore.mawbs.filter(m => m.flight?.id === flightId || m.flightId === flightId)
}

function flightUlds(flightId) {
  return appStore.ulds.filter(u => u.flightId === flightId)
}

function selectFlight(f) {
  appStore.selectFlight(f.id)
  router.push('/bookings')
}

function getStatusDot(status) {
  if (status === 'SCHEDULED') return 'bg-slate-400'
  if (status === 'BOARDING') return 'bg-amber-500'
  if (status === 'DEPARTED') return 'bg-emerald-500'
  if (status === 'ARRIVED') return 'bg-blue-500'
  if (status === 'CANCELLED') return 'bg-rose-500'
  if (status === 'DELAYED') return 'bg-orange-500'
  return 'bg-slate-200'
}

function getProgressWidth(status) {
  if (status === 'SCHEDULED') return '0%'
  if (status === 'BOARDING') return '33%'
  if (status === 'DEPARTED') return '66%'
  if (status === 'ARRIVED') return '100%'
  return '0%'
}

function getLineColor(status) {
  if (status === 'BOARDING') return 'bg-amber-500'
  if (status === 'DEPARTED') return 'bg-emerald-500'
  if (status === 'ARRIVED') return 'bg-blue-500'
  return 'bg-slate-200'
}

function getDotClass(current, step) {
  if (current === step) {
    if (step === 'SCHEDULED') return 'bg-slate-500 border-slate-600 scale-125'
    if (step === 'BOARDING') return 'bg-amber-500 border-amber-600 scale-125 shadow-[0_0_6px_#f59e0b]'
    if (step === 'DEPARTED') return 'bg-emerald-500 border-emerald-600 scale-125 shadow-[0_0_6px_#10b981]'
    if (step === 'ARRIVED') return 'bg-blue-500 border-blue-600 scale-125 shadow-[0_0_6px_#3b82f6]'
  }
  const order = ['SCHEDULED', 'BOARDING', 'DEPARTED', 'ARRIVED']
  if (order.indexOf(current) >= order.indexOf(step)) return 'bg-slate-400 border-slate-500'
  return 'bg-slate-200 border-slate-300'
}

onMounted(async () => {
  await appStore.loadFlights()
  if (appStore.flights.length) {
    await Promise.all([
      appStore.loadBookings(),
      appStore.loadAllMawbs(),
      appStore.loadUlds(),
    ])
  }
})
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04); }
.chalk-sketch { position: relative; overflow: hidden; transition: transform 0.15s cubic-bezier(0.16,1,0.3,1), box-shadow 0.15s; }
.chalk-sketch::before { content: ""; position: absolute; inset: 0; opacity: 0.2; pointer-events: none; background-image: repeating-linear-gradient(30deg, rgba(71,85,105,0.06) 0px, rgba(71,85,105,0.06) 0.8px, transparent 0.8px, transparent 4px), repeating-linear-gradient(-30deg, rgba(71,85,105,0.03) 0px, rgba(71,85,105,0.03) 0.8px, transparent 0.8px, transparent 4px); }
.chalk-sketch:hover { transform: translate(-0.5px,-0.5px); box-shadow: 0px 1px 2px rgba(15,32,67,0.12), 2px 5px 8px rgba(15,32,67,0.09), 4px 10px 16px rgba(15,32,67,0.06); }
.chalk-sketch:hover::before { opacity: 0.65; }
.row-pencil { position: relative; }
.row-pencil::before { content: ""; position: absolute; inset: 0; opacity: 0; transition: opacity 0.12s; pointer-events: none; background-image: repeating-linear-gradient(45deg, rgba(15,32,67,0.05) 0px, rgba(15,32,67,0.05) 0.6px, transparent 0.6px, transparent 2px); }
.row-pencil:hover { background-color: rgba(243,247,254,0.65); box-shadow: inset 4px 0 0 0 rgba(15,23,42,0.90); }
.row-pencil:hover::before { opacity: 1; }
</style>
