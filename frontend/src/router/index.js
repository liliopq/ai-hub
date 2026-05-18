import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, isAdmin } from '../utils/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('../views/post/PostDetail.vue')
  },
  {
    path: '/post/create',
    name: 'CreatePost',
    component: () => import('../views/post/CreatePost.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/post/edit/:id',
    name: 'EditPost',
    component: () => import('../views/post/EditPost.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/user/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/:userId',
    name: 'UserProfile',
    component: () => import('../views/user/UserProfile.vue')
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('../views/ai/AIChat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/notification/Notifications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/AdminPanel.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isLoggedIn()) {
    next('/login')
    return
  }
  if (to.meta.requiresAdmin && !isAdmin()) {
    next('/')
    return
  }
  next()
})

export default router
