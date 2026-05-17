import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../stores/auth.js'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard/members',
    children: [
      {
        path: 'members',
        name: 'Members',
        component: () => import('../components/MemberTable.vue')
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('../components/LogTable.vue')
      },
      {
        path: 'account',
        name: 'Account',
        component: () => import('../components/AccountTable.vue')
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('../components/StaffTable.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录拦截
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && authStore.isLoggedIn) {
    next({ name: 'Members' })
  } else {
    next()
  }
})

export default router
