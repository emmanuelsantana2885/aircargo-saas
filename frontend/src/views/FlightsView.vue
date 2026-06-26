<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">

    <!-- Header -->
    <header class="flex justify-between items-center border-b border-slate-400 pb-3 shrink-0">
      <div>
        <h1 class="text-[12px] font-black tracking-tight text-slate-950 uppercase font-mono">Flight Control</h1>
        <p class="text-[15px] font-mono text-slate-950 mt-0.5 uppercase tracking-widest font-bold">SDQ Ramp Ops // Active Manifest Board</p>
      </div>
      <div class="flex items-center gap-3">
        <div v-if="store.error" class="text-[15px] font-mono text-rose-600 bg-rose-50 border border-rose-200 px-2 py-1 rounded">
          {{ store.error }}
        </div>
        <div class="flex items-center gap-1.5 text-[15px] font-mono font-bold text-slate-950">
          <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span>
          {{ store.flights.length }} VUELOS
        </div>
        <button @click="openCreate"
          class="flex items-center gap-1.5 text-[15px] px-4 py-2 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold transition active:scale-95 shadow-pencil-marine bg-slate-950 text-white hover:bg-slate-800">
          <IconPlus :size="12" :stroke-width="2.5" /> Nuevo Vuelo
        </button>
      </div>
    </header>

    <!-- KPIs -->
    <section class="grid grid-cols-5 gap-3 my-4 shrink-0">
      <div v-for="stat in kpis" :key="stat.label"
        class="pencil-sketch py-2 px-3 rounded bg-white border border-slate-400 shadow-pencil-marine flex flex-col justify-between min-h-[72px]">
        <div class="relative z-10">
          <h3 class="text-[12px] font-black text-slate-950 uppercase tracking-wider font-mono">{{ stat.label }}</h3>
          <div class="text-2xl font-mono font-black tracking-tight text-slate-950 mt-0.5">{{ stat.value }}</div>
        </div>
        <div class="pt-1 border-t border-slate-300 text-[15px] font-mono text-slate-950 relative z-10">{{ stat.sub }}</div>
      </div>
    </section>

    <!-- Table -->
    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">

      <div class="bg-slate-950 border-b border-slate-700 text-[11px] font-bold text-white uppercase tracking-wider grid grid-cols-12 py-3 px-5 items-center shrink-0 font-mono">
        <div class="col-span-2">Vuelo</div>
        <div class="col-span-2">Ruta</div>
        <div class="col-span-1">Aeronave</div>
        <div class="col-span-1">Matrícula</div>
        <div class="col-span-1 text-center">Fecha</div>
        <div class="col-span-1 text-center">Pos.</div>
        <div class="col-span-1 text-center">Payload kg</div>
        <div class="col-span-2 text-center">Estado</div>
        <div class="col-span-1 text-center">Acciones</div>
      </div>

      <!-- Loading -->
      <div v-if="store.loading" class="flex-1 flex items-center justify-center">
        <span class="text-[15px] font-mono text-slate-950 uppercase tracking-widest animate-pulse">Cargando vuelos...</span>
      </div>

      <!-- Empty -->
      <div v-else-if="store.flights.length === 0" class="flex-1 flex flex-col items-center justify-center gap-3">
        <IconPlaneDeparture :size="32" class="text-slate-200" :stroke-width="1.2" />
        <p class="text-[15px] font-mono text-slate-950 uppercase tracking-widest">No hay vuelos registrados</p>
        <button @click="openCreate"
          class="text-[15px] px-4 py-2 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-slate-50 transition">
          + Crear primer vuelo
        </button>
      </div>

      <!-- Rows -->
      <div v-else class="divide-y divide-slate-400 text-[10px] text-slate-950 overflow-y-auto flex-1 min-h-0 scrollbar-none">
        <div v-for="f in store.flights" :key="f.id"
          class="row-pencil grid grid-cols-12 items-center py-3 px-5 transition-all duration-150 cursor-pointer"
          @click="selectFlight(f)">

          <div class="col-span-2 font-mono font-black text-slate-950 relative z-10 flex items-center gap-2">
            <span class="text-[10px] font-bold text-white bg-slate-800 rounded px-1.5 py-0.5 uppercase tracking-wider">{{ airlineCode(f) }}</span>
            <span>{{ f.flightNumber }}</span>
          </div>
          <div class="col-span-2 font-semibold text-slate-950 relative z-10">
            {{ f.origin }} <span class="text-slate-500 mx-1">→</span> {{ f.destination }}
          </div>
          <div class="col-span-1 font-mono text-[10px] text-slate-950 relative z-10">{{ f.aircraftType }}</div>
          <div class="col-span-1 font-mono text-[10px] text-slate-950 relative z-10">{{ f.aircraftReg || 'TMP-' + f.flightNumber }}</div>
          <div class="col-span-1 text-center font-mono text-[10px] text-slate-950 relative z-10">{{ f.flightDate }}</div>
          <div class="col-span-1 text-center font-mono font-black text-slate-950 relative z-10">{{ f.totalPositions || '—' }}</div>
          <div class="col-span-1 text-center font-mono font-black text-slate-950 relative z-10">
            {{ f.maxPayloadKg ? Number(f.maxPayloadKg).toLocaleString() : '—' }}
          </div>

          <!-- Status flow -->
          <div class="col-span-2 flex justify-center relative z-10">
            <div class="flex items-center gap-1.5">
              <div v-for="step in statusSteps" :key="step.key"
                class="flex flex-col items-center">
                <span class="h-2 w-2 rounded-full border-2 transition-all"
                  :class="f.status === step.key ? step.active : (statusOrder.indexOf(f.status) > statusOrder.indexOf(step.key) ? 'bg-slate-400 border-slate-500' : 'bg-slate-100 border-slate-400')">
                </span>
                <span class="text-[10px] font-mono mt-0.5 font-black uppercase"
                  :class="f.status === step.key ? step.labelClass : 'text-slate-500'">
                  {{ step.label }}
                </span>
              </div>
            </div>
          </div>

          <div class="col-span-1 flex justify-center gap-1 relative z-10">
            <button @click.stop="openEdit(f)"
              class="w-6 h-6 flex items-center justify-center rounded border border-slate-400 text-slate-950 hover:border-slate-950 hover:text-slate-950 transition">
              <IconPencil :size="11" :stroke-width="2" />
            </button>
            <button @click.stop="confirmDelete(f)"
              class="w-6 h-6 flex items-center justify-center rounded border border-rose-100 text-rose-400 hover:border-rose-500 hover:text-rose-600 transition">
              <IconTrash :size="11" :stroke-width="2" />
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal crear/editar vuelo -->
    <div v-if="showModal" class="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4" @click.self="closeModal">
      <div class="bg-white rounded-xl border border-slate-400 shadow-2xl w-full max-w-xl p-6">

        <div class="flex justify-between items-center mb-5 pb-3 border-b border-slate-400">
          <h2 class="text-[12px] font-black font-mono uppercase tracking-wider text-slate-950">
            {{ editingFlight ? 'Editar Vuelo' : 'Nuevo Vuelo' }}
          </h2>
          <button @click="closeModal" class="text-slate-950 hover:text-slate-950">
            <IconX :size="16" :stroke-width="2" />
          </button>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Número de Vuelo *</label>
            <input v-model="form.flightNumber" type="text" placeholder="335"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Fecha *</label>
            <input v-model="form.flightDate" type="date"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Origen</label>
            <input v-model="form.origin" type="text" placeholder="SDQ" maxlength="3"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Destino</label>
            <input v-model="form.destination" type="text" placeholder="MIA" maxlength="3"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Tipo de Aeronave</label>
            <select v-model="form.aircraftType"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition">
              <option v-for="t in aircraftTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Matrícula (temporal)</label>
            <input v-model="form.aircraftReg" type="text" placeholder="N-372-UP"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Aerolínea *</label>
            <select v-model="form.airlineId"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition">
              <option value="" disabled>Seleccionar aerolínea</option>
              <option v-for="a in airlines" :key="a.id" :value="a.id">{{ a.code }} — {{ a.name }}</option>
            </select>
            <p v-if="airlinesError" class="text-[15px] font-mono text-rose-500 mt-1">Error cargando aerolíneas. ¿El backend está corriendo?</p>
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Posiciones ULD</label>
            <input v-model.number="form.totalPositions" type="number" placeholder="31"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
          </div>
          <div>
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Max Payload (kg)</label>
            <input v-model.number="form.maxPayloadKg" type="number" placeholder="45000"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
          </div>
          <div class="col-span-2">
            <label class="text-[15px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Estado</label>
            <select v-model="form.status"
              class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition">
              <option v-for="s in flightStatuses" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-6 pt-4 border-t border-slate-400">
          <button @click="closeModal"
            class="text-[15px] px-4 py-2 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-slate-50 transition">
            Cancelar
          </button>
          <button @click="saveForm" :disabled="saving"
            class="flex items-center gap-1.5 text-[15px] px-5 py-2 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 shadow-pencil-marine transition active:scale-95 disabled:opacity-50">
            <IconCheck v-if="!saving" :size="12" :stroke-width="2.5" />
            <span>{{ saving ? 'Guardando...' : (editingFlight ? 'Actualizar' : 'Crear Vuelo') }}</span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import { airlinesApi } from '../api/airlines'
