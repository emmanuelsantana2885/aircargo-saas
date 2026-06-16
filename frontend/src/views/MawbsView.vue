<template>
  <div class="p-8 bg-white min-h-screen text-slate-900 font-sans antialiased">
    
    <header class="flex justify-between items-baseline mb-12 border-b border-slate-100 pb-6">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight text-slate-900">Master Air Waybills (MAWBs)</h1>
        <p class="text-xs text-slate-400 mt-1 font-mono uppercase tracking-wider">Manifiesto de Consolidación</p>
      </div>
      <button class="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-lg font-medium transition active:scale-95 shadow-sm bg-slate-900 text-white hover:bg-slate-800">
        + Nuevo MAWB
      </button>
    </header>

    <section class="border border-slate-100 rounded-xl overflow-hidden shadow-sm bg-white">
      <div class="overflow-x-auto">
        <div class="min-w-[800px] w-full">
          
          <div class="bg-slate-50/70 border-b border-slate-100 text-xs font-semibold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-3.5 px-6">
            <div class="col-span-3 text-left">MAWB Number</div>
            <div class="col-span-2 text-left">Vuelo Asignado</div>
            <div class="col-span-2 text-left">Destino</div>
            <div class="col-span-1 text-center">Piezas</div>
            <div class="col-span-2 text-right">Peso Brutal</div>
            <div class="col-span-2 text-center">Estado</div>
          </div>
          
          <div class="divide-y divide-slate-100 text-sm text-slate-700">
            <div v-for="(mawb, i) in mawbs" :key="i" 
              class="row-sketch grid grid-cols-12 items-center py-4 px-6 transition-all duration-200 cursor-pointer
                     hover:shadow-[inset_4px_0_0_0_#10b981,0_4px_12px_rgba(16,185,129,0.12)]">
              
              <div class="col-span-3 font-mono font-bold text-slate-950 relative z-10 truncate">
                {{ mawb.number }}
              </div>
              
              <div class="col-span-2 font-mono font-semibold text-xs text-slate-800 relative z-10">
                {{ mawb.flight }}
              </div>
              
              <div class="col-span-2 font-mono text-xs text-slate-500 relative z-10">
                {{ mawb.destination }}
              </div>
              
              <div class="col-span-1 text-center font-mono text-xs font-medium text-slate-900 relative z-10">
                {{ mawb.pieces }}
              </div>
              
              <div class="col-span-2 text-right font-mono text-xs font-semibold text-slate-950 relative z-10">
                {{ mawb.weight }} <span class="text-[10px] text-slate-400 font-normal">kg</span>
              </div>
              
              <div class="col-span-2 text-center relative z-10">
                <span :class="getStatusClass(mawb.status)" class="inline-block text-[10px] font-mono px-2.5 py-0.5 rounded-md font-medium uppercase tracking-wide border">
                  {{ mawb.status }}
                </span>
              </div>

            </div>
          </div>

        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
const mawbs = [
  { number: "176-12345678", flight: "AA-335", destination: "MIA", pieces: 45, weight: "1240", status: "MANIFESTED" },
  { number: "176-87654321", flight: "AA-335", destination: "MIA", pieces: 28, weight: "890", status: "RECEIVED" },
  { number: "176-11223344", flight: "DL-712", destination: "JFK", pieces: 19, weight: "670", status: "BOOKED" },
]

function getStatusClass(status) {
  if (status === 'MANIFESTED') return 'bg-emerald-50/40 text-emerald-600 border-emerald-100'
  if (status === 'RECEIVED') return 'bg-blue-50/40 text-blue-600 border-blue-100'
  if (status === 'BOOKED') return 'bg-amber-50/40 text-amber-600 border-amber-100'
  return 'bg-slate-50 text-slate-400 border-slate-200'
}
</script>

<style scoped>
.row-sketch {
  position: relative;
}
.row-sketch::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.15s ease-in-out;
  pointer-events: none;
  background-image: 
    repeating-linear-gradient(60deg, rgba(16, 185, 129, 0.12) 0px, rgba(16, 185, 129, 0.12) 1px, transparent 1px, transparent 2px),
    repeating-linear-gradient(-60deg, rgba(16, 185, 129, 0.10) 0px, rgba(16, 185, 129, 0.10) 1px, transparent 1px, transparent 2px);
}
.row-sketch:hover::before { opacity: 1; }
</style>