import { defineStore } from 'pinia';
import api from '../api';
import { ref } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  // state
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'));
  const token = ref(localStorage.getItem('token') || '');
  const menus = ref([]);
  const isLoading = ref(false);
  const error = ref(null);

  const menusLoading = ref(false);

  async function refreshUserMenus() {
    try {
      const data = await api.get('/menus/user');
      menus.value = data;
      return data;
    } catch (err) {
      console.error('Failed to refresh menus:', err);
      throw err;
    }
  }

  // actions
  async function login(credentials) {
    isLoading.value = true;
    error.value = null;
    try {
      const data = await api.post('/auth/login', credentials);

      // data 已经是后端的 data 部分，假设包含 token 和 user
      user.value = data.user;
      token.value = data.token;
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user)); // 保存用户信息
      await fetchUserMenus();

      return { success: true };
    } catch (err) {
      error.value = err.message || '登录失败';
      return { success: false, message: error.value };
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchUserMenus() {
    menusLoading.value = true;
    try {
      const data = await api.get('/menus/user');
      menus.value = data;
    } catch (err) {
      menus.value = [];
    } finally {
      menusLoading.value = false;
    }
  }

  // 获取第一个可访问菜单的路径
  function getFirstMenuPath() {
    // 从菜单树中递归查找第一个叶子节点（或第一个非隐藏菜单）
    const findFirst = (items) => {
      for (const item of items) {
        if (item.hidden) continue;
        if (item.children && item.children.length > 0) {
          const childPath = findFirst(item.children);
          if (childPath) return childPath;
        } else if (item.path) {
          return item.path;
        }
      }
      return null;
    };
    return findFirst(menus.value) || '/dashboard'; // fallback
  }

  async function register(userData) {
    isLoading.value = true;
    error.value = null;
    try {
      await api.post('/auth/register', userData);
      return { success: true };
    } catch (err) {
      error.value = err.message || '注册失败';
      return { success: false, message: error.value };
    } finally {
      isLoading.value = false;
    }
  }

  function logout() {
    user.value = null;
    token.value = '';
    menus.value = [];
    localStorage.removeItem('token');
    localStorage.removeItem('user'); // 同时清除
  }

  return { user, token, menus, menusLoading, isLoading, error, login, register, logout, fetchUserMenus, getFirstMenuPath, refreshUserMenus };
});
