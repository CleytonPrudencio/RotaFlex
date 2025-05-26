<template lang="pug">
section.tela-inicial
  .conteudo
    h1 Novo sistema de Rotas - Em breve
    img.dashboard-img(src="@/assets/dashsemfundo.png", alt="Dashboard")

</template>

<script setup lang="ts">
// Lógica reativa ou composables, se precisar
</script>

<style scoped>
.tela-inicial {
  width: 100%; /* evita scroll lateral */
  min-height: 100vh; /* ocupa toda altura da viewport */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  text-align: center;
  overflow-x: hidden;
}

h1 {
  font-weight: 700; /* negrito */
  color: #000000; /* título preto (opcional, para garantir) */
  margin-bottom: 0.5rem;
}

p {
  font-weight: 400; /* normal */
  color: #000000; /* texto preto */
}

.conteudo {
  max-width: 600px;
  padding: 2rem;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center; /* centraliza conteúdo */
}

.dashboard-img {
  max-width: 150%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
