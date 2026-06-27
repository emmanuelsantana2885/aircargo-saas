<template>
  <aside :style="sidebarStyle"
    class="flex flex-col flex-shrink-0 h-full border-r transition-all duration-300 ease-out relative"
    style="background: var(--surface); border-color: var(--border)">

    <!-- Toggle button -->
    <button @click="collapsed = !collapsed"
      class="absolute -right-3.5 top-5 z-20 w-7 h-7 rounded-full flex items-center justify-center transition-all duration-300 ease-out hover:scale-110 hover:shadow-xl active:scale-95"
      :class="collapsed ? 'rotate-180' : ''"
      style="background: #1e293b; color: white; box-shadow: 0 2px 8px rgba(30,41,59,0.4)">
      <IconLayoutSidebarLeftCollapse :size="16" :stroke-width="2.5" />
    </button>

    <!-- Logo -->
    <div class="px-4 py-4 border-b overflow-hidden" style="border-color: var(--border)">
      <div class="flex items-center" :class="collapsed ? 'justify-center' : 'gap-2.5'">
        <div class="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
          style="background: var(--accent)">
          <IconPlaneDeparture :size="18" color="white" :stroke-width="2" />
        </div>
        <div v-if="!collapsed" class="overflow-hidden whitespace-nowrap">
          <div class="font-semibold title" style="color: var(--text); letter-spacing: -0.01em">AirCargo</div>
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
        :title="collapsed ? item.label : ''"
        :style="isActive(item.path) ? { '--glow-color': item.activeColor } : {}">
        <component :is="item.icon" :size="collapsed ? 26 : 24" :stroke-width="isActive(item.path) ? 2.5 : 2" :color="isActive(item.path) ? item.activeColor : item.color" />
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
        <component :is="item.icon" :size="collapsed ? 26 : 24" :stroke-width="2" :color="item.color" />
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
  IconPackageExport,
  IconPlane,
  IconFileText,
  IconContainer,
  IconBuildingWarehouse,
  IconSettings,
  IconUsers,
  IconPlaneDeparture,
  IconDotsVertical,
  IconLayoutSidebarLeftCollapse,
} from '@tabler/icons-vue'

const route = useRoute()
const collapsed = ref(false)
const isActive = (path) => path === '/' ? route.path === '/' : route.path.startsWith(path)

const sidebarStyle = computed(() => ({
  width: collapsed.value ? '60px' : 'var(--sidebar-width)',
}))

const mainMenu = [
  { path: '/',            label: 'Dashboard',     icon: IconLayoutDashboard,     color: '#f59e0b', activeColor: '#f59e0b' },
  { path: '/bookings',    label: 'Bookings',       icon: IconClipboardList,      color: '#ef4444', activeColor: '#ef4444', badge: '7' },
  { path: '/receipts',    label: 'WarehouseReceipts', icon: IconBuildingWarehouse, color: '#10b981', activeColor: '#10b981' },
  { path: '/flights',     label: 'Flights',         icon: IconPlane,             color: '#3b82f6', activeColor: '#3b82f6' },
  { path: '/mawbs',       label: 'MAWBs',          icon: IconFileText,           color: '#8b5cf6', activeColor: '#8b5cf6' },
  { path: '/load-planning', label: 'LoadPlanning', icon: IconContainer,          color: '#ec4899', activeColor: '#ec4899' },
  { path: '/ulds',        label: 'ULDs -- Pallet Sheets', icon: IconPackageExport, color: '#f97316', activeColor: '#f97316' },
]

const settingsMenu = [
  { path: '/users',    label: 'Usuarios',      icon: IconUsers,     color: '#64748b', activeColor: '#64748b' },
  { path: '/settings', label: 'Configuración', icon: IconSettings,  color: '#64748b', activeColor: '#64748b' },
]
</script>

<style scoped>
.nav-default { color: var(--muted); }
.nav-default:hover { background: var(--bg); color: var(--text); }
.nav-active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 500;
  box-shadow: 0 0 14px 2px color-mix(in srgb, var(--glow-color, var(--accent)) 25%, transparent), inset 0 0 0 1px color-mix(in srgb, var(--glow-color, var(--accent)) 15%, transparent);
}
</style>
