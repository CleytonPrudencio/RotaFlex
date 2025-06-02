<template lang="pug">
header.navbar
  .navbar-content
    .logo
      img(src="@/assets/logosite.png", alt="Logo")
    nav.nav-links
      RouterLink(to="/", exact) Início
      RouterLink(to="/rota", exact) Rota e Abastecimento
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.navbar {
  background-color: #ffffff;
  border-bottom: 1px solid #eaeaea;
  padding: 0.5rem 2rem;
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo img {
  height: 50px;
  width: auto;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-links a.router-link-exact-active {
  color: #007bff;
  border-bottom: 2px solid #007bff;
}

.nav-links a:hover {
  color: #0056b3;
}

.logo img {
  height: 30px; /* aumentei de 50px para 80px */
  width: auto;
}
</style>
