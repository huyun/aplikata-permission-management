import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  {
    path: '/system',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: 'menu', component: () => import('@/views/admin/menuList.vue') }, // 对应 /system/menu
      { path: 'role', component: () => import('@/views/admin/RoleList.vue') }, // 对应 /system/role
      { path: 'user', component: () => import('@/views/admin/UserList.vue') }, // 对应 /system/user
      { path: 'project', component: () => import('@/views/admin/ProjectList.vue') }, // 对应 /system/project
      { path: 'domain', component: () => import('@/views/admin/DomainList.vue') }, // 对应 /system/domain
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '/dashboard', component: () => import('@/views/Dashboard.vue'), meta: { requiresAuth: true } },
      { path: '/profile', component: () => import('@/views/user/Profile.vue'), meta: { requiresAuth: true } },
      { path: '/changePassword', component: () => import('@/views/user/ChangePassword.vue'), meta: { requiresAuth: true } },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局前置守卫
router.beforeEach(async (to, from) => {
  const authStore = useAuthStore();
  const token = localStorage.getItem('token'); // 或者 authStore.token

  // 如果访问的是登录或注册页，放行
  if (to.path === '/login' || to.path === '/register') {
    return true;
  }

  // 检查是否已认证
  if (!token) {
    return '/login'; // 重定向到登录页
  }

  // 已认证，但根路径需要动态重定向
  if (to.path === '/') {
    // 如果菜单数据还未加载，先加载
    if (!authStore.menus || authStore.menus.length === 0) {
      try {
        await authStore.fetchUserMenus();
      } catch (err) {
        return '/dashboard'; // 降级处理
      }
    }
    const firstPath = authStore.getFirstMenuPath();

    return firstPath || '/dashboard';
  }

  return true; // 其他情况正常导航
});

export default router;
