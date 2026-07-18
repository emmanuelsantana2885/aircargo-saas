<template>
  <div class="min-h-screen flex items-center justify-center p-3 md:p-8" style="background: var(--bg)">
    <div class="w-full max-w-sm p-6 md:p-8 rounded-2xl shadow-xl" style="background: var(--surface); border: 1px solid var(--border)">
      <div class="text-center mb-6">
        <div class="w-12 h-12 rounded-xl flex items-center justify-center mx-auto mb-3" style="background: var(--accent)">
          <IconLock :size="28" color="white" :stroke-width="2" />
        </div>
        <h1 class="text-xl font-bold" style="color: var(--text)">Establecer contraseña</h1>
        <p class="text-sm mt-1" style="color: var(--muted)">Ingresa tu nueva contraseña</p>
      </div>

      <form @submit.prevent="handleSetPassword" class="space-y-4">
        <div>
          <label class="block text-xs font-medium mb-1" style="color: var(--text)">Correo electrónico</label>
          <input
            v-model="email"
            type="email"
            required
            readonly
            class="w-full px-3 py-2.5 rounded-xl text-sm outline-none opacity-70"
            style="background: var(--bg); color: var(--text); border: 1px solid var(--border)"
          />
        </div>

        <div v-if="hasCurrentPassword">
          <label class="block text-xs font-medium mb-1" style="color: var(--text)">Contraseña actual</label>
          <input
            v-model="currentPassword"
            type="password"
            required
            placeholder="••••••••"
            class="w-full px-3 py-2.5 rounded-xl text-sm outline-none transition-all"
            style="background: var(--bg); color: var(--text); border: 1px solid var(--border)"
            :disabled="saving"
          />
        </div>

        <div>
          <label class="block text-xs font-medium mb-1" style="color: var(--text)">Nueva contraseña</label>
          <input
            v-model="newPassword"
            type="password"
            required
            minlength="6"
            placeholder="Mínimo 6 caracteres"
            class="w-full px-3 py-2.5 rounded-xl text-sm outline-none transition-all"
            style="background: var(--bg); color: var(--text); border: 1px solid var(--border)"
            :disabled="saving"
          />
        </div>

        <div>
          <label class="block text-xs font-medium mb-1" style="color: var(--text)">Confirmar contraseña</label>
          <input
            v-model="confirmPassword"
            type="password"
            required
            minlength="6"
            placeholder="Repite la contraseña"
            class="w-full px-3 py-2.5 rounded-xl text-sm outline-none transition-all"
            style="background: var(--bg); color: var(--text); border: 1px solid var(--border)"
            :disabled="saving"
          />
        </div>

        <button
          type="submit"
          :disabled="saving || !canSubmit"
          class="w-full py-2.5 rounded-xl text-sm font-semibold transition-all"
          :class="saving ? 'opacity-60' : 'hover:brightness-110 active:scale-[0.98]'"
          style="background: var(--accent); color: white"
        >
          <span v-if="saving" class="inline-flex items-center gap-2">
            <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            Guardando...
          </span>
          <span v-else>Establecer contraseña</span>
        </button>

        <p v-if="errorMsg" class="text-xs text-center" style="color: var(--muted)">{{ errorMsg }}</p>
        <p v-if="successMsg" class="text-xs text-center" style="color: var(--text)">{{ successMsg }}</p>

        <p class="text-xs text-center">
          <router-link to="/login" style="color: var(--accent)" class="underline">
            Volver al inicio de sesión
          </router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi } from '../api/auth'
import { IconLock } from '@tabler/icons-vue'
import { useToastStore } from '../stores/toast'
import { extractError } from '../utils/error'

const route = useRoute()
const toast = useToastStore()
const router = useRouter()

const email = ref('')
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const saving = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const hasCurrentPassword = ref(false)

const canSubmit = computed(() => {
  if (!email.value || !newPassword.value || newPassword.value.length < 6) return false
  if (newPassword.value !== confirmPassword.value) return false
  if (hasCurrentPassword.value && !currentPassword.value) return false
  return true
})

onMounted(() => {
  email.value = route.query.email || ''
  if (!email.value) {
    errorMsg.value = 'Correo electrónico no especificado'
  }
})

async function handleSetPassword() {
  errorMsg.value = ''
  successMsg.value = ''

  if (newPassword.value !== confirmPassword.value) {
    errorMsg.value = 'Las contraseñas no coinciden'
    return
  }

  saving.value = true
  try {
    await authApi.setPassword(email.value, newPassword.value, currentPassword.value || undefined)

    successMsg.value = 'Contraseña establecida correctamente'

    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (e) {
    toast.error(extractError(e))
    const status = e.response?.status
    if (status === 404) errorMsg.value = 'Usuario no encontrado'
    else if (status === 403) errorMsg.value = 'Usuario inactivo'
    else if (status === 401) errorMsg.value = 'Contraseña actual incorrecta'
    else errorMsg.value = 'Error al conectar con el servidor'
  } finally {
    saving.value = false
  }
}
</script>
