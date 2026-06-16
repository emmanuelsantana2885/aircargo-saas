<template>
  <aside :style="{width: 'var(--sidebar-width)'}"
    class="flex flex-col flex-shrink-0 h-full border-r"
    style="background: var(--surface); border-color: var(--border)">

    <!-- Logo -->
    <div class="px-5 py-4 border-b" style="border-color: var(--border)">
      <div class="flex items-center gap-2.5">
        <div class="w-7 h-7 rounded-lg flex items-center justify-center"
          style="background: var(--accent)">
          <IconPlaneDeparture :size="15" color="white" :stroke-width="2" />
        </div>
        <div>
          <div class="font-semibold text-sm" style="color: var(--text); letter-spacing: -0.01em">AirCargo</div>
          <div class="text-xs" style="color: var(--muted)">SDQ Operations</div>
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
      <div class="text-xs font-medium mb-2 px-2" style="color: var(--muted); letter-spacing: .06em; text-transform: uppercase">
        Principal
      </div>

      <RouterLink v-for="item in mainMenu" :key="item.path" :to="item.path"
        class="nav-link flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all"
        :class="isActive(item.path) ? 'nav-active' : 'nav-default'">
        <component :is="item.icon" :size="16" :stroke-width="isActive(item.path) ? 2.2 : 1.8" />
        <span>{{ item.label }}</span>
        <span v-if="item.badge"
          class="ml-auto text-xs px-1.5 py-0.5 rounded-md font-medium"
          style="background: #fee2e2; color: #dc2626">
          {{ item.badge }}
        </span>
      </RouterLink>

      <div class="text-xs font-medium mt-4 mb-2 px-2" style="color: var(--muted); letter-spacing: .06em; text-transform: uppercase">
        Configuración
      </div>

      <RouterLink v-for="item in settingsMenu" :key="item.path" :to="item.path"
        class="nav-link flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all"
        :class="isActive(item.path) ? 'nav-active' : 'nav-default'">
        <component :is="item.icon" :size="16" :stroke-width="1.8" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>

    <!-- User -->
    <div class="px-3 py-3 border-t" style="border-color: var(--border)">
      <div class="flex items-center gap-2.5 px-2 py-2 rounded-lg" style="background: var(--bg)">
        <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold"
          style="background: #dbeafe; color: #1d4ed8">
          JS
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-xs font-medium truncate" style="color: var(--text)">Juan Santana</div>
          <div class="text-xs truncate" style="color: var(--muted)">Operations</div>
        </div>
        <IconDotsVertical :size="14" style="color: var(--muted)" :stroke-width="1.8" />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
import {
  IconLayoutDashboard,
  IconClipboardList,
  IconPackage,
  IconPlane,
  IconFileText,
  IconContainer,
  IconBuildingWarehouse,
  IconSettings,
  IconUsers,
  IconPlaneDeparture,
  IconDotsVertical
} from '@tabler/icons-vue'

const route = useRoute()
const isActive = (path) => path === '/' ? route.path === '/' : route.path.startsWith(path)

const mainMenu = [
  { path: '/',            label: 'Dashboard',     icon: IconLayoutDashboard },
  { path: '/bookings',    label: 'Bookings',       icon: IconClipboardList,   badge: '7' },
  { path: '/receipts',    label: 'Recibos',        icon: IconBuildingWarehouse },
  { path: '/flights',     label: 'Vuelos',         icon: IconPlane },
  { path: '/mawbs',       label: 'MAWBs',          icon: IconFileText },
  { path: '/load-planning', label: 'Load Planning', icon: IconContainer },
  { path: '/ulds',        label: 'ULDs',           icon: IconPackage },
]

const settingsMenu = [
  { path: '/users',    label: 'Usuarios',      icon: IconUsers },
  { path: '/settings', label: 'Configuración', icon: IconSettings },
]
</script>

<style scoped>
.nav-default { color: var(--muted); }
.nav-default:hover { background: var(--bg); color: var(--text); }
.nav-active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 500;
}
</style>
