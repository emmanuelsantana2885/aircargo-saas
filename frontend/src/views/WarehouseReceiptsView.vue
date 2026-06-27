<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex flex-wrap items-end justify-between gap-2 border-b border-slate-400 pb-3 shrink-0">
      <div class="flex items-end gap-3 flex-1 min-w-0">
        <div>
          <h1 class="text-xs font-black tracking-tight text-slate-950 uppercase font-mono">Warehouse Receipts</h1>
          <p class="text-xs font-mono text-slate-950 mt-0.5 uppercase tracking-widest font-bold">SDQ Dock // Recepción de Carga</p>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col gap-0.5 opacity-50">
          <span class="text-xs font-black text-slate-950 uppercase tracking-widest">Vuelo</span>
          <select disabled
            class="bg-slate-100 border border-slate-300 rounded px-3 py-1.5 font-black text-slate-800 uppercase tracking-widest text-sm min-w-[140px] cursor-not-allowed">
            <option value="">Todos los vuelos</option>
            <option v-for="f in store.flights" :key="f.id" :value="f.id">
              UPS-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }})
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-0.5 flex-1 min-w-[180px] max-w-[280px]">
          <span class="text-xs font-black text-slate-950 uppercase tracking-widest">Filtro (* &lt; &gt; =)</span>
          <input v-model="filterText" type="text" placeholder="Ej: *MELYSOL, >50, 406-*, =SDQ"
            class="w-full text-xs font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
        </div>
        <div class="flex flex-col gap-0.5">
          <span class="text-xs font-black text-slate-950 uppercase tracking-widest">Fecha</span>
          <input v-model="filterDate" type="date"
            class="text-[17px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
        </div>
      </div>
        <div class="flex items-center gap-2 text-xs font-mono font-bold text-slate-950 shrink-0">
        <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> {{ filteredMawbs.length }}/{{ store.mawbs.length }} MAWBs
      </div>
    </header>

    <section class="grid grid-cols-4 gap-3 my-4 shrink-0">
      <div v-for="stat in receiptStats" :key="stat.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-400 border-l-4 shadow-pencil-marine flex flex-col justify-between min-h-[68px] cursor-pointer transition-all duration-200"
        :class="[stat.border, statusFilter === stat.filterKey ? 'ring-2 ring-slate-950 scale-[1.02]' : 'hover:scale-[1.01]']"
        @click="toggleStatusFilter(stat.filterKey)">
        <div class="relative z-10">
          <h3 class="text-xs font-black text-slate-950 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">{{ stat.value }}</div>
        </div>
        <div class="text-xs font-mono text-slate-950 relative z-10 truncate"><span>{{ stat.sub }}</span></div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-950 border-b border-slate-700 text-xs font-bold text-white uppercase tracking-wider grid grid-cols-12 py-3 px-5 items-center shrink-0 font-mono">
        <div class="col-span-3 text-left">MAWB</div>
        <div class="col-span-2 text-left">Shipper</div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-2 text-right pr-2">Peso (kg)</div>
        <div class="col-span-1 text-center">Dest</div>
        <div class="col-span-1 text-center">Docs</div>
        <div class="col-span-2 text-center">Estado</div>
      </div>

      <div v-if="store.mawbs.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-xs font-mono text-slate-950 uppercase tracking-widest">No hay MAWBs</p>
      </div>
      <div v-else-if="filteredMawbs.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-xs font-mono text-slate-950 uppercase tracking-widest">Ningún MAWB coincide con el filtro</p>
      </div>
      <div v-else class="divide-y divide-slate-200 text-xs text-slate-950 overflow-y-auto flex-1 min-h-0 scrollbar-none">
          <div v-for="m in filteredMawbs" :key="m.id" class="flex flex-col">
          <div class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer"
            :class="expandedId === m.id ? 'row-selected' : ''"
            @click="toggleExpand(m)">
            <div class="col-span-3 font-mono font-bold text-slate-950 relative z-10 flex items-center gap-1.5">
              <span class="text-xs text-slate-950 transition-transform duration-200" :class="{ 'rotate-90': expandedId === m.id }">&#9654;</span>
              {{ m.awbNumber || m.id?.slice(0, 8) || '—' }}
              <span v-if="receiptHawbs[m.id] && receiptHawbs[m.id].length > 1"
                class="text-[16px] font-black text-amber-600 bg-amber-100 px-2 py-1 rounded leading-none"
                title="Múltiples HAWBs">{{ receiptHawbs[m.id].length }} HAWBs</span>
              <span v-else-if="receiptHawbs[m.id] && receiptHawbs[m.id].length === 1"
                class="text-[16px] font-black text-slate-950 bg-slate-100 px-2 py-1 rounded leading-none">1 HAWB</span>
            </div>
            <div class="col-span-2 text-slate-950 font-semibold relative z-10 truncate pr-3">{{ m.shipperName || '—' }}</div>
            <div class="col-span-1 text-center font-mono font-bold relative z-10"
              :class="receiptTotals[m.id]?.pieces > 0 ? 'text-emerald-600' : 'text-slate-950'">
              {{ receiptTotals[m.id]?.pieces || m.pieces || '—' }}
              <span v-if="receiptTotals[m.id]?.pieces > 0 && receiptTotals[m.id]?.pieces !== m.pieces" class="text-[17px] text-emerald-500 block leading-tight">rec: {{ receiptTotals[m.id].pieces }}</span>
            </div>
            <div class="col-span-2 text-right font-mono font-bold relative z-10 pr-2"
              :class="receiptTotals[m.id]?.weightKg > 0 ? 'text-emerald-700' : 'text-slate-950'">
              {{ receiptTotals[m.id]?.weightKg ? Number(receiptTotals[m.id].weightKg).toLocaleString() : (m.reportedWeightKg ? Number(m.reportedWeightKg).toLocaleString() : '—') }}
              <span v-if="receiptTotals[m.id]?.weightKg > 0 && receiptTotals[m.id]?.weightKg !== Number(m.reportedWeightKg)" class="text-[17px] text-emerald-500 block leading-tight">recibo</span>
            </div>
            <div class="col-span-1 text-center font-mono font-bold text-slate-950 relative z-10">{{ m.destination || '—' }}</div>
            <div class="col-span-1 flex items-center justify-center gap-0.5 relative z-10">
              <template v-if="receiptById[m.id]">
                <button @click.stop="downloadReceiptById(m)" title="Descargar Excel"
                  class="text-[16px] px-0.5 py-0.5 rounded hover:bg-emerald-100 hover:text-emerald-700 transition">&#11015;</button>
                <button @click.stop="downloadHtmlById(m)" title="Descargar HTML evidencias"
                  class="text-[16px] px-0.5 py-0.5 rounded hover:bg-slate-200 hover:text-slate-950 transition">&#128196;</button>
                <button @click.stop="downloadPdfById(m)" title="Descargar PDF evidencias"
                  class="text-[16px] px-0.5 py-0.5 rounded hover:bg-rose-100 hover:text-rose-700 transition">&#128213;</button>
              </template>
            </div>
            <div class="col-span-2 flex items-center gap-2 relative z-10">
              <div class="flex items-center gap-1">
                <span v-for="s in statusSteps" :key="s.key"
                  @click.stop="changeMawbStatus(m, s.key)"
                  class="h-3 w-3 rounded-full border-2 transition-all duration-150 cursor-pointer hover:scale-150"
                  :class="getStatusDot(m, s)"
                  :title="s.key + ((m.status || 'BOOKED') === s.key ? ' (actual)' : ' → clic')"></span>
              </div>
              <span class="text-xs font-mono font-bold uppercase tracking-wider whitespace-nowrap"
                :class="statusLabelClass(m)">{{ statusLabel(m) }}</span>
              <button @click.stop="toggleMawbEvidenceManager(m)"
                class="ml-auto text-[17px] px-2 py-1 rounded border border-slate-300 text-slate-950 hover:text-slate-950 hover:border-slate-950 transition font-mono"
                title="Gestionar evidencias documentales de esta MAWB">
                &#128193;
              </button>
            </div>
          </div>

          <div v-if="expandedId === m.id && receiptForms[m.id]" class="bg-slate-100 border-b border-slate-400">
            <div class="p-5">
              <div class="flex items-center gap-1 mb-5">
                <div v-for="(step, si) in steps" :key="si"
                  @click="localStep = si + 1"
                  class="flex items-center cursor-pointer">
                  <div class="flex items-center gap-1.5 px-2.5 py-1 rounded text-[18px] font-mono font-bold uppercase tracking-wider transition-all"
                    :class="localStep === si + 1 ? 'bg-slate-950 text-white' : (localStep > si + 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-950')">
                    <span class="w-3.5 h-3.5 rounded-full flex items-center justify-center text-[17px] font-black"
                      :class="localStep === si + 1 ? 'bg-white text-slate-950' : (localStep > si + 1 ? 'bg-emerald-500 text-white' : 'bg-slate-300 text-white')">
                      {{ si + 1 }}
                    </span>
                    {{ step }}
                  </div>
                  <span v-if="si < steps.length - 1" class="text-slate-300 mx-1 text-[16px]">&#9654;</span>
                </div>
              </div>

              <!-- ═══ STEP 1: HEADER ═══ -->
              <div v-if="localStep === 1" class="space-y-4">
                <div class="grid grid-cols-2 gap-x-8 gap-y-4">
                  <!-- Left column -->
                  <div class="space-y-3">
                    <div>
                      <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Gateway / CFS Name</label>
                      <input v-model="receiptForms[m.id].gatewayCfs" type="text" placeholder="SDQ"
                        class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
                    </div>
                    <div>
                      <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Shipper Name</label>
                      <div class="flex gap-2 items-center">
                        <input v-model="receiptForms[m.id].shipperName" type="text" placeholder="Shipper"
                          class="flex-1 text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition"
                          @blur="syncMawbName(m, 'shipperName')" />
                        <span class="text-[17px] text-slate-950 font-mono shrink-0">MAWB: {{ m.shipperName || '—' }}</span>
                      </div>
                    </div>
                    <div>
                      <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Consignee Name</label>
                      <div class="flex gap-2 items-center">
                        <input v-model="receiptForms[m.id].consigneeName" type="text" placeholder="Consignee"
                          class="flex-1 text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition"
                          @blur="syncMawbName(m, 'consigneeName')" />
                        <span class="text-[17px] text-slate-950 font-mono shrink-0">MAWB: {{ m.consigneeName || '—' }}</span>
                      </div>
                    </div>
                    <div>
                      <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">MAWB Number</label>
                      <input :value="m.awbNumber || ''" readonly
                        class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-slate-100 outline-none text-slate-950" />
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Origin</label>
                        <input v-model="receiptForms[m.id].origin" type="text" maxlength="3" placeholder="SDQ"
                          class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
                      </div>
                      <div>
                        <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Destination</label>
                        <input v-model="receiptForms[m.id].destination" type="text" maxlength="3" placeholder="MIA"
                          class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 uppercase transition" />
                      </div>
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                      <div>
                        <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">AWB Reported Pieces</label>
                        <input v-model.number="receiptForms[m.id].awbReportedPieces" type="number" min="0"
                          class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                      <div>
                        <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">MAWB Weight (Greatest Shipper Reported)</label>
                        <input v-model.number="receiptForms[m.id].mawbWeightGreatest" type="number" step="0.001"
                          class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                    </div>
                  </div>

                  <!-- Right column: HAWB info + checkboxes -->
                  <div class="space-y-3">
                    <div class="border-2 border-emerald-800 rounded-lg bg-white overflow-hidden shadow-sm">
                      <div class="flex items-center justify-between bg-emerald-800 px-4 py-2.5 border-b border-emerald-900">
                        <span class="text-xs font-mono font-bold text-white uppercase tracking-wider">
                          HAWBs
                        </span>
                        <div class="flex items-center gap-2 text-sm font-mono">
                          <span class="text-emerald-200">Cantidad:</span>
                          <input v-model.number="receiptForms[m.id].hawbCount" type="number" min="1" max="50"
                            @input="syncHawbCount(m)"
                            class="w-16 text-center border border-emerald-600 rounded px-1.5 py-1 bg-white outline-none focus:border-emerald-900 text-sm font-bold" />
                          <span v-if="receiptHawbs[m.id] && receiptHawbs[m.id].length > 0"
                            class="text-emerald-300 ml-1">({{ receiptHawbs[m.id].length }} DB)</span>
                        </div>
                      </div>
                      <div class="overflow-x-auto p-3">
                        <table class="w-full text-sm font-mono border-collapse">
                          <thead>
                            <tr class="text-emerald-900 font-bold uppercase tracking-wider">
                              <th class="px-4 py-2.5 text-left border-b-2 border-emerald-200"># HAWB</th>
                              <th class="px-4 py-2.5 text-left border-b-2 border-emerald-200">Consignee</th>
                              <th class="px-4 py-2.5 text-center border-b-2 border-emerald-200">Pcs</th>
                              <th class="px-4 py-2.5 text-right border-b-2 border-emerald-200">Kg</th>
                              <th class="px-4 py-2.5 text-center border-b-2 border-emerald-200">Dest</th>
                              <th v-if="(receiptForms[m.id].hawbEntries || []).length > 1"
                                class="px-4 py-2.5 text-center border-b-2 border-emerald-200"></th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(entry, ei) in receiptForms[m.id].hawbEntries" :key="ei"
                              class="hover:bg-emerald-50">
                              <td class="px-4 py-2.5 border-b border-emerald-100">
                                <input v-model="entry.hawbNumber" placeholder="HAWB #"
                                  class="w-24 border border-emerald-200 rounded px-3 py-1.5 outline-none focus:border-emerald-800 bg-white text-sm font-bold text-slate-950" />
                              </td>
                              <td class="px-4 py-2.5 border-b border-emerald-100">
                                <input v-model="entry.consigneeName" placeholder="Consignee"
                                  class="w-full min-w-[120px] border border-emerald-200 rounded px-3 py-1.5 outline-none focus:border-emerald-800 bg-white text-sm" />
                              </td>
                              <td class="px-4 py-2.5 border-b border-emerald-100">
                                <input v-model.number="entry.pieces" type="number" min="0" placeholder="0"
                                  class="w-16 text-center border border-emerald-200 rounded px-3 py-1.5 outline-none focus:border-emerald-800 bg-white text-sm" />
                              </td>
                              <td class="px-4 py-2.5 border-b border-emerald-100">
                                <input v-model.number="entry.weightKg" type="number" step="0.1" min="0" placeholder="0"
                                  class="w-24 text-right border border-emerald-200 rounded px-3 py-1.5 outline-none focus:border-emerald-800 bg-white text-sm" />
                              </td>
                              <td class="px-4 py-2.5 border-b border-emerald-100">
                                <input v-model="entry.destination" maxlength="3" placeholder="MIA"
                                  class="w-16 text-center border border-emerald-200 rounded px-3 py-1.5 outline-none focus:border-emerald-800 bg-white text-sm uppercase" />
                              </td>
                              <td v-if="(receiptForms[m.id].hawbEntries || []).length > 1"
                                class="px-4 py-2.5 text-center border-b border-emerald-100">
                                <button @click="removeHawbEntry(m.id, ei)"
                                  class="text-rose-400 hover:text-rose-600 transition text-sm font-bold">✕</button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>

                    <!-- Checkboxes group -->
                    <div class="border-2 border-emerald-800 rounded-lg bg-white p-4 shadow-sm">
                      <div class="text-xs font-mono font-bold text-emerald-800 uppercase tracking-wider mb-3">Flags / Marcas</div>
                      <div class="grid grid-cols-2 gap-x-5 gap-y-3">
                        <label class="text-sm font-mono font-bold text-slate-950 flex items-center gap-2 cursor-pointer select-none">
                          <input type="checkbox" v-model="receiptForms[m.id].cashOnly" class="accent-emerald-800 rounded" />
                          <span>Cash Only</span>
                        </label>
                        <label class="text-sm font-mono font-bold text-slate-950 flex items-center gap-2 cursor-pointer select-none">
                          <input type="checkbox" v-model="receiptForms[m.id].bookedInAcoms" class="accent-emerald-800 rounded" />
                          <span>Booked in ACOMS</span>
                        </label>
                        <label class="text-sm font-mono font-bold text-slate-950 flex items-center gap-2 cursor-pointer select-none">
                          <input type="checkbox" v-model="receiptForms[m.id].docsProvided" class="accent-emerald-800 rounded" />
                          <span>Documents Provided</span>
                        </label>
                        <label class="text-sm font-mono font-bold text-slate-950 flex items-center gap-2 cursor-pointer select-none">
                          <input type="checkbox" v-model="receiptForms[m.id].customsCompleted" class="accent-emerald-800 rounded" />
                          <span>Export Customs Completed</span>
                        </label>
                        <label class="text-sm font-mono font-bold text-slate-950 flex items-center gap-2 cursor-pointer select-none">
                          <input type="checkbox" v-model="receiptForms[m.id].preBuilt" class="accent-emerald-800 rounded" />
                          <span>Pre-built / Shipper Built</span>
                        </label>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- ═══ STEP 2: PIECES ═══ -->
              <div v-if="localStep === 2" class="space-y-4">
                <div class="text-xs font-mono font-bold text-slate-950 uppercase tracking-wider flex items-center gap-2">
                  <span>Loose Tender — Dimensiones y Pesos</span>
                  <span class="text-[17px] font-mono font-normal text-slate-950 normal-case tracking-normal">
                    ((L x W x H) x #pcs) / 366 = Kg dimensional
                  </span>
                </div>

                <template v-if="(receiptHawbs[m.id] || []).length <= 1">
                  <div class="overflow-x-auto border border-slate-400 rounded">
                    <table class="w-full text-xs font-mono border-collapse">
                      <thead>
                        <tr class="bg-slate-700 text-white text-xs uppercase tracking-wider">
                          <th class="px-3 py-2 border-r border-slate-600 w-6 text-center">#</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-12 text-center">Pieces</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-16 text-center">Length (in)</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-16 text-center">Width (in)</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-16 text-center">Height (in)</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-16 text-right">Dim Wt</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-20 text-right">Scale LBS</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-20 text-right">Dim LBS</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-20 text-right">Scale KGS</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-20 text-right">Dim KGS</th>
                          <th class="px-3 py-2 border-r border-slate-600 w-20 text-right">CHG KGS</th>
                          <th class="px-3 py-2 w-20 text-right">DOM LBS</th>
                          <th class="px-3 py-2 w-6"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(p, pi) in receiptForms[m.id].pieces" :key="pi"
                          class="border-b border-slate-300 hover:bg-slate-50 transition-colors">
                          <td class="px-3 py-2 text-center text-slate-950 border-r border-slate-300">{{ pi + 1 }}</td>
                          <td class="px-3 py-2 border-r border-slate-300">
                            <input v-model.number="p.pieces" type="number" min="0"
                              class="w-full text-center border border-slate-400 rounded px-2 py-1 outline-none focus:border-slate-500 bg-white text-[18px]" />
                          </td>
                          <td class="px-3 py-2 border-r border-slate-300">
                            <input v-model.number="p.lengthIn" type="number" step="0.01" min="0"
                              class="w-full text-center border border-slate-400 rounded px-2 py-1 outline-none focus:border-slate-500 bg-white text-[18px]"
                              @input="calcPiece(m.id, pi)" />
                          </td>
                          <td class="px-3 py-2 border-r border-slate-300">
                            <input v-model.number="p.widthIn" type="number" step="0.01" min="0"
                              class="w-full text-center border border-slate-400 rounded px-2 py-1 outline-none focus:border-slate-500 bg-white text-[18px]"
                              @input="calcPiece(m.id, pi)" />
                          </td>
                          <td class="px-3 py-2 border-r border-slate-300">
                            <input v-model.number="p.heightIn" type="number" step="0.01" min="0"
                              class="w-full text-center border border-slate-400 rounded px-2 py-1 outline-none focus:border-slate-500 bg-white text-[18px]"
                              @input="calcPiece(m.id, pi)" />
                          </td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-semibold text-slate-950">{{ p.dimWeight ? p.dimWeight.toFixed(1) : '—' }}</td>
                          <td class="px-3 py-2 border-r border-slate-300">
                            <input v-model.number="p.scaleWeightLbs" type="number" step="0.001" min="0"
                              class="w-full text-center border border-slate-400 rounded px-2 py-1 outline-none focus:border-slate-500 bg-white text-[18px]"
                              @input="calcPiece(m.id, pi)" />
                          </td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-semibold text-slate-950">{{ p.dimWeightLbs ? p.dimWeightLbs.toFixed(1) : '—' }}</td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-semibold">{{ p.scaleWeightKg ? p.scaleWeightKg.toFixed(2) : '—' }}</td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-semibold">{{ p.dimWeightKg ? p.dimWeightKg.toFixed(2) : '—' }}</td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-bold text-slate-900">{{ p.chargeableKg ? p.chargeableKg.toFixed(2) : '—' }}</td>
                          <td class="px-3 py-2 border-r border-slate-300 text-right font-bold text-slate-900">{{ p.chargeableLbs ? p.chargeableLbs.toFixed(2) : '—' }}</td>
                          <td class="px-3 py-2 text-center">
                            <button @click="removePiece(m.id, pi)" class="text-rose-400 hover:text-rose-600 transition text-sm">✕</button>
                          </td>
                        </tr>
                      </tbody>
                      <tfoot>
                        <tr class="bg-slate-100 font-bold text-slate-950 text-xs">
                          <td class="px-3 py-2 border-t border-slate-300 text-slate-950 text-center"></td>
                          <td class="px-3 py-2 border-t border-slate-300 text-center">{{ totalPieces(m.id, null) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-slate-950 text-[17px]" colspan="3">TOTAL</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right">{{ totalDimWeight(m.id, null).toFixed(1) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right">{{ totalScaleLbs(m.id, null).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right">{{ totalDimLbs(m.id, null).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right">{{ totalScaleKg(m.id, null).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right">{{ totalDimKg(m.id, null).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right text-slate-950">{{ totalChargeableKg(m.id).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300 text-right text-slate-950">{{ totalChargeableLbs(m.id).toFixed(2) }}</td>
                          <td class="px-3 py-2 border-t border-slate-300"></td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>
                  <div class="flex justify-between items-center">
                    <button @click="addPiece(m.id)" class="text-[18px] text-slate-950 font-mono uppercase tracking-wider hover:text-slate-950 transition">+ Agregar pieza</button>
                    <div class="flex gap-4 text-[18px] font-mono text-slate-950">
                      <span>Piece Count: <strong class="text-slate-900">{{ totalPieces(m.id, null) }}</strong></span>
                      <span>Actual: <strong class="text-slate-900">{{ totalScaleKg(m.id, null).toFixed(0) }} KGS / {{ totalScaleLbs(m.id, null).toFixed(0) }} LBS</strong></span>
                      <span>Chargeable: <strong class="text-slate-900">{{ totalChargeableKg(m.id).toFixed(0) }} KGS / {{ totalChargeableLbs(m.id).toFixed(0) }} LBS</strong></span>
                    </div>
                  </div>
                </template>

                <template v-else>
                  <div v-for="h in receiptHawbs[m.id]" :key="h.id" class="border border-slate-400 rounded overflow-hidden bg-white">
                    <div class="flex items-center justify-between bg-slate-100 px-4 py-2.5 border-b border-slate-400">
                      <span class="text-xs font-mono font-bold text-slate-950">
                        HAWB: {{ h.hawbNumber }} &mdash; {{ h.consigneeName || '—' }}
                        <span class="text-slate-950 font-normal ml-2">({{ piecesByHawb(m.id, h.id).length }} pieza(s))</span>
                      </span>
                      <span class="text-[17px] font-mono text-slate-950 font-bold">{{ piecesByHawb(m.id, h.id).reduce((s, p) => s + (p.pieces || 1), 0) }} pcs</span>
                    </div>
                    <div class="overflow-x-auto">
                      <table class="w-full text-xs font-mono border-collapse">
                        <thead>
                          <tr class="bg-slate-600 text-white text-xs uppercase tracking-wider">
                            <th class="px-1.5 py-1 border-r border-slate-500 w-5 text-center">#</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-10 text-center">Pcs</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-center">L</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-center">W</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-center">H</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-12 text-right">DimWt</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-16 text-right">S.LBS</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-right">DLBS</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-16 text-right">S.KGS</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-right">DKGS</th>
                            <th class="px-1.5 py-1 border-r border-slate-500 w-14 text-right">CKGS</th>
                            <th class="px-1.5 py-1 w-16 text-right">CLBS</th>
                            <th class="px-1.5 py-1 w-4"></th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="(p, pi) in piecesByHawb(m.id, h.id)" :key="pi"
                            class="border-b border-slate-300 hover:bg-slate-50">
                            <td class="px-1.5 py-1 text-center text-slate-950 border-r border-slate-300">{{ pi + 1 }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300">
                              <input v-model.number="p.pieces" type="number" min="0"
                                class="w-full text-center border border-slate-400 rounded px-1.5 py-1 outline-none focus:border-slate-500 bg-white text-[17px]" />
                            </td>
                            <td class="px-1.5 py-1 border-r border-slate-300">
                              <input v-model.number="p.lengthIn" type="number" step="0.01"
                                class="w-full text-center border border-slate-400 rounded px-1.5 py-1 outline-none focus:border-slate-500 bg-white text-[17px]"
                                @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" />
                            </td>
                            <td class="px-1.5 py-1 border-r border-slate-300">
                              <input v-model.number="p.widthIn" type="number" step="0.01"
                                class="w-full text-center border border-slate-400 rounded px-1.5 py-1 outline-none focus:border-slate-500 bg-white text-[17px]"
                                @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" />
                            </td>
                            <td class="px-1.5 py-1 border-r border-slate-300">
                              <input v-model.number="p.heightIn" type="number" step="0.01"
                                class="w-full text-center border border-slate-400 rounded px-1.5 py-1 outline-none focus:border-slate-500 bg-white text-[17px]"
                                @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" />
                            </td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right text-slate-950">{{ p.dimWeight ? p.dimWeight.toFixed(1) : '—' }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300">
                              <input v-model.number="p.scaleWeightLbs" type="number" step="0.001"
                                class="w-full text-center border border-slate-400 rounded px-1.5 py-1 outline-none focus:border-slate-500 bg-white text-[17px]"
                                @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" />
                            </td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right text-slate-950">{{ p.dimWeightLbs ? p.dimWeightLbs.toFixed(1) : '—' }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right">{{ p.scaleWeightKg ? p.scaleWeightKg.toFixed(2) : '—' }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right">{{ p.dimWeightKg ? p.dimWeightKg.toFixed(2) : '—' }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right font-bold">{{ p.chargeableKg ? p.chargeableKg.toFixed(2) : '—' }}</td>
                            <td class="px-1.5 py-1 border-r border-slate-300 text-right">{{ p.chargeableLbs ? p.chargeableLbs.toFixed(2) : '—' }}</td>
                            <td class="px-1.5 py-1 text-center">
                              <button @click="removePiece(m.id, receiptForms[m.id].pieces.indexOf(p))" class="text-rose-400 hover:text-rose-600 text-sm">✕</button>
                            </td>
                          </tr>
                        </tbody>
                        <tfoot>
                          <tr class="bg-slate-100 font-bold text-slate-950 text-xs">
                            <td class="px-1.5 py-1 border-t border-slate-400"></td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-center">{{ hawbTotalPieces(m.id, h.id) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400" colspan="3">TOTAL</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbDimWeight(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbScaleLbs(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbDimLbs(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbScaleKg(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbDimKg(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right font-bold">{{ hawbChargeableKg(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400 text-right">{{ hawbChargeableLbs(m.id, h.id).toFixed(1) }}</td>
                            <td class="px-1.5 py-1 border-t border-slate-400"></td>
                          </tr>
                        </tfoot>
                      </table>
                    </div>
                    <div class="px-3 py-1.5 border-t border-slate-300">
                      <button @click="addPiece(m.id, h.id)" class="text-[16px] text-slate-950 font-mono uppercase tracking-wider hover:text-slate-950 transition">+ Agregar pieza a este HAWB</button>
                    </div>
                  </div>
                  <div class="border-t border-slate-300 pt-2 mt-1">
                    <div class="text-[18px] font-mono text-slate-950 flex flex-wrap gap-x-5 gap-y-1">
                      <span>Total Piezas: <strong class="text-slate-900">{{ totalPieces(m.id, null) }}</strong></span>
                      <span>Escala: <strong class="text-slate-900">{{ totalScaleKg(m.id, null).toFixed(0) }} KGS</strong></span>
                      <span>Dimensional: <strong class="text-slate-900">{{ totalDimKg(m.id, null).toFixed(0) }} KGS</strong></span>
                      <span>Chargeable: <strong class="text-slate-900">{{ totalChargeableKg(m.id).toFixed(0) }} KGS / {{ totalChargeableLbs(m.id).toFixed(0) }} LBS</strong></span>
                    </div>
                  </div>
                </template>
              </div>

              <!-- ═══ STEP 3: REMARKS ═══ -->
              <div v-if="localStep === 3" class="space-y-3">
                <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Remarks / Observaciones</label>
                <textarea v-model="receiptForms[m.id].remarks" rows="4" placeholder="Notas, observaciones, instrucciones especiales..."
                  class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition resize-none"></textarea>
              </div>

              <!-- ═══ STEP 4: EVIDENCE ═══ -->
              <div v-if="localStep === 4" class="space-y-4">
                <p class="text-[16px] font-mono text-slate-950">Adjuntar fotos, documentos u otras evidencias</p>

                <!-- Evidencias del MAWB (desde base de datos) -->
                <div v-if="(receiptForms[m.id].mawbEvidence || []).length > 0">
                  <span class="text-xs font-mono font-bold text-slate-950 uppercase tracking-wider mb-2 block">Evidencias del MAWB</span>
                  <div class="grid grid-cols-4 gap-3 mb-4">
                    <div v-for="(ev, ei) in receiptForms[m.id].mawbEvidence" :key="'mawb-' + ei"
                      class="relative border border-amber-200 rounded bg-amber-50/30 overflow-hidden group">
                      <img v-if="ev.type === 'image' && ev.url" :src="ev.url" class="w-full h-24 object-cover" />
                      <div v-else-if="ev.type === 'text'" class="w-full h-24 flex items-center justify-center bg-slate-50 text-slate-950 text-xs font-mono px-2 text-center leading-tight">{{ ev.name }}</div>
                      <div v-else class="w-full h-24 flex items-center justify-center bg-slate-100 text-slate-950 text-[18px] font-mono">{{ ev.name }}</div>
                      <span class="block text-xs font-mono text-slate-950 px-2 py-1 leading-tight">{{ ev.name }}</span>
                    </div>
                  </div>
                </div>

                <!-- Nuevas evidencias (subidas en este formulario) -->
                <span class="text-xs font-mono font-bold text-slate-950 uppercase tracking-wider mb-2 block">Nuevas evidencias (este recibo)</span>
                <div class="grid grid-cols-4 gap-3">
                  <div v-for="(ev, ei) in receiptForms[m.id].evidence" :key="'rec-' + ei" class="relative border border-slate-400 rounded bg-white overflow-hidden group">
                    <img v-if="ev.type === 'image'" :src="ev.url" class="w-full h-24 object-cover" />
                    <div v-else class="w-full h-24 flex items-center justify-center bg-slate-100 text-slate-950 text-[18px] font-mono">{{ ev.name }}</div>
                    <button @click="removeEvidence(m.id, ei)" class="absolute top-1 right-1 w-4 h-4 bg-rose-500 text-white rounded-full text-[16px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition">✕</button>
                    <span class="block text-[16px] font-mono text-slate-950 px-2 py-1 truncate">{{ ev.name }}</span>
                  </div>
                  <div class="border-2 border-dashed border-slate-400 rounded flex flex-col items-center justify-center cursor-pointer hover:border-slate-950 transition group min-h-[100px]"
                    @click="addEvidence(m.id)">
                    <span class="text-[21px] text-slate-300 font-mono group-hover:text-slate-950 transition leading-none">+</span>
                    <span class="text-[16px] font-mono text-slate-950 mt-0.5 uppercase tracking-wider">Subir</span>
                  </div>
                  <div class="border-2 border-dashed border-slate-400 rounded flex flex-col items-center justify-center cursor-pointer hover:border-slate-950 transition group min-h-[100px]"
                    @click="openCamera(m.id)">
                    <IconCamera :size="20" class="text-slate-300 group-hover:text-slate-950 transition" />
                    <span class="text-[16px] font-mono text-slate-950 mt-0.5 uppercase tracking-wider">Cámara</span>
                  </div>
                </div>
                <CameraCapture :show="showCamera" @close="showCamera = false" @captured="onCameraCapture" />
                <input type="file" :ref="el => { if (el) evidenceInputs[m.id] = el }" @change="handleEvidenceUpload(m.id, $event)" accept="image/*,.pdf" class="hidden" />
              </div>

              <!-- ═══ STEP 5: SIGNATURES ═══ -->
              <div v-if="localStep === 5" class="space-y-3">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Dock Signature</label>
                    <SignaturePad v-model="receiptForms[m.id].dockSignature" :width="320" :height="80" />
                  </div>
                  <div class="flex flex-col justify-end">
                    <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-1">Print Name</label>
                    <input v-model="receiptForms[m.id].printName" type="text" placeholder="Nombre"
                      class="w-full text-sm font-mono px-4 py-2.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4 border-t border-slate-400 pt-3">
                  <div>
                    <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-2">Delivered By</label>
                    <div class="grid grid-cols-2 gap-2 mb-2">
                      <div>
                        <label class="text-[16px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-0.5">Name</label>
                        <input v-model="receiptForms[m.id].deliveredByName" type="text"
                          class="w-full text-[16px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                      <div>
                        <label class="text-[16px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-0.5">ID / Cédula</label>
                        <input v-model="receiptForms[m.id].deliveredByIdNum" type="text"
                          class="w-full text-[16px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                    </div>
                    <SignaturePad v-model="receiptForms[m.id].deliveredBySig" :width="320" :height="60" />
                  </div>
                  <div>
                    <label class="text-[18px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-2">Broker Representative</label>
                    <div class="grid grid-cols-2 gap-2 mb-2">
                      <div>
                        <label class="text-[16px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-0.5">Name</label>
                        <input v-model="receiptForms[m.id].brokerName" type="text"
                          class="w-full text-[16px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                      <div>
                        <label class="text-[16px] font-mono uppercase tracking-wider font-bold text-slate-950 block mb-0.5">ID / Cédula</label>
                        <input v-model="receiptForms[m.id].brokerIdNum" type="text"
                          class="w-full text-[16px] font-mono px-3 py-1.5 rounded border border-slate-400 bg-white outline-none focus:border-slate-950 transition" />
                      </div>
                    </div>
                    <SignaturePad v-model="receiptForms[m.id].brokerSig" :width="320" :height="60" />
                  </div>
                </div>
              </div>

              <div class="flex justify-between items-center mt-4 pt-3 border-t border-slate-400">
                <div class="flex items-center gap-3">
                  <button @click="prevStep" :disabled="localStep === 1"
                    class="text-[16px] px-3 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-white transition disabled:opacity-30">
                    &#9664; Anterior
                  </button>
                  <span v-if="successMsg" class="text-emerald-700 text-[16px] font-mono font-bold animate-pulse">{{ successMsg }}</span>
                </div>
                <div v-if="localStep < 5">
                  <button @click="nextStep"
                    class="text-[16px] px-3 py-1.5 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition">
                    Siguiente &#9654;
                  </button>
                </div>
                <div v-else>
                  <button v-if="receiptForms[m.id]._existingReceiptId" @click="editReceipt(m)"
                    class="flex items-center gap-1.5 text-[16px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-blue-600 hover:bg-blue-700 transition mr-2">
                    &#9998; Editar Recibo
                  </button>
                  <button @click="submitReceipt(m)" :disabled="submitting"
                    class="flex items-center gap-1.5 text-[16px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50">
                    <span>{{ submitting ? 'Guardando...' : (receiptForms[m.id]._existingReceiptId ? '&#10003; Actualizar Recibo' : '&#10003; Confirmar Recibo') }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- MAWB Evidence Manager Modal -->
    <Teleport to="body">
      <div v-if="mawbEvidenceMgr.show" class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm" @click.self="closeMawbEvidenceMgr">
        <div class="bg-white rounded-lg shadow-2xl overflow-hidden mx-4" style="max-width: 640px; width: 100%; max-height: 80vh;">
          <div class="flex items-center justify-between px-4 py-2.5 border-b border-slate-400">
            <span class="text-xs font-mono font-black uppercase tracking-widest text-slate-950">
              Evidencias — {{ mawbEvidenceMgr.mawb?.awbNumber || 'MAWB' }}
            </span>
            <button @click="closeMawbEvidenceMgr" class="text-slate-950 hover:text-slate-950 transition text-base">✕</button>
          </div>
          <div class="p-4 overflow-y-auto" style="max-height: calc(80vh - 120px);">
            <div v-if="mawbEvidenceMgr.docs.length === 0" class="text-[16px] font-mono text-slate-950 text-center py-6 uppercase tracking-widest">
              Sin evidencias documentales
            </div>
            <div v-else class="grid grid-cols-3 gap-2 mb-4">
              <div v-for="(doc, di) in mawbEvidenceMgr.docs" :key="di"
                class="relative border border-slate-400 rounded overflow-hidden bg-white group">
                <img v-if="doc.type === 'image' && doc.url" :src="doc.url" class="w-full h-20 object-cover" />
                <div v-else class="w-full h-20 flex items-center justify-center bg-slate-100 text-slate-950 text-[17px] font-mono">{{ doc.name }}</div>
                <button @click="removeMawbEvidence(di)" class="absolute top-0.5 right-0.5 w-3.5 h-3.5 bg-rose-500 text-white rounded-full text-[18px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition">✕</button>
                <span class="block text-[18px] font-mono text-slate-950 px-2 py-1 truncate">{{ doc.name }}</span>
              </div>
            </div>
            <div class="flex items-center gap-2 border-t border-slate-300 pt-3">
              <button @click="mawbEvidenceInput.click()"
                class="text-[18px] px-3 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-white transition">
                + Subir archivo
              </button>
              <button @click="openMawbCamera()"
                class="text-[18px] px-3 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-950 hover:bg-white transition flex items-center gap-1">
                <IconCamera :size="12" /> Cámara
              </button>
              <button v-if="mawbEvidenceMgr.docs.length > 0" @click="downloadMawbEvidencePdf()"
                class="ml-auto text-[18px] px-3 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-rose-700 hover:bg-rose-800 transition">
                &#128196; PDF
              </button>
            </div>
            <input type="file" ref="mawbEvidenceInput" @change="handleMawbEvidenceUpload" accept="image/*,.pdf" class="hidden" />
            <CameraCapture :show="mawbCameraOpen" @close="mawbCameraOpen = false" @captured="onMawbCameraCapture" />
          </div>
          <div class="flex justify-end px-4 py-2.5 border-t border-slate-400 bg-slate-100">
            <button @click="saveMawbEvidence()"
              class="text-[18px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition">
              Guardar cambios
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '../stores/app'
import SignaturePad from '../components/SignaturePad.vue'
import CameraCapture from '../components/CameraCapture.vue'
import { IconCamera } from '@tabler/icons-vue'
import { hawbsApi } from '../api/hawbs'
import { mawbsApi } from '../api/mawbs'
import { receiptsApi } from '../api/receipts'

const store = useAppStore()
const route = useRoute()

const localFlightId = ref(store.selectedFlightId)
const expandedId = ref(null)
const localStep = ref(1)
const submitting = ref(false)
const successMsg = ref('')
let successTimer = null
const evidenceInputs = reactive({})
const showCamera = ref(false)
const cameraMawbId = ref(null)

const receiptForms = reactive({})
const receiptHawbs = reactive({})
const generatedReceiptId = ref(null)

const downloadableReceiptId = computed(() => {
  if (generatedReceiptId.value) return generatedReceiptId.value
  const mId = expandedId.value
  if (!mId) return null
  const f = receiptForms[mId]
  return f?._existingReceiptId || null
})

const filterText = ref('')
const filterDate = ref('')
const statusFilter = ref(null)

const statusPriority = { BOOKED: 0, RECEIVED: 1, MANIFESTED: 2, DEPARTED: 3 }

function toggleStatusFilter(key) {
  statusFilter.value = statusFilter.value === key ? null : key
}

const filteredMawbs = computed(() => {
  let list = store.mawbs
  if (statusFilter.value === 'PENDING') {
    list = list.filter(m => !m.status || m.status === 'BOOKED')
  } else if (statusFilter.value === 'RECEIVED') {
    list = list.filter(m => m.status === 'RECEIVED' || m.status === 'MANIFESTED')
  } else if (statusFilter.value === 'MANIFESTED') {
    list = list.filter(m => m.status === 'MANIFESTED')
  }
  if (filterDate.value) {
    const target = filterDate.value
    const mawbsWithReceipt = store.receipts
      .filter(r => {
        const d = r.receiptDate || r.createdAt
        return d && d.startsWith(target)
      })
      .map(r => r.mawb?.id || r.mawbId)
      .filter(Boolean)
    list = list.filter(m => mawbsWithReceipt.includes(m.id))
  }
  const ft = filterText.value.trim()
  if (ft) list = list.filter(m => applyFilter(m, ft))

  return [...list].sort((a, b) => {
    const pa = statusPriority[a.status || 'BOOKED'] ?? 0
    const pb = statusPriority[b.status || 'BOOKED'] ?? 0
    if (pa !== pb) return pa - pb
    return (b.awbNumber || '').localeCompare(a.awbNumber || '')
  })
})

function applyFilter(m, ft) {
  const numMatch = ft.match(/^(>=?|<=?|=)?(\d+(?:\.\d+)?)$/)
  if (numMatch) {
    const op = numMatch[1] || '='
    const val = parseFloat(numMatch[2])
    const num = m.pieces || 0
    switch (op) {
      case '>=': return num >= val
      case '>':  return num > val
      case '<=': return num <= val
      case '<':  return num < val
      case '=':  return num === val
      default:   return false
    }
  }
  if (ft.startsWith('*') && ft.endsWith('*')) {
    const mid = ft.slice(1, -1).toLowerCase()
    return matchAnyField(m, v => v.toLowerCase().includes(mid))
  }
  if (ft.startsWith('*')) {
    const suffix = ft.slice(1).toLowerCase()
    return matchAnyField(m, v => v.toLowerCase().endsWith(suffix))
  }
  if (ft.endsWith('*')) {
    const prefix = ft.slice(0, -1).toLowerCase()
    return matchAnyField(m, v => v.toLowerCase().startsWith(prefix))
  }
  const lower = ft.toLowerCase()
  return matchAnyField(m, v => v.toLowerCase().includes(lower))
}

function matchAnyField(m, fn) {
  const fields = [
    m.awbNumber, m.shipperName, m.consigneeName,
    m.destination, m.origin,
  ].filter(Boolean)
  return fields.some(fn)
}

const steps = ['HEADER', 'PIECES', 'REMARKS', 'EVIDENCE', 'SIGNATURES']
const statusSteps = [
  { key: 'BOOKED',     color: 'bg-amber-500 border-amber-600', label: 'Pendiente', tone: 'amber' },
  { key: 'RECEIVED',   color: 'bg-blue-500 border-blue-600',   label: 'En Proceso', tone: 'blue' },
  { key: 'MANIFESTED', color: 'bg-emerald-500 border-emerald-600', label: 'En Proceso', tone: 'emerald' },
  { key: 'DEPARTED',   color: 'bg-slate-950 border-slate-950', label: 'Terminado', tone: 'slate' },
]
const statusOrder = ['BOOKED', 'RECEIVED', 'MANIFESTED', 'DEPARTED']

function getStatusDot(m, s) {
  const cur = m.status || 'BOOKED'
  const idx = statusOrder.indexOf(cur)
  const stepIdx = statusOrder.indexOf(s.key)
  if (idx < 0) return 'bg-slate-200 border-slate-300'
  if (stepIdx < idx) return s.color + ' opacity-60'
  if (stepIdx === idx) return s.color + ' scale-125 ring-2 ring-offset-1 ' + (
    s.tone === 'amber' ? 'ring-amber-300' :
    s.tone === 'blue' ? 'ring-blue-300' :
    s.tone === 'emerald' ? 'ring-emerald-300' :
    s.tone === 'slate' ? 'ring-slate-400' : 'ring-slate-300'
  )
  return 'bg-slate-200 border-slate-300'
}

function statusLabel(m) {
  const labels = { BOOKED: 'Pendiente', RECEIVED: 'Recibido', MANIFESTED: 'Manifestado', DEPARTED: 'Despachado' }
  return labels[m.status] || 'Pendiente'
}

function statusLabelClass(m) {
  const cls = {
    BOOKED: 'text-amber-600',
    RECEIVED: 'text-blue-600',
    MANIFESTED: 'text-emerald-600',
    DEPARTED: 'text-slate-500',
  }
  return cls[m.status] || 'text-amber-600'
}

function initForm(m) {
  if (!receiptForms[m.id]) {
    const hawbs = receiptHawbs[m.id] || []
    const h0 = hawbs[0]
    const fallbackShipper = h0?.shipperName || m.shipperName || ''
    const fallbackConsignee = h0?.consigneeName || m.consigneeName || (hawbs.length === 1 ? hawbs[0]?.consigneeName : '') || ''
    const fallbackWeight = m.reportedWeightKg || (hawbs.length > 0 ? hawbs.reduce((s, h) => s + (h.weightKg ? Number(h.weightKg) : 0), 0) : 0) || 0
    const hawbEntries = hawbs.length > 0
      ? hawbs.map(h => ({
          hawbNumber: h.hawbNumber || '',
          consigneeName: h.consigneeName || '',
          pieces: h.pieces || 0,
          weightKg: h.weightKg ? Number(h.weightKg) : 0,
          destination: h.destination || m.destination || 'MIA',
          _dbId: h.id,
        }))
      : [{ hawbNumber: '', consigneeName: '', pieces: 0, weightKg: 0, destination: m.destination || 'MIA', _dbId: null }]

    receiptForms[m.id] = {
      gatewayCfs: 'SDQ',
      shipperName: m.shipperName || fallbackShipper,
      consigneeName: m.consigneeName || fallbackConsignee,
      origin: m.origin || store.selectedFlight?.origin || 'SDQ',
      destination: m.destination || store.selectedFlight?.destination || 'MIA',
      awbReportedPieces: m.pieces || (hawbs.length > 0 ? hawbs.reduce((s, h) => s + (h.pieces || 0), 0) : 0) || 0,
      mawbWeightGreatest: m.reportedWeightKg || fallbackWeight,
      cashOnly: false,
      bookedInAcoms: false,
      docsProvided: false,
      customsCompleted: false,
      preBuilt: false,
      hawbCount: Math.max(hawbs.length, 1),
      hawbEntries,
      pieces: [{ pieces: 1, hawbId: null, lengthIn: null, widthIn: null, heightIn: null, scaleWeightLbs: null, dimWeight: 0, dimWeightLbs: 0, scaleWeightKg: 0, dimWeightKg: 0, chargeableKg: 0, chargeableLbs: 0 }],
      remarks: '',
      evidence: [],
      mawbEvidence: [],
      dockSignature: '',
      printName: '',
      deliveredByName: '',
      deliveredByIdNum: '',
      deliveredBySig: '',
      brokerName: '',
      brokerIdNum: '',
      brokerSig: '',
      _existingReceiptId: null,
      pieceCount: 0,
      totalWeightKg: 0,
    }
  }
}

async function loadExistingReceiptData(m) {
  const f = receiptForms[m.id]
  if (!f) return
  const existingReceipts = store.receipts.filter(r => (r.mawb?.id || r.mawbId) === m.id)
  if (existingReceipts.length === 0) return
  const lastReceipt = existingReceipts[existingReceipts.length - 1]
  f._existingReceiptId = lastReceipt.id
  f.gatewayCfs = lastReceipt.gatewayCfs || 'SDQ'
  f.shipperName = lastReceipt.shipperName || f.shipperName
  f.consigneeName = lastReceipt.consigneeName || f.consigneeName
  f.origin = lastReceipt.origin || f.origin
  f.destination = lastReceipt.destination || f.destination
  f.awbReportedPieces = lastReceipt.awbReportedPieces || f.awbReportedPieces
  f.mawbWeightGreatest = lastReceipt.mawbWeightGreatest || f.mawbWeightGreatest
  f.cashOnly = lastReceipt.cashOnly ?? f.cashOnly
  f.bookedInAcoms = lastReceipt.bookedInAcoms ?? f.bookedInAcoms
  f.docsProvided = lastReceipt.docsProvided ?? f.docsProvided
  f.customsCompleted = lastReceipt.customsCompleted ?? f.customsCompleted
  f.preBuilt = lastReceipt.preBuilt ?? f.preBuilt
  f.remarks = lastReceipt.remarks || f.remarks
  f.dockSignature = lastReceipt.dockSignature || f.dockSignature
  f.printName = lastReceipt.printName || f.printName
  f.deliveredByName = lastReceipt.deliveredByName || f.deliveredByName
  f.deliveredByIdNum = lastReceipt.deliveredByIdNum || f.deliveredByIdNum
  f.deliveredBySig = lastReceipt.deliveredBySigUrl || f.deliveredBySig
  f.brokerName = lastReceipt.brokerName || f.brokerName
  f.brokerIdNum = lastReceipt.brokerIdNum || f.brokerIdNum
  f.brokerSig = lastReceipt.brokerSigUrl || f.brokerSig
  f.pieceCount = lastReceipt.pieceCount || 0
  f.totalWeightKg = lastReceipt.actualWeightKg || lastReceipt.chargeableWeightKg || 0
  try {
    const piecesRes = await receiptsApi.getPieces(lastReceipt.id)
    const loadedPieces = piecesRes.data || []
    if (loadedPieces.length > 0) {
      f.pieces = loadedPieces.map(p => ({
        pieces: 1,
        hawbId: p.hawbId || null,
        lengthIn: p.lengthIn || null,
        widthIn: p.widthIn || null,
        heightIn: p.heightIn || null,
        scaleWeightLbs: p.scaleWeightLbs || null,
        dimWeight: p.dimWeight || 0,
        dimWeightLbs: p.dimWeightLbs || 0,
        scaleWeightKg: p.scaleWeightKg || 0,
        dimWeightKg: p.dimWeightKg || 0,
        chargeableKg: p.chargeableKg || 0,
        chargeableLbs: p.chargeableLbs || 0,
      }))
    }
  } catch {}
}

function calcPiece(mawbId, pi) {
  const p = receiptForms[mawbId].pieces[pi]
  const l = p.lengthIn || 0
  const w = p.widthIn || 0
  const h = p.heightIn || 0
  const qty = p.pieces || 1
  const vol = l * w * h * qty
  p.dimWeight = vol > 0 ? vol / 194 : 0
  p.dimWeightLbs = vol > 0 ? vol / 194 : 0
  p.dimWeightKg = vol > 0 ? vol / 366 : 0
  p.scaleWeightKg = p.scaleWeightLbs ? p.scaleWeightLbs / 2.20462 : 0
  p.chargeableKg = Math.max(p.dimWeightKg, p.scaleWeightKg)
  p.chargeableLbs = Math.max(p.dimWeightLbs, p.scaleWeightLbs || 0)
}

function allPieces(mawbId, hawbId) {
  const pieces = receiptForms[mawbId]?.pieces || []
  return hawbId ? pieces.filter(p => p.hawbId === hawbId) : pieces
}

function totalPieces(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.pieces || 1), 0)
}

function totalDimWeight(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.dimWeight || 0), 0)
}

function totalScaleLbs(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.scaleWeightLbs || 0), 0)
}

function totalDimLbs(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.dimWeightLbs || 0), 0)
}

function totalScaleKg(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.scaleWeightKg || 0), 0)
}

function totalDimKg(mawbId, hawbId) {
  return allPieces(mawbId, hawbId).reduce((s, p) => s + (p.dimWeightKg || 0), 0)
}

function totalChargeableKg(mawbId, hawbId) {
  const pieces = allPieces(mawbId, hawbId)
  const totalDim = pieces.reduce((s, p) => s + (p.dimWeightKg || 0), 0)
  const totalScale = pieces.reduce((s, p) => s + (p.scaleWeightKg || 0), 0)
  return Math.max(totalDim, totalScale)
}

function totalChargeableLbs(mawbId, hawbId) {
  const pieces = allPieces(mawbId, hawbId)
  const totalDim = pieces.reduce((s, p) => s + (p.dimWeightLbs || 0), 0)
  const totalScale = pieces.reduce((s, p) => s + (p.scaleWeightLbs || 0), 0)
  return Math.max(totalDim, totalScale)
}

function piecesByHawb(mawbId, hawbId) {
  return allPieces(mawbId, hawbId)
}

function hawbTotalPieces(mawbId, hawbId) { return totalPieces(mawbId, hawbId) }
function hawbDimWeight(mawbId, hawbId) { return totalDimWeight(mawbId, hawbId) }
function hawbScaleLbs(mawbId, hawbId) { return totalScaleLbs(mawbId, hawbId) }
function hawbDimLbs(mawbId, hawbId) { return totalDimLbs(mawbId, hawbId) }
function hawbScaleKg(mawbId, hawbId) { return totalScaleKg(mawbId, hawbId) }
function hawbDimKg(mawbId, hawbId) { return totalDimKg(mawbId, hawbId) }
function hawbChargeableKg(mawbId, hawbId) { return totalChargeableKg(mawbId, hawbId) }
function hawbChargeableLbs(mawbId, hawbId) { return totalChargeableLbs(mawbId, hawbId) }

function syncHawbCount(m) {
  const f = receiptForms[m.id]
  if (!f) return
  const target = Math.max(1, Math.min(50, f.hawbCount || 1))
  f.hawbCount = target
  while (f.hawbEntries.length < target) {
    f.hawbEntries.push({ hawbNumber: '', consigneeName: '', pieces: 0, weightKg: 0, destination: f.destination || 'MIA', _dbId: null })
  }
  while (f.hawbEntries.length > target) {
    f.hawbEntries.pop()
  }
}

function removeHawbEntry(mawbId, idx) {
  const f = receiptForms[mawbId]
  if (!f || f.hawbEntries.length <= 1) return
  f.hawbEntries.splice(idx, 1)
  f.hawbCount = f.hawbEntries.length
}

function updateHawbConsignee(h, val) {
  h.consigneeName = val
  h._dirty = true
}

async function syncMawbName(m, field) {
  const val = receiptForms[m.id]?.[field]
  if (val !== undefined && val !== m[field]) {
    try {
      await mawbsApi.update(m.id, { [field]: val })
      m[field] = val
    } catch (e) {
      console.warn('Error syncing MAWB ' + field + ':', e)
    }
  }
}

async function changeMawbStatus(m, newStatus) {
  const cur = m.status || 'BOOKED'
  if (cur === newStatus) return
  const labels = { BOOKED: 'Booked', RECEIVED: 'Recibido', MANIFESTED: 'Manifestado', DEPARTED: 'Despachado' }
  if (!confirm(`¿Cambiar estado de ${m.awbNumber || m.id.slice(0, 8)} de "${labels[cur]}" a "${labels[newStatus] || newStatus}"?`)) return
  m.status = newStatus
  try {
    await mawbsApi.updateStatus(m.id, newStatus)
    if (store.selectedFlightId) {
      await store.loadMawbs(store.selectedFlightId)
    } else {
      await store.loadAllMawbs()
    }
  } catch (e) {
    m.status = cur
    alert('Error al actualizar estado: ' + (e.response?.data?.error || e.message))
  }
}

function addPiece(mawbId, hawbId) {
  receiptForms[mawbId].pieces.push({ pieces: 1, hawbId: hawbId || null, lengthIn: null, widthIn: null, heightIn: null, scaleWeightLbs: null, dimWeight: 0, dimWeightLbs: 0, scaleWeightKg: 0, dimWeightKg: 0, chargeableKg: 0, chargeableLbs: 0 })
}

function removePiece(mawbId, idx) {
  const pieces = receiptForms[mawbId].pieces
  if (pieces.length > 1) pieces.splice(idx, 1)
}

async function toggleExpand(m) {
  if (expandedId.value === m.id) {
    expandedId.value = null
    localStep.value = 1
    generatedReceiptId.value = null
  } else {
    expandedId.value = m.id
    localStep.value = 1
    generatedReceiptId.value = null
    initForm(m)
    try {
      const [hawbRes, docsRes] = await Promise.all([
        hawbsApi.getByMawb(m.id),
        mawbsApi.getSupportingDocs(m.id).catch(() => ({ data: [] })),
      ])
      const hawbData = hawbRes.data
      receiptHawbs[m.id] = hawbData
      const f = receiptForms[m.id]
      if (f) {
        // Cargar evidencias del MAWB (solo lectura) en el formulario
        f.mawbEvidence = (docsRes.data || []).filter(d => d.type === 'image' || d.type === 'document')

        await loadExistingReceiptData(m)

        if (hawbData.length > 0) {
          const h0 = hawbData[0]
          if (!f.shipperName) f.shipperName = m.shipperName || h0?.shipperName || ''
          if (!f.consigneeName) f.consigneeName = m.consigneeName || (hawbData.length === 1 ? h0?.consigneeName : '') || ''
          if (!f.mawbWeightGreatest) f.mawbWeightGreatest = m.reportedWeightKg || hawbData.reduce((s, h) => s + (h.weightKg ? Number(h.weightKg) : 0), 0) || 0
          if (!f.awbReportedPieces) f.awbReportedPieces = m.pieces || hawbData.reduce((s, h) => s + (h.pieces || 0), 0) || 0
          const existingIds = new Set(f.hawbEntries.filter(e => e._dbId).map(e => e._dbId))
          for (const h of hawbData) {
            if (!existingIds.has(h.id)) {
              f.hawbEntries.push({
                hawbNumber: h.hawbNumber || '',
                consigneeName: h.consigneeName || '',
                pieces: h.pieces || 0,
                weightKg: h.weightKg ? Number(h.weightKg) : 0,
                destination: h.destination || f.destination || 'MIA',
                _dbId: h.id,
              })
            }
          }
          f.hawbCount = f.hawbEntries.length
        }
        if (hawbData.length > 1 && receiptForms[m.id]?.pieces?.[0]) {
          receiptForms[m.id].pieces[0].hawbId = hawbData[0].id
        }
      }
    } catch {
      receiptHawbs[m.id] = []
    }
  }
}

function nextStep() { if (localStep.value < 5) localStep.value++ }
function prevStep() { if (localStep.value > 1) localStep.value-- }

function openCamera(mawbId) {
  cameraMawbId.value = mawbId
  showCamera.value = true
}

function onCameraCapture(dataUrl) {
  const mawbId = cameraMawbId.value
  if (mawbId && dataUrl) {
    receiptForms[mawbId].evidence.push({
      name: 'Foto_' + new Date().toISOString().slice(0, 19).replace(/[T:]/g, '-') + '.jpg',
      type: 'image',
      url: dataUrl,
      file: null,
    })
  }
  cameraMawbId.value = null
}

function addEvidence(mawbId) {
  const el = evidenceInputs[mawbId]
  if (el) el.click()
}

function handleEvidenceUpload(mawbId, e) {
  const files = e.target.files
  if (!files || !files.length) return
  for (const file of files) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      receiptForms[mawbId].evidence.push({
        name: file.name,
        type: file.type.startsWith('image/') ? 'image' : 'document',
        url: ev.target.result,
        file: file,
      })
    }
    reader.readAsDataURL(file)
  }
  e.target.value = ''
}

function removeEvidence(mawbId, idx) {
  receiptForms[mawbId].evidence.splice(idx, 1)
}

async function downloadReceipt() {
  const id = downloadableReceiptId.value
  if (!id) return
  try {
    const res = await receiptsApi.export(id)
    const disposition = res.headers?.['content-disposition'] || ''
    const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";\n]+)"?/)
    const filename = match ? match[1].trim() : `RECIBO_BODEGA_${id.slice(0, 8)}.xlsx`
    const url = URL.createObjectURL(new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Download error:', e)
    alert('Error descargando el recibo')
  }
}

async function downloadSupportingDocsHtml() {
  const id = downloadableReceiptId.value
  if (!id) return
  try {
    const res = await receiptsApi.getSupportingDocsHtml(id)
    const blob = new Blob([res.data], { type: 'text/html; charset=UTF-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `EVIDENCIAS_${id.slice(0, 8)}.html`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Supporting docs HTML error:', e)
    alert('Error descargando documento de evidencias')
  }
}

async function downloadSupportingDocsPdf() {
  const id = downloadableReceiptId.value
  if (!id) return
  try {
    const res = await receiptsApi.getSupportingDocsPdf(id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `EVIDENCIAS_${id.slice(0, 8)}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.message || e.message
    console.error('Supporting docs PDF error:', e)
    alert('Error: ' + msg)
  }
}

async function downloadReceiptById(m) {
  const id = receiptById.value[m.id]
  if (!id) return
  try {
    const res = await receiptsApi.export(id)
    const disposition = res.headers?.['content-disposition'] || ''
    const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";\n]+)"?/)
    const filename = match ? match[1].trim() : `RECIBO_BODEGA_${id.slice(0, 8)}.xlsx`
    const url = URL.createObjectURL(new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Download error:', e)
  }
}

async function downloadHtmlById(m) {
  const id = receiptById.value[m.id]
  if (!id) return
  try {
    const res = await receiptsApi.getSupportingDocsHtml(id)
    const blob = new Blob([res.data], { type: 'text/html; charset=UTF-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `EVIDENCIAS_${id.slice(0, 8)}.html`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Supporting docs HTML error:', e)
  }
}

async function downloadPdfById(m) {
  const id = receiptById.value[m.id]
  if (!id) return
  try {
    const res = await receiptsApi.getSupportingDocsPdf(id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `EVIDENCIAS_${id.slice(0, 8)}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    const msg = e.response?.data?.error || e.response?.data?.message || e.message
    console.error('Supporting docs PDF error:', e)
  }
}

async function editReceipt(m) {
  return submitReceipt(m)
}

async function submitReceipt(m) {
  const f = receiptForms[m.id]
  const hawbs = receiptHawbs[m.id] || []
  if (!f) return
  if (f.pieces.length === 0 || !f.pieces[0].lengthIn) {
    alert('Ingresa al menos una pieza con dimensiones')
    return
  }
  submitting.value = true
  try {
    // Save MAWB name changes
    if (f.shipperName && f.shipperName !== m.shipperName) {
      await mawbsApi.update(m.id, { shipperName: f.shipperName })
      m.shipperName = f.shipperName
    }
    if (f.consigneeName && f.consigneeName !== m.consigneeName) {
      await mawbsApi.update(m.id, { consigneeName: f.consigneeName })
      m.consigneeName = f.consigneeName
    }

    // Save HAWB consignee changes
    for (const h of hawbs) {
      if (h._dirty) {
        await hawbsApi.update(h.id, { consigneeName: h.consigneeName })
      }
    }

    function buildPayload(pieceList, remarkSuffix) {
      return {
        receipt: {
          airline: { id: '00000000-0000-0000-0000-000000000001' },
          mawb: { id: m.id },
          gatewayCfs: f.gatewayCfs || 'SDQ',
          shipperName: f.shipperName || m.shipperName,
          consigneeName: f.consigneeName || m.consigneeName || '',
          origin: f.origin || 'SDQ',
          destination: f.destination || 'MIA',
          awbReportedPieces: f.awbReportedPieces || pieceList.reduce((s, p) => s + (p.pieces || 1), 0),
          mawbWeightGreatest: f.mawbWeightGreatest || 0,
          pieceCount: pieceList.reduce((s, p) => s + (p.pieces || 1), 0),
          cashOnly: f.cashOnly || false,
          bookedInAcoms: f.bookedInAcoms || false,
          docsProvided: f.docsProvided || false,
          customsCompleted: f.customsCompleted || false,
          preBuilt: f.preBuilt || false,
          remarks: (f.remarks || '') + (remarkSuffix ? ' — ' + remarkSuffix : ''),
          dockSignature: f.dockSignature || '',
          printName: f.printName || '',
          deliveredByName: f.deliveredByName || '',
          deliveredByIdNum: f.deliveredByIdNum || '',
          deliveredBySigUrl: f.deliveredBySig || '',
          receivedByName: f.printName || '',
          receivedBySigUrl: '',
          brokerName: f.brokerName || '',
          brokerIdNum: f.brokerIdNum || '',
          brokerSigUrl: f.brokerSig || '',
          startDatetime: new Date().toISOString(),
          receiptDate: new Date().toISOString(),
        },
        pieces: pieceList.map((p, i) => ({
          pieceNumber: i + 1,
          lengthIn: p.lengthIn || 0,
          widthIn: p.widthIn || 0,
          heightIn: p.heightIn || 0,
          scaleWeightLbs: p.scaleWeightLbs || 0,
          scaleWeightKg: p.scaleWeightKg || 0,
          dimWeightLbs: p.dimWeightLbs || 0,
          dimWeightKg: p.dimWeightKg || 0,
          chargeableLbs: p.chargeableLbs || 0,
          chargeableKg: p.chargeableKg || 0,
        })),
        supportingDocs: f.evidence.map(ev => ({
          name: ev.name,
          type: ev.type,
          url: ev.url,
        })),
      }
    }

    function sendReceipt(payload) {
      if (f._existingReceiptId) {
        return receiptsApi.updateEmit(f._existingReceiptId, payload)
      }
      return store.emitReceipt(payload)
    }

    if (hawbs.length <= 1) {
      const res = await sendReceipt(buildPayload(f.pieces, ''))
      const receiptId = res?.id || res?.receipt?.id || null
      if (receiptId) generatedReceiptId.value = receiptId
    } else {
      let lastId = null
      const genRes = await sendReceipt(buildPayload(f.pieces, 'RECIBO GENERAL'))
      lastId = genRes?.id || genRes?.receipt?.id || null
      for (const h of hawbs) {
        const hawbPieces = f.pieces.filter(p => p.hawbId === h.id)
        if (hawbPieces.length > 0) {
          await sendReceipt(buildPayload(hawbPieces, 'HAWB: ' + h.hawbNumber))
        }
      }
      if (lastId) generatedReceiptId.value = lastId
    }
    const wasExisting = !!f._existingReceiptId
    if (store.selectedFlightId) {
      await store.loadMawbs(store.selectedFlightId)
    } else {
      await store.loadAllMawbs()
    }
    await store.loadReceipts()
    await loadExistingReceiptData(m)
    localStep.value = 5
    successMsg.value = wasExisting ? 'Recibo actualizado correctamente' : 'Recibo de almacén generado exitosamente'
    if (successTimer) clearTimeout(successTimer)
    successTimer = setTimeout(() => { successMsg.value = '' }, 4000)
  } catch (e) {
    const data = e.response?.data
    const msg = data?.error || data?.message || (typeof data === 'string' ? data : null) || e.message
    console.error('Receipt submit error:', { error: e, responseData: data })
    alert('Error (' + e.response?.status + '): ' + msg)
  } finally {
    submitting.value = false
  }
}

const receiptTotals = computed(() => {
  const totals = {}
  for (const r of store.receipts) {
    const mawbId = r.mawb?.id || r.mawbId
    if (!mawbId) continue
    if (!totals[mawbId]) totals[mawbId] = { pieces: 0, weightKg: 0 }
    totals[mawbId].pieces += r.pieceCount || 0
    const w = r.actualWeightKg || r.chargeableWeightKg || 0
    totals[mawbId].weightKg += Number(w)
  }
  return totals
})

const receiptById = computed(() => {
  const map = {}
  for (const r of store.receipts) {
    const mawbId = r.mawb?.id || r.mawbId
    if (mawbId) map[mawbId] = r.id
  }
  return map
})

const receiptStats = computed(() => {
  const all = filteredMawbs.value
  const received = all.filter(m => m.status === 'RECEIVED' || m.status === 'MANIFESTED')
  return [
    { label: "MAWBs Totales", value: all.length, sub: store.mawbs.length + " totales", border: "border-l-slate-700", filterKey: null },
    { label: "Pendientes", value: all.filter(m => !m.status || m.status === 'BOOKED').length, sub: "Esperando recepción", border: "border-l-amber-500", filterKey: 'PENDING' },
    { label: "Recibidos", value: received.length, sub: "En bodega", border: "border-l-emerald-500", filterKey: 'RECEIVED' },
    { label: "Manifestados", value: all.filter(m => m.status === 'MANIFESTED').length, sub: "En plan de carga", border: "border-l-blue-500", filterKey: 'MANIFESTED' },
  ]
})

// ── MAWB Evidence Manager ──
const mawbEvidenceMgr = reactive({ show: false, mawbId: null, mawb: null, docs: [], dirty: false })
const mawbEvidenceInput = ref(null)
const mawbCameraOpen = ref(false)

async function toggleMawbEvidenceManager(m) {
  if (mawbEvidenceMgr.show && mawbEvidenceMgr.mawbId === m.id) {
    closeMawbEvidenceMgr()
    return
  }
  mawbEvidenceMgr.show = true
  mawbEvidenceMgr.mawbId = m.id
  mawbEvidenceMgr.mawb = m
  mawbEvidenceMgr.dirty = false
  try {
    const res = await mawbsApi.getSupportingDocs(m.id)
    mawbEvidenceMgr.docs = res.data || []
  } catch {
    mawbEvidenceMgr.docs = []
  }
}

function closeMawbEvidenceMgr() {
  if (mawbEvidenceMgr.dirty) {
    if (!confirm('Hay cambios sin guardar. ¿Descartar cambios?')) return
  }
  mawbEvidenceMgr.show = false
  mawbEvidenceMgr.mawbId = null
  mawbEvidenceMgr.mawb = null
  mawbEvidenceMgr.docs = []
  mawbEvidenceMgr.dirty = false
}

function handleMawbEvidenceUpload(e) {
  const files = e.target.files
  if (!files || !files.length) return
  for (const file of files) {
    const reader = new FileReader()
    reader.onload = (ev) => {
      mawbEvidenceMgr.docs.push({
        name: file.name,
        type: file.type.startsWith('image/') ? 'image' : 'document',
        url: ev.target.result,
      })
      mawbEvidenceMgr.dirty = true
    }
    reader.readAsDataURL(file)
  }
  e.target.value = ''
}

function openMawbCamera() {
  mawbCameraOpen.value = true
}

function onMawbCameraCapture(dataUrl) {
  if (dataUrl) {
    mawbEvidenceMgr.docs.push({
      name: 'Foto_' + new Date().toISOString().slice(0, 19).replace(/[T:]/g, '-') + '.jpg',
      type: 'image',
      url: dataUrl,
    })
    mawbEvidenceMgr.dirty = true
  }
}

function removeMawbEvidence(idx) {
  mawbEvidenceMgr.docs.splice(idx, 1)
  mawbEvidenceMgr.dirty = true
}

async function saveMawbEvidence() {
  const id = mawbEvidenceMgr.mawbId
  if (!id) return
  try {
    const docsToSave = mawbEvidenceMgr.docs.map(d => ({ name: d.name, type: d.type, url: d.url }))
    await mawbsApi.updateSupportingDocs(id, docsToSave)
    mawbEvidenceMgr.dirty = false
    alert('Evidencias guardadas correctamente')
  } catch (e) {
    alert('Error guardando evidencias: ' + (e.response?.data?.error || e.message))
  }
}

async function downloadMawbEvidencePdf() {
  const id = mawbEvidenceMgr.mawbId
  if (!id || mawbEvidenceMgr.docs.length === 0) return
  try {
    const res = await mawbsApi.getSupportingDocsPdf(id)
    const blob = new Blob([res.data], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `EVIDENCIAS_MAWB_${mawbEvidenceMgr.mawb?.awbNumber || id.slice(0, 8)}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('MAWB evidence PDF error:', e)
    alert('Error descargando PDF de evidencias')
  }
}

onMounted(async () => {
  if (!store.flights.length) await store.loadFlights()
  await store.loadReceipts()
  await store.loadAllMawbs()
  if (route.query.mawbId && store.mawbs.length) {
    const m = store.mawbs.find(x => x.id === route.query.mawbId)
    if (m) {
      expandedId.value = m.id
      initForm(m)
    }
  }
})

watch(() => store.mawbs, (mawbs) => {
  if (expandedId.value) {
    const m = mawbs.find(x => x.id === expandedId.value)
    if (m) initForm(m)
  }
})
</script>

<style scoped>
.scrollbar-none::-webkit-scrollbar { display: none; }
.scrollbar-none { -ms-overflow-style: none; scrollbar-width: none; }
.shadow-pencil-marine { box-shadow: 0px 1px 2px rgba(15,32,67,0.08), 1px 3px 6px rgba(15,32,67,0.06), 3px 6px 12px rgba(15,32,67,0.04); }
.chalk-sketch { position: relative; overflow: hidden; transition: transform 0.15s cubic-bezier(0.16,1,0.3,1), box-shadow 0.15s; }
.chalk-sketch::before { content: ""; position: absolute; inset: 0; opacity: 0.2; pointer-events: none; background-image: repeating-linear-gradient(30deg, rgba(71,85,105,0.06) 0px, rgba(71,85,105,0.06) 0.8px, transparent 0.8px, transparent 4px), repeating-linear-gradient(-30deg, rgba(71,85,105,0.03) 0px, rgba(71,85,105,0.03) 0.8px, transparent 0.8px, transparent 4px); }
.chalk-sketch:hover { transform: translate(-0.5px,-0.5px); }
.chalk-sketch:hover::before { opacity: 0.65; }
.row-pencil { position: relative; transition: background-color 0.15s ease, box-shadow 0.15s ease; }
.row-pencil::before { content: ""; position: absolute; inset: 0; opacity: 0; transition: opacity 0.12s ease-in-out; pointer-events: none; background-image: repeating-linear-gradient(45deg, rgba(15,32,67,0.05) 0px, rgba(15,32,67,0.05) 0.6px, transparent 0.6px, transparent 2px); }
.row-pencil:hover { background-color: rgba(243,247,254,0.6); box-shadow: inset 3px 0 0 0 #0f172a; }
.row-pencil:hover::before { opacity: 1; }
</style>
