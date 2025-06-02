import { createRouter, createWebHistory } from 'vue-router'
import TelaInicial from '../views/TelaInicial.vue'
import RotaAbastecimento from '../views/Rota/DetalhesRota.vue'
const routes = [
  {
    path: '/',
    name: 'TelaInicial',
    component: TelaInicial,
  },
  {
    path: '/rota',
    name: 'RotaAbastecimento',
    component: RotaAbastecimento,
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
