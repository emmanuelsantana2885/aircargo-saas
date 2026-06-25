<template>
  <aside :style="sidebarStyle"
    class="flex flex-col flex-shrink-0 h-full border-r transition-all duration-300 ease-out relative"
    style="background: var(--surface); border-color: var(--border)">

    <!-- Toggle button -->
    <button @click="collapsed = !collapsed"
      class="absolute -right-3.5 top-5 z-20 w-7 h-7 rounded-full flex items-center justify-center transition-all duration-300 ease-out hover:scale-110 hover:shadow-xl active:scale-95"
      :class="collapsed ? 'rotate-180' : ''"
      style="background: linear-gradient(135deg, var(--accent), #6366f1); color: white; box-shadow: 0 2px 8px rgba(99,102,241,0.3)">
      <IconChevronsLeft :size="14" :stroke-width="2.5" />
    </button>

    <!-- Logo -->
    <div class="px-4 py-4 border-b overflow-hidden" style="border-color: var(--border)">
      <div class="flex items-center" :class="collapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
          style="background: var(--accent)">
          <IconPlaneDeparture :size="15" color="white" :stroke-width="2" />
        </div>
        <div v-if="!collapsed" class="overflow-hidden whitespace-nowrap">
          <div class="font-semibold text-sm" style="color: var(--text); letter-spacing: -0.01em">AirCargo</div>
          <div class="text-xs" style="color: var(--muted)">SDQ Operations</div>
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 px-2 py-4 space-y-0.5 overflow-y-auto overflow-x-hidden">
      <div v-if="!collapsed" class="text-xs font-medium mb-2 px-2" style="color: var(--muted); letter-spacing: .06em; text-transform: uppercase">
        Principal
      </div>

      <RouterLink v-for="item in mainMenu" :key="item.path" :to="item.path"
        class="nav-link flex items-center rounded-xl text-sm transition-all duration-200 whitespace-nowrap"
        :class="[collapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2', isActive(item.path) ? 'nav-active' : 'nav-default']"
        :title="collapsed ? item.label : ''">
        <component :is="item.icon" :size="16" :stroke-width="isActive(item.path) ? 2.2 : 1.8" />
        <template v-if="!collapsed">
          <span>{{ item.label }}</span>
          <span v-if="item.badge"
            class="ml-auto text-xs px-1.5 py-0.5 rounded-md font-medium"
            style="background: #fee2e2; color: #dc2626">
            {{ item.badge }}
          </span>
        </template>
      </RouterLink>

      <div v-if="!collapsed" class="text-xs font-medium mt-4 mb-2 px-2" style="color: var(--muted); letter-spacing: .06em; text-transform: uppercase">
        Configuración
      </div>

      <div v-for="item in settingsMenu" :key="item.path"
        class="nav-link flex items-center rounded-xl text-sm transition-all duration-200 whitespace-nowrap cursor-not-allowed opacity-50"
        :class="[collapsed ? 'justify-center px-0 py-2.5' : 'gap-3 px-3 py-2']"
        :title="collapsed ? item.label : ''">
        <component :is="item.icon" :size="16" :stroke-width="1.8" />
        <span v-if="!collapsed">{{ item.label }}</span>
      </div>
    </nav>

    <!-- User -->
    <div class="px-2 py-3 border-t overflow-hidden" style="border-color: var(--border)">
      <div class="flex items-center rounded-lg px-2 py-2" style="background: var(--bg)"
        :class="collapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold shrink-0"
          style="background: #dbeafe; color: #1d4ed8">
          JS
        </div>
        <template v-if="!collapsed">
          <div class="flex-1 min-w-0">
            <div class="text-xs font-medium truncate" style="color: var(--text)">Juan Santana</div>
            <div class="text-xs truncate" style="color: var(--muted)">Operations</div>
          </div>
          <IconDotsVertical :size="14" style="color: var(--muted)" :stroke-width="1.8" />
        </template>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed } from 'vue'
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
  IconDotsVertical,
  IconChevronsLeft,
} from '@tabler/icons-vue'

const route = useRoute()
const collapsed = ref(false)
const isActive = (path) => path === '/' ? route.path === '/' : route.path.startsWith(path)

const sidebarStyle = computed(() => ({
  width: collapsed.value ? '60px' : 'var(--sidebar-width)',
}))

const mainMenu = [
  { path: '/',            label: 'Dashboard',     icon: IconLayoutDashboard },
  { path: '/bookings',    label: 'Bookings',       icon: IconClipboardList,   badge: '7' },
  { path: '/receipts',    label: 'WarehouseReceipts',        icon: IconBuildingWarehouse },
  { path: '/flights',     label: 'Flights',         icon: IconPlane },
  { path: '/mawbs',       label: 'MAWBs',          icon: IconFileText },
  { path: '/load-planning', label: 'LoadPlanning', icon: IconContainer },
  { path: '/ulds',        label: 'ULDs -- Pallet Sheets',           icon: IconPackage },
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
