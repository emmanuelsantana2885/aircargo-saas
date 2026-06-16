<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col justify-between text-slate-900 font-sans antialiased overflow-hidden select-none">

    <header class="flex justify-between items-center border-b border-slate-200 pb-3 shrink-0">
      <div>
        <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Bookings Hub</h1>
        <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Control Desk // Marine Custom</p>
      </div>
      <button class="flex items-center gap-1.5 text-[10px] px-2.5 py-1.5 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine bg-slate-950 text-white hover:bg-slate-800">
        <span class="text-xs font-semibold leading-none">+</span> New Booking
      </button>
    </header>

    <section class="grid grid-cols-1 md:grid-cols-4 gap-4 my-4 shrink-0">
      <div v-for="stat in stats" :key="stat.label"
        :class="stat.borderClass"
        class="pencil-sketch py-2 px-3.5 rounded bg-white border border-slate-200 shadow-pencil-marine transition-all flex flex-col justify-between min-h-[78px] cursor-pointer">
        <div class="relative z-10">
          <h3 class="text-[9px] font-black text-slate-400 uppercase tracking-wider font-mono">{{ stat.label }}</h3>
          <div class="flex items-baseline gap-1 mt-0.5">
            <div class="text-2xl font-mono font-black tracking-tight text-slate-950">{{ stat.value }}</div>
          </div>
        </div>
        <div class="pt-1 border-t border-slate-150 text-[9px] font-mono text-slate-400 relative z-10 truncate">
          {{ stat.sub }}
        </div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2 text-left">Booking ID</div>
        <div class="col-span-1 text-left">Vuelo</div>
        <div class="col-span-2 text-left">Agente / Broker</div>
        <div class="col-span-1 text-left">Shipper</div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-1 text-right pr-2">Peso</div>
        <div class="col-span-4 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Flujograma de Estado Intermitente (Holgado)</div>
      </div>
      
      <div class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="(booking, i) in bookings" :key="i" 
          class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer">
          
          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10">
            {{ booking.id }}
          </div>
          
          <div class="col-span-1 font-mono font-bold text-[10px] text-slate-800 bg-white border border-slate-200 rounded px-1 py-0.5 w-max relative z-10 shadow-sm">
            {{ booking.flight }}
          </div>
          
          <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">
            {{ booking.agent }}
          </div>

          <div class="col-span-1 text-slate-900 font-bold relative z-10 truncate pr-2 font-mono text-[10px]">
            {{ booking.shipper }}
          </div>
          
          <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">
            {{ booking.pieces }}
          </div>
          
          <div class="col-span-1 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">
            {{ booking.weight }}<span class="text-[9px] text-slate-400 font-normal font-mono">k</span>
          </div>
          
          <div class="col-span-4 px-3 relative z-10 flex items-center justify-between h-full select-none border-l border-slate-100">
            <div class="flex items-center w-full justify-between relative px-4">
              
              <div class="absolute top-[5px] left-6 right-6 h-[3px] bg-slate-100 z-0 rounded-full overflow-hidden flex">
                <div class="h-full rounded-full transition-all duration-300"
                     :class="getLineProgressColor(booking.status)"
                     :style="{ width: getProgressWidth(booking.status) }">
                </div>
              </div>
              
              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="booking.status === 'PENDING'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-amber-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="booking.status === 'PENDING' ? 'bg-amber-500 border-amber-600 scale-125 shadow-[0_0_6px_#f59e0b]' : 'bg-slate-400 border-slate-500'"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="booking.status === 'PENDING' ? 'text-amber-600' : 'text-slate-400'">BKD</span>
              </div>

              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="booking.status === 'CONFIRMED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-emerald-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="booking.status === 'CONFIRMED' ? 'bg-emerald-500 border-emerald-600 scale-125 shadow-[0_0_6px_#10b981]' : (['ARRIVED','MANIFESTED'].includes(booking.status) ? 'bg-slate-400 border-slate-500' : 'bg-slate-200 border-slate-300')"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="booking.status === 'CONFIRMED' ? 'text-emerald-600' : (['ARRIVED','MANIFESTED'].includes(booking.status) ? 'text-slate-500' : 'text-slate-300')">CNF</span>
              </div>

              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="booking.status === 'ARRIVED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-blue-400 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="booking.status === 'ARRIVED' ? 'bg-blue-500 border-blue-600 scale-125 shadow-[0_0_6px_#3b82f6]' : (['MANIFESTED'].includes(booking.status) ? 'bg-slate-400 border-slate-500' : 'bg-slate-200 border-slate-300')"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="booking.status === 'ARRIVED' ? 'text-blue-600' : (['MANIFESTED'].includes(booking.status) ? 'text-slate-500' : 'text-slate-300')">ARRV</span>
              </div>

              <div class="flex flex-col items-center z-10 relative">
                <div class="relative flex items-center justify-center">
                  <span v-if="booking.status === 'MANIFESTED'" class="animate-ping absolute inline-flex h-3.5 w-3.5 rounded-full bg-slate-800 opacity-75"></span>
                  <span class="h-2 w-2 rounded-full border-2 transition-all duration-300"
                        :class="booking.status === 'MANIFESTED' ? 'bg-slate-950 border-slate-950 scale-125 shadow-[0_0_6px_#0f172a]' : 'bg-slate-200 border-slate-300'"></span>
                </div>
                <span class="text-[9px] font-mono mt-1 tracking-tight font-black uppercase" :class="booking.status === 'MANIFESTED' ? 'text-slate-950' : 'text-slate-300'">MNST</span>
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

