<template>
  <div class="p-4 bg-slate-100 min-h-screen font-mono flex items-center justify-center select-none">
    <div class="p-6 bg-white border border-slate-300 rounded-lg shadow-pencil-marine max-w-xl w-full">
      <h3 class="text-sm font-black text-slate-900 uppercase tracking-wider mb-4 border-b pb-2">
        Manifiesto de Ingreso // Registro de ULD
      </h3>
      
      <form @submit.prevent="submitToLoadPlan" class="space-y-3 text-[11px]">
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block font-black text-slate-500 uppercase mb-1">Prefijo ULD</label>
            <select v-model="form.prefix" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-bold text-slate-950 focus:outline-none">
              <option value="PMC">PMC (Pallet Grande)</option>
              <option value="PAH">PAH (Pallet Reforzado)</option>
              <option value="AAY">AAY (Contenedor Contorno)</option>
              <option value="AAD">AAD (Contenedor Ancho)</option>
              <option value="AAZ">AAZ (Contenedor Standard)</option>
              <option value="PAG">PAG (Pallet Tradicional)</option>
            </select>
          </div>
          
          <div>
            <label class="block font-black text-slate-500 uppercase mb-1">Número de Serie</label>
            <input type="text" v-model="form.serial" placeholder="Ej: 95193" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-black uppercase text-slate-950 focus:outline-none" required />
          </div>
        </div>

        <div class="grid grid-cols-3 gap-2">
          <div>
            <label class="block font-black text-slate-500 uppercase mb-1">Gross Weight (lbs)</label>
            <input type="number" v-model.number="form.weight" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-black text-right text-slate-950 focus:outline-none" required />
          </div>
          <div>
            <label class="block font-black text-slate-500 uppercase mb-1">Tara (lbs)</label>
            <input type="number" v-model.number="form.tara" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-black text-right text-slate-400 focus:outline-none" required />
          </div>
          <div>
            <label class="block font-black text-slate-500 uppercase mb-1">Posición Deck</label>
            <input type="text" v-model="form.pos" placeholder="Ej: 12L o P1" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-black text-center text-emerald-700 uppercase focus:outline-none" />
          </div>
        </div>

        <div class="border-t border-dashed pt-3 mt-2">
          <label class="block font-black text-slate-500 uppercase mb-1">Datos de la Guía Inicial (MAWB / Commodity)</label>
          <div class="grid grid-cols-2 gap-2">
            <input type="text" v-model="form.mawb" placeholder="406-XXXXXXXX" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-mono font-bold focus:outline-none" required />
            <input type="number" v-model.number="form.pcs" placeholder="Piezas (PCS)" class="w-full bg-slate-50 border border-slate-300 rounded px-2 py-1.5 font-bold text-right focus:outline-none" required />
          </div>
        </div>

        <div class="grid grid-cols-1 gap-2 pt-1">
          <div v-if="serverError" class="p-2.5 bg-pink-50 border-l-4 border-l-pink-500 text-pink-950 text-[10px] font-black rounded uppercase tracking-wide leading-relaxed">
            {{ serverError }}
          </div>

          <div v-if="successMessage" class="p-2.5 bg-emerald-50 border-l-4 border-l-emerald-500 text-emerald-950 text-[10px] font-black rounded uppercase tracking-wide">
            {{ successMessage }}
          </div>
        </div>

        <button type="submit" class="w-full bg-slate-950 hover:bg-slate-900 text-white font-black uppercase tracking-widest py-2 rounded text-[10px] transition-colors mt-2">
          Inyectar a Matriz de Vuelo
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const serverError = ref(null)
const successMessage = ref(null)

const form = ref({
  prefix: 'PMC',
  serial: '',
  weight: null,
  tara: 270,
  pos: '',
  mawb: '',
  pcs: null
})

async function submitToLoadPlan() {
  serverError.value = null
  successMessage.value = null
  
  const finalPayload = {
    uld: `${form.value.prefix}-${form.value.serial}`,
    pos: form.value.pos,
    config: form.value.prefix,
    sello: '',
    weight: form.value.weight,
    tara: form.value.tara,
    status: 'INCOMPLETE',
    items: [{
      mawb: form.value.mawb,
      pcs: form.value.pcs,
      volumePct: 100,
      description: 'DRY CARGO',
      destino: 'MIA'
    }]
  }

  try {
    const response = await axios.post('http://localhost:8080/api/load-planning/flight/1/add-uld', finalPayload)
    if (response.data.success) {
      successMessage.value = response.data.message
      form.value.serial = ''
      form.value.weight = null
      form.value.pos = ''
      form.value.mawb = ''
      form.value.pcs = null
    }
  } catch (error) {
    if (error.response && (error.response.status === 422 || error.response.status === 400)) {
      serverError.value = error.response.data.error
    } else {
      serverError.value = 'Error de comunicación con el servicio de control de carga.'
    }
  }
}
</script>

<style scoped>
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15, 32, 67, 0.05), 1px 3px 6px rgba(15, 32, 67, 0.04); }
</style>
