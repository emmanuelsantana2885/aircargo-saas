<template>
  <div class="warehouse-form-container">
    <h2 class="form-title">REGISTRO DE RECIBO DE BODEGA</h2>
    
    <form @submit.prevent="handleEmit" class="main-form">
      <div class="form-group">
        <label>MAWB Number</label>
        <input v-model="receipt.mawb" type="text" placeholder="Ej: 406-12345678" />
      </div>

      <div class="form-group">
        <label>Shipper / Remitente</label>
        <input v-model="receipt.shipper" type="text" />
      </div>

      <button type="submit" class="submit-btn">
        INICIAR PROCESO DE CUBICAJE
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const receipt = ref({
  mawb: '',
  shipper: ''
});

const handleEmit = () => {
  // Lógica de envío a tu API de Spring Boot
  console.log("Enviando a API:", receipt.value);
  alert("Datos listos para enviar al backend");
};
</script>

<style scoped>
.warehouse-form-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 30px;
  background: #18181b;
  border: 1px solid #3f3f46;
  border-radius: 8px;
  color: white;
}
.form-title {
  color: #f59e0b;
  font-weight: bold;
  margin-bottom: 20px;
  border-bottom: 1px solid #3f3f46;
  padding-bottom: 10px;
}
.form-group {
  margin-bottom: 15px;
}
input {
  width: 100%;
  padding: 10px;
  background: #09090b;
  border: 1px solid #52525b;
  color: white;
  margin-top: 5px;
}
.submit-btn {
  background: #d97706;
  color: #09090b;
  padding: 15px;
  width: 100%;
  font-weight: bold;
  cursor: pointer;
  border: none;
  margin-top: 20px;
}
</style>