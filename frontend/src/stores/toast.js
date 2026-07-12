import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])
  let nextId = 0

  function add(type, message, duration = 4000) {
    const id = ++nextId
    toasts.value.push({ id, type, message })
    if (duration > 0) {
      setTimeout(() => remove(id), duration)
    }
  }

  function remove(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx >= 0) toasts.value.splice(idx, 1)
  }

  function success(msg, duration) { add('success', msg, duration) }
  function error(msg, duration) { add('error', msg, duration) }
  function warning(msg, duration) { add('warning', msg, duration) }
  function info(msg, duration) { add('info', msg, duration) }

  return { toasts, add, remove, success, error, warning, info }
})
