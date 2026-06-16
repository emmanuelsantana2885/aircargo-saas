<template>
  <div class="warehouse-container">
    <h2>Registro de Recibo de Bodega</h2>
    <form @submit.prevent="handleSubmit">
      <input v-model="form.mawbNumber" placeholder="MAWB Number" required />
      <input v-model="form.shipperName" placeholder="Shipper Name" required />
      <button type="submit">Iniciar Proceso</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const form = ref({
  mawbNumber: '',
  shipperName: ''
});

const handleSubmit = () => {
  console.log('Formulario enviado:', form.value);
  alert('Formulario capturado en Vue.js');
};
</script>

<style scoped>
.warehouse-container {
  padding: 20px;
  background: #18181b;
  color: white;
  min-height: 100vh;
}
input {
  display: block;
  margin: 10px 0;
  padding: 10px;
  width: 100%;
  color: black;
}
</style>
