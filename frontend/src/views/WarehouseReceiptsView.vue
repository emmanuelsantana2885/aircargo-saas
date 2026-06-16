<template>
  <div class="p-8 bg-white min-h-screen text-slate-900 font-sans antialiased">
    
    <header class="flex justify-between items-baseline mb-12 border-b border-slate-100 pb-6">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight text-slate-900">Warehouse Receipts</h1>
        <p class="text-xs text-slate-400 mt-1 font-mono uppercase tracking-wider">Módulos de Recepción de Carga</p>
      </div>
      <button class="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded-lg font-medium transition active:scale-95 shadow-sm bg-slate-900 text-white hover:bg-slate-800">
        + Nuevo Recibo
      </button>
    </header>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-12 mb-16">
      <div v-for="(receipt, i) in receipts" :key="i" 
        class="card-sketch p-4 rounded-xl transition-all duration-300 flex flex-col justify-between min-h-[160px] cursor-pointer">
        
        <div class="relative z-10 flex justify-between items-start">
          <div>
            <div class="font-mono text-xl font-bold text-slate-950 tracking-tight">{{ receipt.mawb }}</div>
            <div class="text-xs text-slate-400 font-sans mt-0.5">{{ receipt.consignee }}</div>
          </div>
          <span :class="getStatusClass(receipt.status)" class="text-[10px] font-mono px-2 py-0.5 rounded-md font-medium uppercase tracking-wide border">
            {{ receipt.status }}
          </span>
        </div>

        <div class="mt-6 pt-4 border-t border-slate-100 grid grid-cols-2 gap-y-4 gap-x-2 text-[11px] font-mono text-slate-400 relative z-10">
          <div>
            <span class="text-slate-400 block">Peso Recibido</span>
            <span class="text-slate-950 font-bold text-base font-mono">{{ receipt.weight }} <span class="text-xs font-normal text-slate-400">kg</span></span>
          </div>
          <div>
            <span class="text-slate-400 block">Piezas Totales</span>
            <span class="text-slate-950 font-bold text-base font-mono">{{ receipt.pieces }}</span>
          </div>
          <div>
            <span class="text-slate-400 block">Estación Origen</span>
            <span class="text-slate-800 font-medium font-sans">{{ receipt.origin }}</span>
          </div>
          <div>
            <span class="text-slate-400 block">Fecha Registro</span>
            <span class="text-slate-800 font-medium">{{ receipt.date }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const receipts = [
  { mawb: "176-12345678", consignee: "Amazon Logistics", weight: "1240", pieces: 45, origin: "SDQ", date: "14 Jun", status: "COMPLETED" },
  { mawb: "176-87654321", consignee: "PharmaCorp SD", weight: "890", pieces: 28, origin: "SDQ", date: "14 Jun", status: "IN PROGRESS" },
]

function getStatusClass(status) {
  if (status === 'COMPLETED') return 'bg-emerald-50/40 text-emerald-600 border-emerald-100'
  if (status === 'IN PROGRESS') return 'bg-amber-50/40 text-amber-600 border-amber-100'
  return 'bg-slate-50 text-slate-500 border-slate-200'
}
</script>

<style scoped>
.card-sketch {
  position: relative;
  overflow: hidden;
}
.card-sketch::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
  pointer-events: none;
  background-image: 
    repeating-linear-gradient(60deg, rgba(16, 185, 129, 0.16) 0px, rgba(16, 185, 129, 0.16) 1px, transparent 1px, transparent 2px),
    repeating-linear-gradient(-60deg, rgba(16, 185, 129, 0.14) 0px, rgba(16, 185, 129, 0.14) 1px, transparent 1px, transparent 2px);
}
.card-sketch:hover::before { opacity: 1; }
</style>