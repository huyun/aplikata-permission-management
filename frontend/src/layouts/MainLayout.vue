<template>
  <div class="layout">
    <aside :class="['sidebar', { collapsed: isCollapsed }]">
      <Tree
        :value="treeData"
        :expanded-keys="expandedKeys"
        @toggle="onToggle"
        :pt="{
          root: { class: 'sidebar-tree' },
          node: { class: 'sidebar-tree-node' },
        }"
      >
        <template #default="slotProps">
          <div class="tree-node" @click="handleNodeClick(slotProps.node)">
            <span class="tree-node-label">{{ slotProps.node.label }}</span>
          </div>
        </template>
      </Tree>
    </aside>

    <div class="main">
      <div class="header">
        <div class="header-left">
          <Button :icon="isCollapsed ? 'pi pi-chevron-right' : 'pi pi-chevron-left'" text @click="toggleSidebar" />
        </div>
        <div class="header-right">
          <Button :label="authStore.user?.username || 'User'" icon="pi pi-user" text @click="toggleUserMenu" aria-haspopup="true" />
          <Menu ref="userMenuRef" :model="userMenuItems" popup />
        </div>
      </div>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue/usetoast';
import Tree from 'primevue/tree';
import Button from 'primevue/button';
import Menu from 'primevue/menu';

import { onMounted } from 'vue';

onMounted(async () => {
  if (authStore.token && (!authStore.menus || authStore.menus.length === 0)) {
    await authStore.fetchUserMenus();
  }

  // 默认展开第一个根节点
  if (treeData.value.length > 0) {
    const firstRootKey = treeData.value[0].key;
    expandedKeys.value = { [firstRootKey]: true };
  }
});

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const toast = useToast();

const isCollapsed = ref(false);
const userMenuRef = ref(null);
// 默认展开的节点键值（可设为第一级或全部）
const expandedKeys = ref({});

// 用户下拉菜单项
const userMenuItems = computed(() => [
  {
    label: 'Profile',
    icon: 'pi pi-user',
    command: () => router.push('/profile'),
  },
  {
    label: 'Change Password',
    icon: 'pi pi-key',
    command: () => router.push('/change-password'),
  },
  {
    separator: true,
  },
  {
    label: 'Logout',
    icon: 'pi pi-sign-out',
    command: () => handleLogout(),
  },
]);

// 将后端菜单树转换为 Tree 组件所需的数据结构
// 每个节点包含 key（唯一标识）、label（显示文本）、icon（图标类）、data（原始数据）、children
const buildTreeData = (menus) => {
  if (!menus) return [];
  return menus.map((menu) => ({
    key: menu.id.toString(),
    label: menu.name,
    icon: menu.icon ? `pi pi-${menu.icon}` : undefined,
    data: menu, // 保存原始数据，用于路由跳转
    children: menu.children ? buildTreeData(menu.children) : undefined,
  }));
};

const treeData = computed(() => {
  const currentPath = route.path;
  return buildTreeData(authStore.menus, currentPath);
});

// 自动展开第一个根节点
watch(
  treeData,
  (newVal) => {
    if (newVal && newVal.length > 0 && Object.keys(expandedKeys.value).length === 0) {
      const firstRootKey = newVal[0].key;
      expandedKeys.value = { [firstRootKey]: true };
    }
  },
  { immediate: true },
);

// 树节点展开/折叠时更新 expandedKeys
const onToggle = (event) => {
  expandedKeys.value = event.value;
};

// 点击树节点处理路由跳转
const handleNodeClick = (node) => {
  if (node.children && node.children.length > 0) {
    return;
  }
  const menu = node.data;
  if (menu.path) {
    router.push(menu.path);
  }
};

// 折叠侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};

// 显示用户下拉菜单
const toggleUserMenu = (event) => {
  userMenuRef.value.toggle(event);
};

// 退出登录
const handleLogout = () => {
  authStore.logout();
  toast.add({ severity: 'success', summary: 'Logged Out', detail: 'You have been logged out', life: 3000 });
  router.push('/login');
};
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 侧边栏整体容器 */
.sidebar {
  width: 250px;
  background-color: #f5f7fa; /* 浅色背景 */
  color: #2c3e50; /* 深色文字 */
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
  border-right: 1px solid #e2e8f0; /* 增加右边框，使侧边栏与内容区有界限 */
}

.sidebar.collapsed {
  width: 64px;
}

/* Tree 组件样式覆盖 */
:deep(.sidebar .p-tree) {
  background-color: transparent;
  border: none;
  width: 100%;
}

:deep(.sidebar .p-tree .p-treenode-label) {
  color: #2c3e50;
  padding: 0.5rem 0.75rem;
}

:deep(.sidebar .p-tree .p-treenode-icon) {
  margin-right: 0.5rem;
  color: #2c3e50;
}

:deep(.sidebar .p-tree .p-treenode-content:hover) {
  background-color: #e4e7ed;
}

:deep(.sidebar .p-tree .p-treenode-content.p-highlight) {
  background-color: #ecf5ff;
  color: #409eff;
  border-left: 3px solid #409eff;
}

:deep(.sidebar .p-tree .p-treenode-content.p-highlight .p-treenode-label) {
  color: #409eff;
}

:deep(.sidebar .p-tree .p-treenode-content.p-highlight .p-treenode-icon) {
  color: #409eff;
}

/* 折叠时隐藏文本，仅显示图标 */
.sidebar.collapsed :deep(.p-tree .p-treenode-label) {
  display: none;
}

.sidebar.collapsed :deep(.p-tree .p-treenode-icon) {
  margin-right: 0;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f4f6f9;
}

.header {
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.tree-node {
  cursor: pointer;
}

.tree-node:hover {
  background-color: #e4e7ed;
}
</style>
