<template>
  <div class="menu-container">
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <span>Menu Management</span>
          <Button label="Add Root Menu" icon="pi pi-plus" @click="handleAddRoot" />
        </div>
      </template>
      <template #content>
        <div class="toolbar">
          <Select v-model="selectedProjectId" :options="projectOptions" clearable optionLabel="name" optionValue="id" placeholder="Select Project" class="w-48" @change="onProjectChange" />

          <Select v-if="selectedProjectId !== null" v-model="selectedDomainId" :options="domains" optionLabel="name" optionValue="id" placeholder="Select Domain (optional)" class="w-48 ml-2" clearable @change="onDomainChange" />

          <Button label="Refresh" icon="pi pi-refresh" class="ml-2" @click="fetchData" />

          <Button label="Import" icon="pi pi-upload" @click="handleImport" />
        </div>

        <TreeTable :value="menuTree" class="w-full" :scrollable="true" scrollHeight="flex" showGridlines stripedRows selectionMode="single">
          <Column field="name" header="Menu Name" expander>
            <template #body="{ node }">
              <span>{{ node.name }}</span>
            </template>
          </Column>
          <Column field="path" header="Path">
            <template #body="{ node }">
              <span>{{ node.path }}</span>
            </template>
          </Column>
          <Column field="component" header="Component">
            <template #body="{ node }">
              <span>{{ node.component }}</span>
            </template>
          </Column>
          <Column header="Icon" style="width: 80px">
            <template #body="{ node }">
              <i v-if="node.icon" :class="getIconClass(node.icon)" style="font-size: 1.2rem"></i>
              <span v-else>-</span>
            </template>
          </Column>
          <Column field="sortOrder" header="Sort" style="width: 80px">
            <template #body="{ node }">
              <span>{{ node.sortOrder }}</span>
            </template>
          </Column>

          <!-- 新增项目列 -->
          <Column header="Project" style="width: 120px">
            <template #body="{ node }">
              <span v-if="node.projectId === null">Global</span>
              <span v-else>{{ getProjectName(node.projectId) }}</span>
            </template>
          </Column>

          <Column header="Domain" style="width: 120px">
            <template #body="{ node }">
              <span v-if="node.domainId === null && node.projectId !== null">Project Level</span>
              <span v-else-if="node.projectId === null">Global</span>
              <span v-else>{{ getDomainName(node.domainId) }}</span>
            </template>
          </Column>
          <Column header="Actions" class="w-3">
            <template #body="{ node }">
              <div class="flex gap-2 justify-content-between w-full">
                <div class="flex gap-4">
                  <Button size="small" severity="success" label="Edit" icon="pi pi-pencil" @click="handleEdit(node)" />
                  <Button size="small" severity="info" label="Add Child" icon="pi pi-plus" @click="handleAddChild(node)" />
                </div>
                <Button size="small" label="Delete" icon="pi pi-trash" severity="danger" @click="handleDelete(node)" class="p-button-outlined" />
              </div>
            </template>
          </Column>
        </TreeTable>
      </template>
    </Card>

    <!-- Add/Edit Dialog -->
    <Dialog v-model:visible="dialogVisible" :header="dialogTitle" :modal="true" :style="{ width: '600px' }">
      <form @submit.prevent="submitForm">
        <div class="p-fluid">
          <div class="field">
            <label for="project">Project</label>
            <div v-if="!currentEditId">
              <Select id="project" v-model="form.projectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Select project (or Global)" clearable @change="onProjectChangeForForm" class="w-full" />
            </div>
            <div v-else>
              <InputText :value="getProjectName(form.projectId)" disabled class="w-full" />
            </div>
          </div>

          <!-- 域字段（仅当项目非全局时显示） -->
          <div class="field" v-if="form.projectId && form.projectId !== 'global'">
            <label for="domainId">Domain (optional)</label>
            <Select id="domainId" v-model="form.domainId" :options="domainOptions" optionLabel="name" optionValue="id" placeholder="Leave blank for project level" clearable class="w-full" />
          </div>

          <!-- 父菜单 -->
          <div class="field">
            <label for="parentId">Parent Menu</label>
            <Select id="parentId" v-model="form.parentId" :options="parentOptions" optionLabel="name" optionValue="id" placeholder="Select parent menu (leave empty for root)" clearable class="w-full" />
          </div>

          <!-- 菜单名称 -->
          <div class="field">
            <label for="name">Menu Name</label>
            <InputText id="name" v-model="form.name" autocomplete="off" required class="w-full" />
          </div>

          <!-- 路径 -->
          <div class="field">
            <label for="path">Path</label>
            <InputText id="path" v-model="form.path" placeholder="e.g. /system/user" required class="w-full" />
          </div>

          <!-- 组件 -->
          <div class="field">
            <label for="component">Component</label>
            <InputText id="component" v-model="form.component" placeholder="e.g. UserList" required class="w-full" />
          </div>

          <!-- 图标 -->
          <div class="field">
            <label for="icon">Icon</label>
            <div class="flex align-items-center gap-2">
              <InputText id="icon" v-model="form.icon" placeholder="e.g. user, cog, home" class="flex-1" />
              <i v-if="form.icon" :class="getIconClass(form.icon)" style="font-size: 1.2rem;"></i>
            </div>
          </div>

          <!-- 排序 -->
          <div class="field">
            <label for="sortOrder">Sort Order</label>
            <InputNumber id="sortOrder" v-model="form.sortOrder" :min="0" class="w-full" />
          </div>
        </div>

        <div class="flex col-12 justify-content-between gap-2">
          <Button label="Cancel" icon="pi pi-times" @click="dialogVisible = false" severity="danger" class="p-button-outlined" />
          <Button type="submit" label="Confirm" :loading="submitting" icon="pi pi-save" />
        </div>

      </form>
    </Dialog>

    <!-- 导入对话框 -->
    <Dialog v-model:visible="importDialogVisible" header="Import Menus" :modal="true" :style="{ width: '500px' }">
      <div class="p-fluid">
        <div class="field">
          <label for="importProject">Project *</label>
          <Select id="importProject" v-model="importForm.projectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Select project (or Global)" @change="onImportProjectChange" class="w-full" />
        </div>
        <div class="field">
          <label for="importDomain">Domain (optional)</label>
          <Select id="importDomain" v-model="importForm.domainId" :options="domainOptions" optionLabel="name" optionValue="id" placeholder="Select domain (optional)" clearable :disabled="!importForm.projectId || importForm.projectId === null" class="w-full" />
        </div>
        <div class="field">
          <label for="menuFile">Menu JSON File</label>
          <FileUpload mode="basic" accept=".json" :auto="false" chooseLabel="Choose File" @select="onFileSelect" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-content-between gap-2 w-full">
          <Button label="Cancel" icon="pi pi-times" severity="danger" class="p-button-outlined" @click="importDialogVisible = false" />
          <Button label="Upload" icon="pi pi-upload" @click="uploadMenus" :loading="uploading" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import api from '@/api';
