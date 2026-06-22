<template>
  <div class="flex h-screen overflow-hidden" style="background: var(--bg); font-family: 'Inter', sans-serif">
    <Sidebar />
    <div class="flex flex-col flex-1 min-w-0">
      <Header />
      <main class="flex-1 overflow-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import Sidebar from './components/layout/Sidebar.vue'
import Header from './components/layout/Header.vue'
</script>
