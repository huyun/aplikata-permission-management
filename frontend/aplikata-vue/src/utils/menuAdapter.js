// utils/menuAdapter.js
export const toPrimeMenuItem = (menuTree) => {
  return menuTree.map((item) => ({
    label: item.name,
    icon: `pi pi-${item.icon}` || undefined, // 假设 icon 字段存储图标名，如 'user' -> pi-user
    to: item.path,
    items: item.children ? toPrimeMenuItem(item.children) : undefined,
  }));
};
