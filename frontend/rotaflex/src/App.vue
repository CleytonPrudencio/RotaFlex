<template lang="pug">
div.app
  Menu
  main
    RouterView
  Footer
</template>

<script setup lang="ts">
import Menu from './components/Menu.vue'
import Footer from './components/Footer.vue'
import { RouterView } from 'vue-router'
</script>

<style scoped>
main {
  flex: 1 1 auto; /* flex-grow para ocupar espaço */
  width: 100%;
  margin: 0;
  padding: 0;
}

main {
  flex: 1;
  width: 100%; /* Garante largura cheia */
  margin: 0;
  padding: 0;
}
</style>
