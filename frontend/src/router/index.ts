import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView
    },
    {
      path: '/flights',
      name: 'flights',
      component: () => import('../views/FlightsView.vue')
    },
    {
      path: '/load-planning',
      name: 'load-planning',
      component: () => import('../views/LoadPlanningView.vue')
    },
    {
      path: '/ulds',
      name: 'ulds',
      component: () => import('../views/UldsView.vue')
    },
    {
      path: '/mawbs',
      name: 'mawbs',
      component: () => import('../views/MawbsView.vue')
    },
    {
      path: '/bookings',
      name: 'bookings',
      component: () => import('../views/BookingsView.vue')
    },
    {
      path: '/receipts',
      name: 'receipts',
      component: () => import('../views/WarehouseReceiptsView.vue')
    },
    // Redirección por si alguien entra a /home
    {
      path: '/home',
      redirect: '/'
    }
  ]
})

export default router