import { IconPlus, IconPencil, IconTrash, IconX, IconCheck, IconPlaneDeparture } from '@tabler/icons-vue'

const store = useAppStore()
const router = useRouter()

const airlines = ref([])
const airlinesError = ref(false)

onMounted(async () => {
  await Promise.all([
    store.loadFlights(),
    airlinesApi.getAll().then(r => { airlines.value = r.data }).catch(() => { airlinesError.value = true }),
  ])
})

// ── Enums ─────────────────────────────────────────────────────
const aircraftTypes  = ['B767','B757','B737','B747','B777','A300','A310','A330','MD11','DC8','OTHER']
const flightStatuses = ['SCHEDULED','BOARDING','DEPARTED','ARRIVED','CANCELLED','DELAYED']
const statusSteps    = [
  { key: 'SCHEDULED', label: 'SCHED', active: 'bg-slate-500 border-slate-600 scale-125', labelClass: 'text-slate-950' },
  { key: 'BOARDING',  label: 'BOARD', active: 'bg-amber-500 border-amber-600 scale-125',  labelClass: 'text-amber-600' },
  { key: 'DEPARTED',  label: 'DEP',   active: 'bg-emerald-500 border-emerald-600 scale-125', labelClass: 'text-emerald-600' },
  { key: 'ARRIVED',   label: 'ARR',   active: 'bg-blue-500 border-blue-600 scale-125',    labelClass: 'text-blue-600' },
]
const statusOrder = ['SCHEDULED','BOARDING','DEPARTED','ARRIVED','CANCELLED','DELAYED']

