<template>
  <div class="min-h-screen flex items-center justify-center" style="background: var(--bg)">
    <div class="w-full max-w-sm p-8 rounded-lg shadow-md" style="background: var(--surface); border: 1px solid var(--border)">
      <!-- Step 1: Credentials -->
      <template v-if="step === 'credentials'">
        <div class="text-center mb-6">
          <div class="w-12 h-12 rounded-lg flex items-center justify-center mx-auto mb-3" style="background: var(--accent)">
            <IconPlaneDeparture :size="28" color="white" :stroke-width="2" />
          </div>
          <h1 class="text-xl font-bold" style="color: var(--text)">AirCargo</h1>
          <p class="text-sm mt-1" style="color: var(--muted)">SDQ Operations</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label class="block text-xs font-medium mb-1" style="color: var(--text)">Correo electrónico</label>
            <input
              v-model="loginEmail"
              type="email"
              required
              placeholder="usuario@aircargo.com"
              class="w-full px-3 py-2.5 rounded text-sm outline-none transition-all border-slate-300"
              style="background: var(--bg); color: var(--text)"
              :disabled="loading"
            />
          </div>

          <div v-if="needsPassword">
            <label class="block text-xs font-medium mb-1" style="color: var(--text)">Contraseña</label>
            <input
              v-model="password"
              type="password"
              required
              placeholder="••••••••"
              class="w-full px-3 py-2.5 rounded text-sm outline-none transition-all border-slate-300"
              style="background: var(--bg); color: var(--text)"
              :disabled="loading"
            />
          </div>

          <button
            type="submit"
            :disabled="loading || !loginEmail || (needsPassword && !password)"
            class="w-full py-2.5 rounded text-sm font-semibold transition-all"
            :class="loading ? 'opacity-60' : 'hover:brightness-110 active:scale-[0.98]'"
            style="background: var(--accent); color: white"
          >
            <span v-if="loading" class="inline-flex items-center gap-2">
              <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              Ingresando...
            </span>
            <span v-else>Ingresar</span>
          </button>

          <p v-if="errorMsg" class="text-xs text-center" style="color: var(--muted)">{{ errorMsg }}</p>
          <p v-if="showSetupLink" class="text-xs text-center">
            <a href="#" @click.prevent="goToSetPassword" style="color: var(--accent)" class="underline">
              Establecer contraseña
            </a>
          </p>
        </form>
      </template>

      <!-- Step 2: Site selection -->
      <template v-if="step === 'site-select'">
        <div class="text-center mb-6">
          <div class="w-12 h-12 rounded-lg flex items-center justify-center mx-auto mb-3" style="background: var(--accent)">
            <IconBuildingStore :size="28" color="white" :stroke-width="2" />
          </div>
          <h1 class="text-xl font-bold" style="color: var(--text)">Seleccionar sitio</h1>
          <p class="text-sm mt-1" style="color: var(--muted)">Elige el sitio de operación</p>
        </div>

        <div class="space-y-3">
          <div>
            <label class="block text-xs font-medium mb-1" style="color: var(--text)">Sitio</label>
            <select
              v-model="selectedSite"
              class="w-full px-3 py-2.5 rounded text-sm outline-none border-slate-300"
              style="background: var(--bg); color: var(--text)"
            >
              <option v-for="site in auth.sites" :key="site.id" :value="site.id">
                {{ site.name }} ({{ site.code }})
              </option>
            </select>
          </div>

          <button
            @click="handleSiteConfirm"
            :disabled="!selectedSite"
            class="w-full py-2.5 rounded text-sm font-semibold transition-all"
            :class="!selectedSite ? 'opacity-60' : 'hover:brightness-110 active:scale-[0.98]'"
            style="background: var(--accent); color: white"
          >
            Ingresar a {{ selectedSiteLabel }}
          </button>

          <button
            @click="handleBackToLogin"
            class="w-full py-1.5 rounded text-xs font-medium transition-all hover:brightness-110"
            style="background: transparent; color: var(--muted)"
          >
            Volver
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { IconPlaneDeparture, IconBuildingStore } from '@tabler/icons-vue'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const router = useRouter()
const toast = useToastStore()
const auth = useAuthStore()

const loginEmail = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')
const needsPassword = ref(false)
const showSetupLink = ref(false)
const step = ref('credentials')
const selectedSite = ref(null)

const selectedSiteLabel = computed(() => {
  if (!selectedSite.value) return ''
  const site = auth.sites.find(s => s.id === selectedSite.value)
  return site ? `${site.name}` : ''
})

async function handleLogin() {
  errorMsg.value = ''
  showSetupLink.value = false
  loading.value = true
  try {
    await auth.login(loginEmail.value, password.value)
    if (auth.sites.length === 0) {
      errorMsg.value = 'No tienes sitios asignados'
      return
    }
    if (auth.sites.length === 1) {
      selectedSite.value = auth.sites[0].id
      auth.confirmSite(selectedSite.value)
      router.push('/')
      return
    }
    selectedSite.value = auth.sites[0].id
    step.value = 'site-select'
  } catch (e) {
    toast.error(extractError(e))
    const status = e.response?.status
    if (status === 428) {
      needsPassword.value = true
      showSetupLink.value = true
      errorMsg.value = 'Debes establecer una contraseña primero'
    } else if (status === 401) {
      if (needsPassword.value) {
        errorMsg.value = 'Contraseña incorrecta'
      } else {
        errorMsg.value = 'Usuario no encontrado'
      }
    } else if (status === 403) {
      errorMsg.value = 'Usuario inactivo'
    } else {
      errorMsg.value = 'Error al conectar con el servidor'
    }
  } finally {
    loading.value = false
  }
}

function handleSiteConfirm() {
  if (!selectedSite.value) return
  auth.confirmSite(selectedSite.value)
  router.push('/')
}

function handleBackToLogin() {
  step.value = 'credentials'
  auth.logout()
}

function goToSetPassword() {
  router.push(`/set-password?email=${encodeURIComponent(loginEmail.value)}`)
}
</script>
