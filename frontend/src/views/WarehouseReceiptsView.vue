<template>
  <div class="p-5 bg-white h-screen max-h-screen flex flex-col text-slate-900 font-sans antialiased overflow-hidden select-none">
    <header class="flex flex-wrap items-end justify-between gap-2 border-b border-slate-200 pb-3 shrink-0">
      <div class="flex items-end gap-3 flex-1 min-w-0">
        <div>
          <h1 class="text-xl font-black tracking-tight text-slate-950 uppercase font-mono">Warehouse Receipts</h1>
          <p class="text-[10px] font-mono text-slate-400 mt-0.5 uppercase tracking-widest font-bold">SDQ Dock // Recepción de Carga</p>
        </div>
        <div class="h-8 w-[1px] bg-slate-200"></div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Vuelo</span>
          <select v-model="localFlightId" @change="onFlightChange"
            class="bg-slate-100 border border-slate-300 rounded px-2 py-1 font-black text-slate-950 focus:outline-none uppercase tracking-widest text-xs cursor-pointer min-w-[140px]">
            <option value="">Todos los vuelos</option>
            <option v-for="f in store.flights" :key="f.id" :value="f.id">
              UPS-{{ f.flightNumber }} ({{ f.origin }}→{{ f.destination }})
            </option>
          </select>
        </div>
        <div class="flex flex-col gap-0.5 flex-1 min-w-[180px] max-w-[280px]">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Filtro (* &lt; &gt; =)</span>
          <input v-model="filterText" type="text" placeholder="Ej: *MELYSOL, >50, 406-*, =SDQ"
            class="w-full text-[11px] font-mono px-2 py-1 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
        </div>
        <div class="flex flex-col gap-0.5">
          <span class="text-[8px] font-black text-slate-400 uppercase tracking-widest">Fecha</span>
          <input v-model="filterDate" type="date"
            class="text-[11px] font-mono px-2 py-1 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
        </div>
      </div>
      <div class="flex items-center gap-2 text-[10px] font-mono font-bold text-slate-500 shrink-0">
        <button v-if="generatedReceiptId" @click="downloadReceipt"
          class="flex items-center gap-1 text-[9px] px-2 py-1 rounded font-mono uppercase tracking-wider font-bold text-white bg-emerald-600 hover:bg-emerald-700 transition shadow-pencil-marine">
          &#11015; Excel
        </button>
        <span class="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></span> {{ filteredMawbs.length }}/{{ store.mawbs.length }} MAWBs
      </div>
    </header>

    <section class="grid grid-cols-4 gap-3 my-4 shrink-0">
      <div v-for="stat in receiptStats" :key="stat.label"
        class="chalk-sketch py-1.5 px-3 rounded bg-white border border-slate-200 border-l-4 shadow-pencil-marine flex flex-col justify-between min-h-[68px]"
        :class="stat.border">
        <div class="relative z-10">
          <h3 class="text-[8.5px] font-black text-slate-400 uppercase tracking-wider font-mono truncate">{{ stat.label }}</h3>
          <div class="text-xl font-mono font-black tracking-tight text-slate-950 mt-0.5">{{ stat.value }}</div>
        </div>
        <div class="text-[8px] font-mono text-slate-400 relative z-10 truncate"><span>{{ stat.sub }}</span></div>
      </div>
    </section>

    <section class="flex-1 min-h-0 border border-slate-300 rounded overflow-hidden shadow-pencil-marine bg-white flex flex-col mb-1.5">
      <div class="bg-slate-50 border-b border-slate-300 text-[9px] font-bold text-slate-400 uppercase tracking-wider grid grid-cols-12 py-2.5 px-5 items-center shrink-0 font-mono">
        <div class="col-span-3 text-left">MAWB</div>
        <div class="col-span-2 text-left">Shipper</div>
        <div class="col-span-1 text-center">Piezas</div>
        <div class="col-span-2 text-right pr-2">Peso (kg)</div>
        <div class="col-span-2 text-center">Dest</div>
        <div class="col-span-2 text-center bg-slate-100 py-0.5 rounded border border-slate-200 text-slate-600 font-black tracking-wide">Estado</div>
      </div>

      <div v-if="store.mawbs.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">No hay MAWBs</p>
      </div>
      <div v-else-if="filteredMawbs.length === 0" class="flex-1 flex items-center justify-center">
        <p class="text-[10px] font-mono text-slate-400 uppercase tracking-widest">Ningún MAWB coincide con el filtro</p>
      </div>
      <div v-else class="divide-y divide-slate-200 text-xs text-slate-700 overflow-y-auto flex-1 min-h-0 scrollbar-none">
          <div v-for="m in filteredMawbs" :key="m.id" class="flex flex-col">
          <div class="row-pencil grid grid-cols-12 items-center py-2.5 px-5 transition-all duration-150 cursor-pointer"
            @click="toggleExpand(m)">
            <div class="col-span-3 font-mono font-bold text-slate-950 relative z-10 flex items-center gap-1.5">
              <span class="text-[8px] text-slate-400 transition-transform duration-200" :class="{ 'rotate-90': expandedId === m.id }">&#9654;</span>
              {{ m.awbNumber || m.id?.slice(0, 8) || '—' }}
              <span v-if="receiptHawbs[m.id] && receiptHawbs[m.id].length > 1"
                class="text-[7px] font-black text-amber-600 bg-amber-100 px-1 py-0.5 rounded leading-none"
                title="Múltiples HAWBs">{{ receiptHawbs[m.id].length }} HAWBs</span>
              <span v-else-if="receiptHawbs[m.id] && receiptHawbs[m.id].length === 1"
                class="text-[7px] font-black text-slate-400 bg-slate-100 px-1 py-0.5 rounded leading-none">1 HAWB</span>
            </div>
            <div class="col-span-2 text-slate-600 font-semibold relative z-10 truncate pr-3">{{ m.shipperName || '—' }}</div>
            <div class="col-span-1 text-center font-mono font-bold text-slate-900 relative z-10">{{ m.pieces || '—' }}</div>
            <div class="col-span-2 text-right font-mono font-bold text-slate-950 relative z-10 pr-2">{{ m.reportedWeightKg ? Number(m.reportedWeightKg).toLocaleString() : '—' }}</div>
            <div class="col-span-2 text-center font-mono font-bold text-slate-700 relative z-10">{{ m.destination || '—' }}</div>
            <div class="col-span-2 flex justify-center items-center gap-1 relative z-10">
              <span v-for="s in statusSteps" :key="s.key"
                @click.stop="changeMawbStatus(m, s.key)"
                class="w-2.5 h-2.5 rounded-full border-2 transition-all duration-150 cursor-pointer hover:scale-150"
                :class="getStatusDot(m, s)"
                :title="s.key + ((m.status || 'BOOKED') === s.key ? ' (actual)' : ' — clic para cambiar')"></span>
            </div>
          </div>

          <div v-if="expandedId === m.id && receiptForms[m.id]" class="bg-slate-50 border-b border-slate-200">
            <div class="p-4">
              <div class="flex items-center gap-1 mb-4">
                <div v-for="(step, si) in steps" :key="si"
                  @click="localStep = si + 1"
                  class="flex items-center cursor-pointer">
                  <div class="flex items-center gap-1.5 px-2 py-1 rounded text-[9px] font-mono font-bold uppercase tracking-wider transition-all"
                    :class="localStep === si + 1 ? 'bg-slate-950 text-white' : (localStep > si + 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-400')">
                    <span class="w-3.5 h-3.5 rounded-full flex items-center justify-center text-[8px] font-black"
                      :class="localStep === si + 1 ? 'bg-white text-slate-950' : (localStep > si + 1 ? 'bg-emerald-500 text-white' : 'bg-slate-300 text-white')">
                      {{ si + 1 }}
                    </span>
                    {{ step }}
                  </div>
                  <span v-if="si < steps.length - 1" class="text-slate-300 mx-1 text-[10px]">&#9654;</span>
                </div>
              </div>

              <div v-if="localStep === 1" class="space-y-3">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Gateway/CFS Name</label>
                    <input v-model="receiptForms[m.id].gatewayCfs" type="text" placeholder="SDQ"
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 uppercase transition" />
                  </div>
                  <div class="grid grid-cols-2 gap-2 items-end">
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" v-model="receiptForms[m.id].cashOnly" class="accent-slate-950" /> Cash Only
                    </label>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" v-model="receiptForms[m.id].bookedInAcoms" class="accent-slate-950" /> Booked in ACOMS
                    </label>
                  </div>
                </div>
                <div>
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Shipper Name</label>
                  <input v-model="receiptForms[m.id].shipperName" type="text" placeholder="Shipper"
                    class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                </div>
                <div>
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Consignee Name</label>
                  <input v-model="receiptForms[m.id].consigneeName" type="text" placeholder="Consignee"
                    class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">MAWB Number</label>
                    <input :value="m.awbNumber || ''" readonly
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-slate-100 outline-none text-slate-500" />
                  </div>
                  <div class="grid grid-cols-2 gap-2 items-end">
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" v-model="receiptForms[m.id].docsProvided" class="accent-slate-950" /> Documents Provided
                    </label>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" v-model="receiptForms[m.id].customsCompleted" class="accent-slate-950" /> Customs Completed
                    </label>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Origin</label>
                    <input v-model="receiptForms[m.id].origin" type="text" maxlength="3" placeholder="SDQ"
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 uppercase transition" />
                  </div>
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Destination</label>
                    <input v-model="receiptForms[m.id].destination" type="text" maxlength="3" placeholder="MIA"
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 uppercase transition" />
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">AWB Reported Piece</label>
                    <input v-model.number="receiptForms[m.id].awbReportedPieces" type="number" min="0"
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                  </div>
                  <div class="flex items-end">
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 flex items-center gap-2 cursor-pointer">
                      <input type="checkbox" v-model="receiptForms[m.id].preBuilt" class="accent-slate-950" /> Pre-built / Shipper Built
                    </label>
                  </div>
                </div>
                <div>
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">MAWB Weight (Greatest Shipper Reported Weight)</label>
                  <input v-model.number="receiptForms[m.id].mawbWeightGreatest" type="number" step="0.001"
                    class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                </div>

                <div v-if="receiptHawbs[m.id] && receiptHawbs[m.id].length > 0" class="border-t border-slate-200 pt-3 mt-2">
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-2">
                    Desglose por HAWB ({{ receiptHawbs[m.id].length }}) — haz clic en consignee para editar
                  </label>
                  <div class="overflow-x-auto">
                    <table class="w-full text-[9px] font-mono border-collapse">
                      <thead>
                        <tr class="bg-slate-100 text-slate-500 font-bold uppercase tracking-wider">
                          <th class="px-2 py-1 border border-slate-200 text-left">HAWB #</th>
                          <th class="px-2 py-1 border border-slate-200 text-left">Consignee</th>
                          <th class="px-2 py-1 border border-slate-200 text-center">Dest</th>
                          <th class="px-2 py-1 border border-slate-200 text-center">Piezas</th>
                          <th class="px-2 py-1 border border-slate-200 text-right">Peso (kg)</th>
                          <th class="px-2 py-1 border border-slate-200 text-center">Commodity</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="h in receiptHawbs[m.id]" :key="h.id" class="hover:bg-slate-50">
                          <td class="px-2 py-1 border border-slate-100 font-bold text-slate-950">{{ h.hawbNumber }}</td>
                          <td class="px-2 py-1 border border-slate-100">
                            <input :value="h.consigneeName || ''"
                              @input="updateHawbConsignee(h, $event.target.value)"
                              placeholder="Consignee" class="w-full border-0 outline-none bg-transparent text-[9px] font-mono" />
                          </td>
                          <td class="px-2 py-1 border border-slate-100 text-center font-mono">{{ h.destination || '—' }}</td>
                          <td class="px-2 py-1 border border-slate-100 text-center font-mono font-bold">{{ h.pieces || '—' }}</td>
                          <td class="px-2 py-1 border border-slate-100 text-right font-mono">{{ h.weightKg ? Number(h.weightKg).toFixed(2) : '—' }}</td>
                          <td class="px-2 py-1 border border-slate-100 text-center text-[8px]">{{ h.commodityType || '—' }}</td>
                        </tr>
                      </tbody>
                    </table>
                    <p v-if="receiptHawbs[m.id].length > 1" class="text-[8px] font-mono text-amber-600 mt-1">
                      &#9888; Múltiples HAWBs — las piezas en el paso siguiente se agruparán por HAWB y se generará un recibo general + uno por cada HAWB.
                    </p>
                  </div>
                </div>
              </div>

              <div v-if="localStep === 2" class="space-y-3">
                <div class="text-[10px] font-mono font-bold text-slate-500 uppercase tracking-wider">Loose Tender</div>

                <!-- Single HAWB or none: flat table -->
                <template v-if="(receiptHawbs[m.id] || []).length <= 1">
                  <div class="overflow-x-auto"> 
                    <table class="w-full text-[9px] font-mono border-collapse">
                      <thead>
                        <tr class="bg-slate-100 text-slate-500 font-bold uppercase tracking-wider">
                          <th class="px-1 py-1 border border-slate-200 w-6">#</th>
                          <th class="px-1 py-1 border border-slate-200 w-10">Pcs</th>
                          <th class="px-1 py-1 border border-slate-200 w-12">L (in)</th>
                          <th class="px-1 py-1 border border-slate-200 w-12">W (in)</th>
                          <th class="px-1 py-1 border border-slate-200 w-12">H (in)</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">Dim Wt</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">Scale LBS</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">Dim LBS</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">Scale KGS</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">Dim KGS</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">CHG KGS</th>
                          <th class="px-1 py-1 border border-slate-200 w-14">DOM LBS</th>
                          <th class="px-1 py-1 border border-slate-200 w-5"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(p, pi) in receiptForms[m.id].pieces" :key="pi">
                          <td class="px-1 py-0.5 border border-slate-100 text-center text-slate-400">{{ pi + 1 }}</td>
                          <td class="px-1 py-0.5 border border-slate-100"><input v-model.number="p.pieces" type="number" min="0" class="w-full text-center border-0 outline-none bg-transparent" /></td>
                          <td class="px-1 py-0.5 border border-slate-100"><input v-model.number="p.lengthIn" type="number" step="0.01" min="0" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, pi)" /></td>
                          <td class="px-1 py-0.5 border border-slate-100"><input v-model.number="p.widthIn" type="number" step="0.01" min="0" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, pi)" /></td>
                          <td class="px-1 py-0.5 border border-slate-100"><input v-model.number="p.heightIn" type="number" step="0.01" min="0" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, pi)" /></td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-semibold text-slate-600">{{ p.dimWeight || '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100"><input v-model.number="p.scaleWeightLbs" type="number" step="0.001" min="0" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, pi)" /></td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-semibold text-slate-600">{{ p.dimWeightLbs || '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-semibold">{{ p.scaleWeightKg ? p.scaleWeightKg.toFixed(3) : '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-semibold">{{ p.dimWeightKg ? p.dimWeightKg.toFixed(3) : '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-bold text-slate-900">{{ p.chargeableKg ? p.chargeableKg.toFixed(3) : '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100 text-right font-bold text-slate-900">{{ p.chargeableLbs ? p.chargeableLbs.toFixed(3) : '—' }}</td>
                          <td class="px-1 py-0.5 border border-slate-100 text-center">
                            <button @click="removePiece(m.id, pi)" class="text-rose-400 hover:text-rose-600 text-xs">✕</button>
                          </td>
                        </tr>
                        <tr class="bg-slate-50 font-bold text-slate-800">
                          <td class="px-1 py-1 border border-slate-200 text-slate-400"></td>
                          <td class="px-1 py-1 border border-slate-200 text-center">{{ totalPieces(m.id, null) }}</td>
                          <td class="px-1 py-1 border border-slate-200" colspan="3"></td>
                          <td class="px-1 py-1 border border-slate-200 text-right">{{ totalDimWeight(m.id, null).toFixed(2) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right">{{ totalScaleLbs(m.id, null).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right">{{ totalDimLbs(m.id, null).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right">{{ totalScaleKg(m.id, null).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right">{{ totalDimKg(m.id, null).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right text-slate-950">{{ totalChargeableKg(m.id).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200 text-right text-slate-950">{{ totalChargeableLbs(m.id).toFixed(3) }}</td>
                          <td class="px-1 py-1 border border-slate-200"></td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div class="flex justify-between items-center">
                    <button @click="addPiece(m.id)" class="text-[9px] text-slate-500 font-mono uppercase tracking-wider hover:text-slate-950 transition">+ Agregar pieza</button>
                    <div class="text-[10px] font-mono text-slate-600">
                      <span class="mr-3">Piece Count: <strong>{{ totalPieces(m.id, null) }}</strong></span>
                      <span class="mr-3">Actual: <strong>{{ totalScaleKg(m.id, null).toFixed(0) }} KGS / {{ totalScaleLbs(m.id, null).toFixed(0) }} LBS</strong></span>
                      <span>Chargeable: <strong>{{ totalChargeableKg(m.id).toFixed(0) }} KGS / {{ totalChargeableLbs(m.id).toFixed(0) }} LBS</strong></span>
                    </div>
                  </div>
                </template>

                <!-- 2+ HAWBs: sectioned by HAWB -->
                <template v-else>
                  <div v-for="h in receiptHawbs[m.id]" :key="h.id" class="border border-slate-200 rounded p-2 mb-3">
                    <div class="flex items-center justify-between mb-1.5">
                      <span class="text-[9px] font-mono font-bold text-slate-700">
                        HAWB: {{ h.hawbNumber }} &mdash; {{ h.consigneeName || '—' }}
                        <span class="text-slate-400 font-normal ml-2">({{ piecesByHawb(m.id, h.id).length }} pieza(s))</span>
                      </span>
                      <span class="text-[8px] font-mono text-slate-400">{{ piecesByHawb(m.id, h.id).reduce((s, p) => s + (p.pieces || 1), 0) }} pcs</span>
                    </div>
                    <table class="w-full text-[8px] font-mono border-collapse">
                      <thead>
                        <tr class="bg-slate-50 text-slate-400 font-bold uppercase tracking-wider">
                          <th class="px-0.5 py-0.5 border border-slate-100 w-5">#</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-8">Pcs</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">L</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">W</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">H</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">DimWt</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-12">S.LBS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">DLBS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-12">S.KGS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">DKGS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">CKGS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-10">CLBS</th>
                          <th class="px-0.5 py-0.5 border border-slate-100 w-4"></th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="(p, pi) in piecesByHawb(m.id, h.id)" :key="pi">
                          <td class="px-0.5 py-0.5 border border-slate-100 text-center text-slate-400">{{ pi + 1 }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100"><input v-model.number="p.pieces" type="number" min="0" class="w-full text-center border-0 outline-none bg-transparent" /></td>
                          <td class="px-0.5 py-0.5 border border-slate-100"><input v-model.number="p.lengthIn" type="number" step="0.01" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" /></td>
                          <td class="px-0.5 py-0.5 border border-slate-100"><input v-model.number="p.widthIn" type="number" step="0.01" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" /></td>
                          <td class="px-0.5 py-0.5 border border-slate-100"><input v-model.number="p.heightIn" type="number" step="0.01" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" /></td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right text-slate-500">{{ p.dimWeight || '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100"><input v-model.number="p.scaleWeightLbs" type="number" step="0.001" class="w-full text-center border-0 outline-none bg-transparent" @input="calcPiece(m.id, receiptForms[m.id].pieces.indexOf(p))" /></td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right text-slate-500">{{ p.dimWeightLbs || '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ p.scaleWeightKg ? p.scaleWeightKg.toFixed(2) : '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ p.dimWeightKg ? p.dimWeightKg.toFixed(2) : '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right font-bold">{{ p.chargeableKg ? p.chargeableKg.toFixed(2) : '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ p.chargeableLbs ? p.chargeableLbs.toFixed(2) : '—' }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-center">
                            <button @click="removePiece(m.id, receiptForms[m.id].pieces.indexOf(p))" class="text-rose-400 hover:text-rose-600 text-xs">✕</button>
                          </td>
                        </tr>
                        <tr class="bg-slate-50 font-semibold text-slate-600 text-[7px]">
                          <td class="px-0.5 py-0.5 border border-slate-100"></td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-center font-bold">{{ hawbTotalPieces(m.id, h.id) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100" colspan="3"></td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbDimWeight(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbScaleLbs(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbDimLbs(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbScaleKg(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbDimKg(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right font-bold">{{ hawbChargeableKg(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100 text-right">{{ hawbChargeableLbs(m.id, h.id).toFixed(1) }}</td>
                          <td class="px-0.5 py-0.5 border border-slate-100"></td>
                        </tr>
                      </tbody>
                    </table>
                    <button @click="addPiece(m.id, h.id)" class="text-[8px] text-slate-400 font-mono uppercase tracking-wider hover:text-slate-950 transition mt-1">+ Agregar pieza a este HAWB</button>
                  </div>
                  <!-- Overall totals for multi-HAWB -->
                  <div class="border-t border-slate-300 pt-2 mt-1">
                    <div class="text-[10px] font-mono text-slate-600 flex flex-wrap gap-x-4 gap-y-1">
                      <span>Total Piezas: <strong>{{ totalPieces(m.id, null) }}</strong></span>
                      <span>Escala: <strong>{{ totalScaleKg(m.id, null).toFixed(0) }} KGS</strong></span>
                      <span>Dimensional: <strong>{{ totalDimKg(m.id, null).toFixed(0) }} KGS</strong></span>
                      <span>Chargeable: <strong>{{ totalChargeableKg(m.id).toFixed(0) }} KGS / {{ totalChargeableLbs(m.id).toFixed(0) }} LBS</strong></span>
                    </div>
                  </div>
                </template>
              </div>

              <div v-if="localStep === 3" class="space-y-3">
                <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Remarks / Observaciones</label>
                <textarea v-model="receiptForms[m.id].remarks" rows="4" placeholder="Notas, observaciones, instrucciones especiales..."
                  class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition resize-none"></textarea>
              </div>

              <div v-if="localStep === 4" class="space-y-4">
                <p class="text-[10px] font-mono text-slate-400">Adjuntar fotos, documentos u otras evidencias</p>
                <div class="grid grid-cols-3 gap-3">
                  <div v-for="(ev, ei) in receiptForms[m.id].evidence" :key="ei" class="relative border border-slate-200 rounded p-2 bg-white">
                    <img v-if="ev.type === 'image'" :src="ev.url" class="w-full h-20 object-cover rounded" />
                    <div v-else class="w-full h-20 flex items-center justify-center bg-slate-50 rounded text-slate-400 text-[9px] font-mono">{{ ev.name }}</div>
                    <button @click="removeEvidence(m.id, ei)" class="absolute -top-1.5 -right-1.5 w-4 h-4 bg-rose-500 text-white rounded-full text-[8px] flex items-center justify-center">✕</button>
                    <span class="block text-[8px] font-mono text-slate-400 mt-1 truncate">{{ ev.name }}</span>
                  </div>
                  <div class="border-2 border-dashed border-slate-200 rounded p-2 flex flex-col items-center justify-center cursor-pointer hover:border-slate-950 transition group"
                    @click="addEvidence(m.id)">
                    <span class="text-[16px] text-slate-300 font-mono group-hover:text-slate-600 transition leading-none">+</span>
                    <span class="text-[7px] font-mono text-slate-400 mt-0.5 uppercase tracking-wider">Subir</span>
                  </div>
                  <div class="border-2 border-dashed border-slate-200 rounded p-2 flex flex-col items-center justify-center cursor-pointer hover:border-slate-950 transition group"
                    @click="openCamera(m.id)">
                    <IconCamera :size="18" class="text-slate-300 group-hover:text-slate-600 transition" />
                    <span class="text-[7px] font-mono text-slate-400 mt-0.5 uppercase tracking-wider">Cámara</span>
                  </div>
                </div>
                <CameraCapture :show="showCamera" @close="showCamera = false" @captured="onCameraCapture" />
                <input type="file" :ref="el => { if (el) evidenceInputs[m.id] = el }" @change="handleEvidenceUpload(m.id, $event)" accept="image/*,.pdf" class="hidden" />
              </div>

              <div v-if="localStep === 5" class="space-y-4">
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Dock Signature</label>
                    <SignaturePad v-model="receiptForms[m.id].dockSignature" :width="360" :height="100" />
                  </div>
                  <div>
                    <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Print Name</label>
                    <input v-model="receiptForms[m.id].printName" type="text" placeholder="Nombre"
                      class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                  </div>
                </div>
                <div class="border-t border-slate-200 pt-3">
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-2">Delivered By</label>
                  <div class="grid grid-cols-2 gap-4 mb-2">
                    <div>
                      <label class="text-[8px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Name</label>
                      <input v-model="receiptForms[m.id].deliveredByName" type="text"
                        class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                    </div>
                    <div>
                      <label class="text-[8px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">ID / Cédula</label>
                      <input v-model="receiptForms[m.id].deliveredByIdNum" type="text"
                        class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                    </div>
                  </div>
                  <SignaturePad v-model="receiptForms[m.id].deliveredBySig" :width="360" :height="80" />
                </div>
                <div class="border-t border-slate-200 pt-3">
                  <label class="text-[9px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-2">Broker Representative</label>
                  <div class="grid grid-cols-2 gap-4 mb-2">
                    <div>
                      <label class="text-[8px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">Name</label>
                      <input v-model="receiptForms[m.id].brokerName" type="text"
                        class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                    </div>
                    <div>
                      <label class="text-[8px] font-mono uppercase tracking-wider font-bold text-slate-400 block mb-1">ID / Cédula</label>
                      <input v-model="receiptForms[m.id].brokerIdNum" type="text"
                        class="w-full text-xs font-mono px-3 py-2 rounded border border-slate-200 bg-white outline-none focus:border-slate-950 transition" />
                    </div>
                  </div>
                  <SignaturePad v-model="receiptForms[m.id].brokerSig" :width="360" :height="80" />
                </div>
              </div>

              <div class="flex justify-between items-center mt-4 pt-3 border-t border-slate-200">
                <button @click="prevStep" :disabled="localStep === 1"
                  class="text-[10px] px-3 py-1.5 rounded border border-slate-300 font-mono uppercase tracking-wider font-bold text-slate-600 hover:bg-white transition disabled:opacity-30">
                  &#9664; Anterior
                </button>
                <div v-if="localStep < 5">
                  <button @click="nextStep"
                    class="text-[10px] px-3 py-1.5 rounded border border-slate-950 font-mono uppercase tracking-wider font-bold text-white bg-slate-950 hover:bg-slate-800 transition">
                    Siguiente &#9654;
                  </button>
                </div>
                <div v-else>
                  <button @click="submitReceipt(m)" :disabled="submitting"
                    class="flex items-center gap-1.5 text-[10px] px-4 py-1.5 rounded font-mono uppercase tracking-wider font-bold text-white bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50">
                    <span>{{ submitting ? 'Guardando...' : '&#10003; Confirmar Recibo' }}</span>
                  </button>
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
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { useAppStore } from '../stores/app'
import SignaturePad from '../components/SignaturePad.vue'
import CameraCapture from '../components/CameraCapture.vue'
import { IconCamera } from '@tabler/icons-vue'
import { hawbsApi } from '../api/hawbs'
import { mawbsApi } from '../api/mawbs'
import { receiptsApi } from '../api/receipts'

const store = useAppStore()

const localFlightId = ref(store.selectedFlightId)
const expandedId = ref(null)
const localStep = ref(1)
const submitting = ref(false)
const evidenceInputs = reactive({})
const showCamera = ref(false)
const cameraMawbId = ref(null)

const receiptForms = reactive({})
const receiptHawbs = reactive({})
const generatedReceiptId = ref(null)

const filterText = ref('')
const filterDate = ref('')

const filteredMawbs = computed(() => {
  let list = store.mawbs
  // Flight filter
  if (localFlightId.value) {
    list = list.filter(m => m.flight?.id === localFlightId.value || m.flightId === localFlightId.value)
  }
  // Date filter: check if any receipt's date matches
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
  // Text filter
  const ft = filterText.value.trim()
  if (!ft) return list
  return list.filter(m => applyFilter(m, ft))
})

function applyFilter(m, ft) {
  // Numeric comparisons
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
  // Wildcard: *text*, text*, *text
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
  // Plain text: search all text fields
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
  { key: 'BOOKED', bg: 'bg-amber-500 border-amber-600', done: 'bg-slate-400 border-slate-500', pending: 'bg-slate-200 border-slate-300' },
  { key: 'RECEIVED', bg: 'bg-blue-500 border-blue-600', done: 'bg-slate-400 border-slate-500', pending: 'bg-slate-200 border-slate-300' },
  { key: 'MANIFESTED', bg: 'bg-emerald-500 border-emerald-600', done: 'bg-slate-400 border-slate-500', pending: 'bg-slate-200 border-slate-300' },
  { key: 'DEPARTED', bg: 'bg-slate-950 border-slate-950', done: 'bg-slate-950 border-slate-950', pending: 'bg-slate-200 border-slate-300' },
]
const statusOrder = ['BOOKED', 'RECEIVED', 'MANIFESTED', 'DEPARTED']

function getStatusDot(m, s) {
  const cur = m.status || 'BOOKED'
  const idx = statusOrder.indexOf(cur)
  const stepIdx = statusOrder.indexOf(s.key)
  if (cur === s.key) return s.bg + ' scale-125'
  if (idx >= stepIdx) return s.done
  return s.pending
}

function initForm(m) {
  if (!receiptForms[m.id]) {
    // Try to fill missing shipper/consignee/weight from HAWBs
    const hawbs = receiptHawbs[m.id] || []
    const h0 = hawbs[0]
    const fallbackShipper = h0?.shipperName || m.shipperName || ''
    const fallbackConsignee = h0?.consigneeName || m.consigneeName || (hawbs.length === 1 ? hawbs[0]?.consigneeName : '') || ''
    const fallbackWeight = m.reportedWeightKg || (hawbs.length > 0 ? hawbs.reduce((s, h) => s + (h.weightKg ? Number(h.weightKg) : 0), 0) : 0) || 0

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
      pieces: [{ pieces: 1, hawbId: null, lengthIn: null, widthIn: null, heightIn: null, scaleWeightLbs: null, dimWeight: 0, dimWeightLbs: 0, scaleWeightKg: 0, dimWeightKg: 0, chargeableKg: 0, chargeableLbs: 0 }],
      remarks: '',
      evidence: [],
      dockSignature: '',
      printName: '',
      deliveredByName: '',
      deliveredByIdNum: '',
      deliveredBySig: '',
      brokerName: '',
      brokerIdNum: '',
      brokerSig: '',
    }
  }
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

function updateHawbConsignee(h, val) {
  h.consigneeName = val
  h._dirty = true
}

async function changeMawbStatus(m, newStatus) {
  const cur = m.status || 'BOOKED'
  if (cur === newStatus) return
  const labels = { BOOKED: 'Booked', RECEIVED: 'Recibido', MANIFESTED: 'Manifestado', DEPARTED: 'Despachado' }
  if (!confirm(`¿Cambiar estado de ${m.awbNumber || m.id.slice(0, 8)} de "${labels[cur]}" a "${labels[newStatus] || newStatus}"?`)) return
  m.status = newStatus  // Optimistic update: instant UI feedback
  try {
    await mawbsApi.updateStatus(m.id, newStatus)
    if (store.selectedFlightId) {
      await store.loadMawbs(store.selectedFlightId)
    } else {
      await store.loadAllMawbs()
    }
  } catch (e) {
    m.status = cur  // Revert on failure
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
      const res = await hawbsApi.getByMawb(m.id)
      receiptHawbs[m.id] = res.data
      // After HAWBs loaded, fill empty form fields from HAWB data
      if (receiptForms[m.id] && res.data.length > 0) {
        const f = receiptForms[m.id]
        const h0 = res.data[0]
        if (!f.shipperName) f.shipperName = m.shipperName || h0?.shipperName || ''
        if (!f.consigneeName) f.consigneeName = m.consigneeName || (res.data.length === 1 ? h0?.consigneeName : '') || ''
        if (!f.mawbWeightGreatest) f.mawbWeightGreatest = m.reportedWeightKg || res.data.reduce((s, h) => s + (h.weightKg ? Number(h.weightKg) : 0), 0) || 0
        if (!f.awbReportedPieces) f.awbReportedPieces = m.pieces || res.data.reduce((s, h) => s + (h.pieces || 0), 0) || 0
      }
      // If 2+ HAWBs, assign first piece row to first HAWB
      if (res.data.length > 1 && receiptForms[m.id]?.pieces?.[0]) {
        receiptForms[m.id].pieces[0].hawbId = res.data[0].id
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
  if (!generatedReceiptId.value) return
  try {
    const res = await receiptsApi.export(generatedReceiptId.value)
    const url = URL.createObjectURL(new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
    const a = document.createElement('a')
    a.href = url
    a.download = `RECIBO_BODEGA_${generatedReceiptId.value.slice(0, 8)}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Download error:', e)
    alert('Error descargando el recibo')
  }
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
      }
    }

    if (hawbs.length <= 1) {
      // Single HAWB or none: single receipt
      const res = await store.emitReceipt(buildPayload(f.pieces, ''))
      const receiptId = res?.id || res?.receipt?.id || null
      if (receiptId) generatedReceiptId.value = receiptId
    } else {
      // Multi-HAWB: general + one per HAWB
      let lastId = null
      // 1. General receipt (all pieces)
      const genRes = await store.emitReceipt(buildPayload(f.pieces, 'RECIBO GENERAL'))
      lastId = genRes?.id || genRes?.receipt?.id || null
      // 2. One receipt per HAWB
      for (const h of hawbs) {
        const hawbPieces = f.pieces.filter(p => p.hawbId === h.id)
        if (hawbPieces.length > 0) {
          await store.emitReceipt(buildPayload(hawbPieces, 'HAWB: ' + h.hawbNumber))
        }
      }
      if (lastId) generatedReceiptId.value = lastId
    }
    receiptForms[m.id] = null
    expandedId.value = null
    localStep.value = 1
    if (store.selectedFlightId) {
      await store.loadMawbs(store.selectedFlightId)
    } else {
      await store.loadAllMawbs()
    }
    await store.loadReceipts()
    alert('Recibo' + (hawbs.length > 1 ? 's' : '') + ' de almacén generado' + (hawbs.length > 1 ? ' (' + (hawbs.length + 1) + ' recibos)' : '') + ' exitosamente')
  } catch (e) {
    const data = e.response?.data
    const msg = data?.error || data?.message || (typeof data === 'string' ? data : null) || e.message
    console.error('Receipt submit error:', { error: e, responseData: data })
    alert('Error (' + e.response?.status + '): ' + msg)
  } finally {
    submitting.value = false
  }
}

const receiptStats = computed(() => {
  const all = filteredMawbs.value
  const received = all.filter(m => m.status === 'RECEIVED' || m.status === 'MANIFESTED')
  return [
    { label: "MAWBs Totales", value: all.length, sub: store.mawbs.length + " totales", border: "border-l-slate-700" },
    { label: "Pendientes", value: all.filter(m => !m.status || m.status === 'BOOKED').length, sub: "Esperando recepción", border: "border-l-amber-500" },
    { label: "Recibidos", value: received.length, sub: "En bodega", border: "border-l-emerald-500" },
    { label: "Manifestados", value: all.filter(m => m.status === 'MANIFESTED').length, sub: "En plan de carga", border: "border-l-blue-500" },
  ]
})

function onFlightChange() {
  store.selectedFlightId = localFlightId.value || null
  expandedId.value = null
  localStep.value = 1
  if (localFlightId.value) {
    store.loadMawbs(localFlightId.value)
  } else {
    store.loadAllMawbs()
  }
}

onMounted(async () => {
  if (!store.flights.length) await store.loadFlights()
  await store.loadReceipts()
  if (store.selectedFlightId) {
    localFlightId.value = store.selectedFlightId
    store.loadMawbs(store.selectedFlightId)
  } else {
    store.loadAllMawbs()
  }
})

watch(() => store.selectedFlightId, (id) => {
  localFlightId.value = id
  expandedId.value = null
  localStep.value = 1
  if (id) {
    store.loadMawbs(id)
  } else {
    store.loadAllMawbs()
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