import Card from 'primevue/card';
import Button from 'primevue/button';
import Select from 'primevue/select';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Dialog from 'primevue/dialog';
import TreeTable from 'primevue/treetable';
import Column from 'primevue/column';
import FileUpload from 'primevue/fileupload';

import { useDeleteConfirm } from '@/composables/useDeleteConfirm'


const toast = useToast();
const { confirmDelete } = useDeleteConfirm()

// ---------- 辅助函数 ----------
const getIconClass = (iconName) => {
  if (!iconName) return '';
  iconName = iconName.toLowerCase();
  return iconName.startsWith('pi-') ? iconName : `pi pi-${iconName}`;
};

// ---------- 数据 ----------
const projects = ref([]);
const domains = ref([]);
const selectedProjectId = ref(null);
const selectedDomainId = ref(null);
const menuTree = ref([]);
const loading = ref(false);

const dialogVisible = ref(false);
const dialogTitle = ref('Add Menu');
const submitting = ref(false);
const currentEditId = ref(null);

const form = reactive({
  parentId: null,
  name: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  projectId: null,
  domainId: null,
});

// 项目选项（添加全局选项）
const projectOptions = computed(() => {
  return [{ id: null, name: 'Global (all projects)' }, ...projects.value]
})

// 域选项（根据选中的项目过滤）
const domainOptions = computed(() => {
  if (!form.projectId) return []
  return domains.value.filter(d => d.projectId === form.projectId)
})

// 父菜单选项（用于对话框）
const parentOptions = computed(() => {
  const flatten = (nodes, prefix = '') => {
    if (!Array.isArray(nodes)) return [];
    const result = [];
    for (const node of nodes) {
      if (!node || typeof node !== 'object') continue;
      const nodeName = node.name?.trim() || 'Unnamed';
      result.push({ id: node.id, name: prefix + nodeName, path: node.path });
      if (Array.isArray(node.children) && node.children.length) {
        result.push(...flatten(node.children, prefix + '  '));
      }
    }
    return result;
  };
  return flatten(menuTree.value);
});

const getProjectName = (projectId) => {
  if (projectId === null) return 'Global'
  const project = projects.value.find(p => p.id === projectId)
  return project ? project.name : ''
}

const getDomainName = (domainId) => {
  const domain = domains.value.find((d) => d.id === domainId);
  return domain ? domain.name : '';
};

// API 调用
const fetchProjects = async () => {
  try {
    const data = await api.get('/projects')
    projects.value = data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects', life: 3000 })
  }
}

const fetchDomains = async (projectId) => {
  if (!projectId) return;
  try {
    const data = await api.get(`/domains/by-project/${projectId}`);
    domains.value = data;
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load domains', life: 3000 });
  }
};