// ── KPIs ──────────────────────────────────────────────────────
const kpis = computed(() => {
  const all = store.flights
  return [
    { label: 'Total Vuelos',  value: all.length,                                           sub: 'En base de datos' },
    { label: 'Programados',   value: all.filter(f => f.status === 'SCHEDULED').length,     sub: 'Pendientes de salida' },
    { label: 'En Abordaje',   value: all.filter(f => f.status === 'BOARDING').length,      sub: 'Carga en proceso' },
    { label: 'Despachados',   value: all.filter(f => f.status === 'DEPARTED').length,      sub: 'Fuera de rampa' },
    { label: 'Llegados',      value: all.filter(f => f.status === 'ARRIVED').length,       sub: 'En destino' },
  ]
})

// ── Modal ─────────────────────────────────────────────────────
const showModal     = ref(false)
const editingFlight = ref(null)
const saving        = ref(false)

const emptyForm = () => ({
  airlineId:      '',
  flightNumber:   '',
  origin:         'SDQ',
  destination:    'MIA',
  aircraftType:   'B767',
  aircraftReg:    '',
  flightDate:     new Date().toISOString().split('T')[0],
  status:         'SCHEDULED',
  totalPositions: 31,
  maxPayloadKg:   45000,
})
const form = ref(emptyForm())

function openCreate() {
  editingFlight.value = null
  form.value = emptyForm()
  showModal.value = true
}

function openEdit(f) {
  editingFlight.value = f
  form.value = {
    airlineId:      f.airlineId || '',
    flightNumber:   f.flightNumber || '',
    origin:         f.origin,
    destination:    f.destination,
    aircraftType:   f.aircraftType,
    aircraftReg:    f.aircraftReg || '',
    flightDate:     f.flightDate,
    status:         f.status,
    totalPositions: f.totalPositions,
    maxPayloadKg:   f.maxPayloadKg,
  }
  showModal.value = true
}

function airlineCode(f) {
  const a = airlines.value.find(x => x.id === f.airlineId)
  return a?.code || (f.flightNumber ? 'UPS' : '—')
}

function closeModal() {
  showModal.value = false
  editingFlight.value = null
}

async function saveForm() {
  if (!form.value.airlineId || !form.value.flightNumber || !form.value.flightDate) {
    alert('Aerolínea, número de vuelo y fecha son requeridos')
    return
  }
  try {
    saving.value = true
    if (editingFlight.value) {
      await store.updateFlight(editingFlight.value.id, form.value)
    } else {
      const created = await store.createFlight(form.value)
      store.selectedFlightId = created.id
    }
    closeModal()
  } catch (e) {
    alert('Error guardando vuelo: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}

async function confirmDelete(f) {
  if (!confirm(`¿Eliminar vuelo UPS-${f.flightNumber}? Esta acción no se puede deshacer.`)) return
  try {
    await store.deleteFlight(f.id)
  } catch (e) {
    alert('Error eliminando: ' + (e.response?.data?.message || e.message))
  }
}

function selectFlight(f) {
  store.selectedFlightId = f.id
  router.push({ name: 'load-planning', query: { flightId: f.id } })
}
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine {
  box-shadow: 0px 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04);
}
.pencil-sketch { position: relative; overflow: hidden; transition: transform 0.15s cubic-bezier(0.16,1,0.3,1), box-shadow 0.15s; }
.pencil-sketch::before { content: ""; position: absolute; inset: 0; opacity: 0.22; transition: opacity 0.2s; pointer-events: none; background-image: repeating-linear-gradient(45deg, rgba(71,85,105,0.07) 0px, rgba(71,85,105,0.07) 0.6px, transparent 0.6px, transparent 3px); }
.pencil-sketch:hover { transform: translate(-0.5px,-0.5px); }
.pencil-sketch:hover::before { opacity: 0.6; }
.row-pencil { position: relative; transition: background-color 0.15s ease; }
.row-pencil::before { content: ""; position: absolute; inset: 0; opacity: 0; transition: opacity 0.12s; pointer-events: none; background-image: repeating-linear-gradient(45deg, rgba(15,32,67,0.05) 0px, rgba(15,32,67,0.05) 0.6px, transparent 0.6px, transparent 2px); }
.row-pencil:hover { background-color: rgba(243,247,254,0.6); box-shadow: inset 3px 0 0 0 #0f172a; }
.row-pencil:hover::before { opacity: 1; }
</style>
