<template>
  <header class="flex items-center justify-between px-6 border-b flex-shrink-0"
    style="height: 52px; background: var(--surface); border-color: var(--border)">

    <!-- Breadcrumb + title -->
    <div class="flex items-center gap-2">
      <span class="text-xs" style="color: var(--muted)">SDQ Hub</span>
      <IconChevronRight :size="12" style="color: var(--muted)" :stroke-width="2" />
      <span class="text-sm font-medium" style="color: var(--text)">{{ title }}</span>
    </div>

    <!-- Right -->
    <div class="flex items-center gap-3">
      <!-- Status -->
      <div class="flex items-center gap-1.5 text-xs px-2.5 py-1 rounded-full"
        style="background: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0">
        <span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
        Operación normal
      </div>

      <!-- Date -->
      <span class="text-xs" style="color: var(--muted)">{{ date }}</span>

      <!-- Notifications -->
      <button class="relative w-8 h-8 flex items-center justify-center rounded-lg transition-all"
        style="color: var(--muted)"
        onmouseenter="this.style.background='var(--bg)'"
        onmouseleave="this.style.background='transparent'">
        <IconBell :size="16" :stroke-width="1.8" />
        <span class="absolute top-1 right-1 w-2 h-2 rounded-full" style="background: #ef4444"></span>
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { IconChevronRight, IconBell } from '@tabler/icons-vue'

const route = useRoute()
const titles = {
  '/': 'Dashboard',
  '/bookings': 'Bookings',
  '/receipts': 'Recibos de Almacén',
  '/flights': 'Vuelos',
  '/mawbs': 'MAWBs',
  '/load-planning': 'Load Planning',
  '/ulds': 'ULDs',
  '/users': 'Usuarios',
}
const title = computed(() => titles[route.path] || 'AirCargo')
const date = new Intl.DateTimeFormat('es-DO', {
  weekday: 'short', day: 'numeric', month: 'short'
}).format(new Date())
</script>
