<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm" @click.self="cancel">
      <div class="bg-white rounded-xl shadow-2xl mx-4 w-full max-w-md overflow-hidden" @keydown.esc="cancel">
        <div v-if="title" class="px-5 py-3 border-b border-slate-200">
          <h3 class="text-sm font-mono font-black uppercase tracking-wider text-slate-950">{{ title }}</h3>
        </div>
        <div class="px-5 py-4">
          <p class="text-sm font-mono text-slate-900 whitespace-pre-wrap">{{ message }}</p>
        </div>
        <div class="flex justify-end gap-2 px-5 py-3 border-t border-slate-200 bg-slate-50">
          <button v-if="cancelText" @click="cancel"
            class="text-sm px-4 py-2 rounded-lg border border-slate-300 font-mono font-bold text-slate-950 hover:bg-white transition">
            {{ cancelText }}
          </button>
          <button @click="confirm"
            class="text-sm px-4 py-2 rounded-lg font-mono font-bold text-white transition"
            :class="danger ? 'bg-slate-600 hover:bg-slate-500' : 'bg-slate-950 hover:bg-slate-800'">
            {{ confirmText || 'Aceptar' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const visible = ref(false)
const title = ref('')
const message = ref('')
const confirmText = ref('Aceptar')
const cancelText = ref('Cancelar')
const danger = ref(false)
let resolvePromise = null

function show(opts = {}) {
  title.value = opts.title || ''
  message.value = opts.message || ''
  confirmText.value = opts.confirmText || 'Aceptar'
  cancelText.value = opts.cancelText || 'Cancelar'
  danger.value = opts.danger || false
  visible.value = true
  return new Promise((resolve) => {
    resolvePromise = resolve
  })
}

function confirm() {
  visible.value = false
  if (resolvePromise) resolvePromise(true)
}

function cancel() {
  visible.value = false
  if (resolvePromise) resolvePromise(false)
}

function onKeydown(e) {
  if (!visible.value) return
  if (e.key === 'Escape') cancel()
  if (e.key === 'Enter') confirm()
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))

defineExpose({ show })
</script>
