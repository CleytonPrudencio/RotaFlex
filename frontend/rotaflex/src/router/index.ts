import { createRouter, createWebHistory } from 'vue-router'
import TelaInicial from '../views/TelaInicial.vue'

const routes = [
  {
    path: '/',
    name: 'TelaInicial',
    component: TelaInicial,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