const fetchData = async () => {
  if (selectedProjectId.value === undefined) return;
  loading.value = true;
  try {
    const params = {};
    if (selectedProjectId.value !== null) {
      params.projectId = selectedProjectId.value;
    }
    if (selectedDomainId.value !== null) {
      params.domainId = selectedDomainId.value;
    }
    const data = await api.get('/menus/tree', { params });
    menuTree.value = data || [];
  } catch (err) {
    menuTree.value = [];
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load menu tree', life: 3000 });
  } finally {
    loading.value = false;
  }
};

const onProjectChange = async () => {
  selectedDomainId.value = null
  if (selectedProjectId.value) {
    await fetchDomains(selectedProjectId.value)
  } else {
    domains.value = []
  }
  fetchData()
}

// 表单内项目变化时，清空域选择并重新加载域列表
const onProjectChangeForForm = () => {
  form.domainId = null
  if (form.projectId) {
    fetchDomains(form.projectId)
  }
}

const onDomainChange = () => {
  fetchData();
};


const resetForm = () => {
  form.projectId = selectedProjectId.value || null
  form.domainId = null
  form.parentId = null
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sortOrder = 0
  currentEditId.value = null
};

const handleAddRoot = () => {
  if (!selectedProjectId.value && selectedProjectId.value !== null) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select a project first', life: 3000 })
    return
  }
  resetForm()
  dialogTitle.value = 'Add Root Menu'
  dialogVisible.value = true
};

const handleAddChild = (parentNode) => {
  if (!selectedProjectId.value && selectedProjectId.value !== null) {
    toast.add({ severity: 'warn', summary: 'Warning', detail: 'Please select a project first', life: 3000 })
    return
  }
  resetForm()
  dialogTitle.value = 'Add Child Menu'
  form.parentId = parentNode.id
  form.projectId = parentNode.projectId
  form.domainId = parentNode.domainId || null
  dialogVisible.value = true
};

const handleEdit = (node) => {
  resetForm()
  dialogTitle.value = 'Edit Menu'
  currentEditId.value = node.id
  form.projectId = node.projectId
  form.domainId = node.domainId
  form.parentId = node.parentId === 0 ? null : node.parentId
  form.name = node.name
  form.path = node.path
  form.component = node.component
  form.icon = node.icon
  form.sortOrder = node.sortOrder
  if (form.projectId) {
    fetchDomains(form.projectId)
  }
  dialogVisible.value = true
};

const handleDelete = (node) => {
  confirmDelete(node.name, () => api.delete(`/menus/${node.id}`), {
    onSuccess: () => {
      fetchData()
    }
  })
};

const submitForm = async () => {
  if (form.domainId && !form.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Please select a project when domain is chosen', life: 3000 })
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...form,
      parentId: form.parentId || 0,
      projectId: form.projectId === null ? null : form.projectId,
      domainId: form.domainId || null
    }
    if (currentEditId.value) {
      await api.put(`/menus/${currentEditId.value}`, payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Menu updated', life: 3000 })
    } else {
      await api.post('/menus', payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Menu added', life: 3000 })
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Operation failed'
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 })
  } finally {
    submitting.value = false
  }
};

// 导入相关状态
const importDialogVisible = ref(false);
const importForm = reactive({
  projectId: null,
  domainId: null,
  file: null
});
const uploading = ref(false);

const onImportProjectChange = () => {
  importForm.domainId = null;
  if (importForm.projectId && importForm.projectId !== null) {
    fetchDomains(importForm.projectId);
  } else {
    domains.value = []; // 清空域列表
  }
};

// 文件选择
const onFileSelect = (event) => {
  importForm.file = event.files[0];
};

// 打开导入对话框
const handleImport = () => {
  importForm.projectId = null;
  importForm.domainId = null;
  importForm.file = null;
  importDialogVisible.value = true;
};

// 上传菜单
const uploadMenus = async () => {
  if (!importForm.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Project is required', life: 3000 });
    return;
  }
  if (!importForm.file) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Please select a JSON file', life: 3000 });
    return;
  }
  // 如果选了域但项目为空，不允许（实际上项目不会为空，因为上面已校验）
  if (importForm.domainId && !importForm.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Cannot select domain without project', life: 3000 });
    return;
  }
  // 如果项目是 Global（null）却选了域，不允许
  if (importForm.projectId === null && importForm.domainId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Cannot select domain for global menu', life: 3000 });
    return;
  }

  const formData = new FormData();
  formData.append('file', importForm.file);
  formData.append('projectId', importForm.projectId);
  if (importForm.domainId) {
    formData.append('domainId', importForm.domainId);
  }
  uploading.value = true;
  try {
    await api.post('/menus/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    toast.add({ severity: 'success', summary: 'Success', detail: 'Menus imported successfully', life: 3000 });
    importDialogVisible.value = false;
    fetchData(); // 刷新菜单树
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: err.message || 'Import failed', life: 3000 });
  } finally {
    uploading.value = false;
  }
};

onMounted(async () => {
  await fetchProjects()
  fetchData()

});
</script>

<style scoped>
.menu-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-text {
  color: #909399;
  font-size: 12px;
}
</style>
