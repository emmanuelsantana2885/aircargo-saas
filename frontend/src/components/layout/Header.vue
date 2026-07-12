<template>
  <header class="flex items-center justify-between px-6 border-b flex-shrink-0"
    style="height: 44px; background: var(--surface); border-color: var(--border)">

    <div class="flex items-center gap-2">
      <IconChevronRight :size="12" style="color: var(--muted)" :stroke-width="2" />
      <span class="text-xs font-bold uppercase" style="color: var(--text)">{{ title }}</span>
    </div>

    <div class="flex items-center gap-4">
      <span class="text-xs" style="color: var(--muted)">{{ date }}</span>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { IconChevronRight } from '@tabler/icons-vue'

const route = useRoute()
const titles = {
  '/': 'Dashboard',
  '/bookings': 'Bookings',
  '/receipts': 'Receipts',
  '/flights': 'Flights',
  '/mawbs': 'MAWBs',
  '/load-planning': 'Load Planning',
  '/ulds': 'ULDs',
  '/users': 'Usuarios',
  '/settings': 'Configuración',
}
const title = computed(() => titles[route.path] || 'AirCargo')
const date = new Intl.DateTimeFormat('es-DO', {
  weekday: 'short', day: 'numeric', month: 'short'
}).format(new Date())
</script>