const stats = ref([
  { label: "Total Reservas", value: "148", sub: "Activas en sistema", borderClass: "border-l-slate-700" },
  { label: "En Espera", value: "12", sub: "Asignación de espacio", borderClass: "border-l-amber-500" },
  { label: "Confirmadas", value: "114", sub: "Listo para recepción", borderClass: "border-l-emerald-500" },
  { label: "Bloqueadas", value: "22", sub: "Falta documentación", borderClass: "border-l-rose-500" }
])

const bookings = ref([
  { id: "BKG-2026-001", flight: "AA-335", agent: "DHL Global Forwarding", shipper: "MEDTRONIC DR", pieces: 12, weight: "450", status: "CONFIRMED" },
  { id: "BKG-2026-002", flight: "DL-712", agent: "FedEx Air Cargo", shipper: "FRESENIUS KABI", pieces: 8, weight: "310", status: "PENDING" },
  { id: "BKG-2026-003", flight: "UA-154", agent: "Expeditors Intl", shipper: "HANESBRANDS", pieces: 24, weight: "1,150", status: "ARRIVED" },
  { id: "BKG-2026-004", flight: "AA-335", agent: "Kuehne + Nagel", shipper: "EATON IND", pieces: 15, weight: "620", status: "MANIFESTED" },
  { id: "BKG-2026-005", flight: "FX-882", agent: "Panalpina Cargo", shipper: "BARD HEMOCUE", pieces: 19, weight: "840", status: "CONFIRMED" },
  { id: "BKG-2026-006", flight: "DL-712", agent: "CH Robinson Worldwide", shipper: "CONATEK SR", pieces: 5, weight: "210", status: "PENDING" }
])

function getProgressWidth(status) {
  if (status === 'PENDING') return '0%'
  if (status === 'CONFIRMED') return '33.33%'
  if (status === 'ARRIVED') return '66.66%'
  if (status === 'MANIFESTED') return '100%'
  return '0%'
}

function getLineProgressColor(status) {
  if (status === 'CONFIRMED') return 'bg-emerald-500'
  if (status === 'ARRIVED') return 'bg-blue-500'
  if (status === 'MANIFESTED') return 'bg-slate-950'
  return 'bg-slate-200'
}
</script>

<style scoped>
.border-slate-150 { border-color: rgba(226, 232, 240, 0.6); }

/* ─── ELIMINAR BARRA DE SCROLL COMPLETAMENTE ─── */
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }

/* ─── SOMBREADO CON MATICES AZUL MARINO TÉCNICO (GRAFITO INDUSTRIAL) ─── */
.shadow-pencil-marine {
  box-shadow: 
    0px 1px 2px rgba(15, 32, 67, 0.08),
    1px 3px 6px rgba(15, 32, 67, 0.06),
    3px 6px 12px rgba(15, 32, 67, 0.04);
}

/* ─── TEXTURA COMPACTA DE PLANO: RAYADO DE LÁPIZ PORTAMINAS ─── */
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

/* ─── TRAMADO DE RAYADO PARA FILAS DE LA TABLA ─── */
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
  background-color: rgba(243, 247, 254, 0.6); /* Destello sutil azulado en hover */
  box-shadow: 
    inset 3px 0 0 0 #0f172a,
    0px 2px 6px rgba(15, 32, 67, 0.03);
}

.row-pencil:hover::before {
  opacity: 1;
}
</style>