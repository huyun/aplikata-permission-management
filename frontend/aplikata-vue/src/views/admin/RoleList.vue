<template>
  <div class="role-container">
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <span>Role Management</span>
          <div>
            <Button label="Add Role" icon="pi pi-plus" @click="handleAdd" />
          </div>
        </div>
      </template>
      <template #content>
        <div class="toolbar">
          <Select v-model="selectedProjectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Filter by Project" clearable class="w-48" @change="onProjectChange" />
          <Select v-if="selectedProjectId" v-model="selectedDomainId" :options="domains" optionLabel="name" optionValue="id" placeholder="Filter by Domain" clearable class="w-48 ml-2" @change="fetchData" />
          <Button label="Import" icon="pi pi-upload" class="mr-2" @click="handleImport" />
        </div>

        <DataTable :value="roleList" :loading="loading" class="w-full" stripedRows showGridlines>
          <Column field="id" header="ID" style="width: 80px" />
          <Column field="name" header="Role Name" />
          <Column field="description" header="Description" />
          <Column header="Project">
            <template #body="{ data }">
              {{ getProjectName(data.projectId) }}
            </template>
          </Column>
          <Column header="Domain">
            <template #body="{ data }">
              <span v-if="data.domainId === null">Global (Project Level)</span>
              <span v-else>{{ getDomainName(data.domainId) }}</span>
            </template>
          </Column>
          <Column header="Actions" style="width: 200px">
            <template #body="{ data }">
              <div class="flex gap-2">
                <Button icon="pi pi-pencil" text rounded @click="handleEdit(data)" />
                <Button icon="pi pi-lock" text rounded @click="handleAssignMenus(data)" />
                <Button icon="pi pi-trash" text rounded severity="danger" @click="handleDelete(data)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Add/Edit Role Dialog -->
    <Dialog v-model:visible="roleDialogVisible" :header="roleDialogTitle" :modal="true" :style="{ width: '500px' }">
      <form @submit.prevent="submitRoleForm">
        <div class="p-fluid">
          <div class="field">
            <label for="roleProject">Project (optional)</label>
            <Select class="w-full" id="roleProject" v-model="roleForm.projectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Select Project" :disabled="!!currentRoleId" clearable @change="onRoleProjectChange" />
          </div>

          <div class="field">
            <label for="roleDomain">Domain (optional)</label>
            <Select class="w-full" id="roleDomain" v-model="roleForm.domainId" :options="domainOptions" optionLabel="name" optionValue="id" placeholder="Leave blank for project-level role" clearable :disabled="!roleForm.projectId" />
          </div>
          <!-- 角色名称（必填） -->

          <div class="field">
            <label for="roleName">Role Name</label>
            <InputText class="w-full" id="roleName" v-model="roleForm.name" autocomplete="off" required />
          </div>

        </div>

        <div class="field">
          <label for="roleDescription">Description</label>
          <Textarea class="w-full" id="roleDescription" v-model="roleForm.description" rows="2" autoResize />
        </div>

        <div class="flex justify-content-between gap-2 mt-3">
          <Button label="Cancel" icon="pi pi-times" severity="danger" class="p-button-outlined" @click="roleDialogVisible = false" />
          <Button type="submit" label="Confirm" icon="pi pi-check" :loading="submittingRole" />
        </div>
      </form>
    </Dialog>

    <!-- Assign Menus Dialog (unchanged) -->
    <Dialog v-model:visible="menuDialogVisible" header="Assign Menus" :modal="true" :style="{ width: '500px' }" @hide="onMenuDialogClose">
      <div class="p-fluid">
        <Tree :value="menuTree" selectionMode="checkbox" v-model:selectionKeys="selectedMenuKeys" :expandedKeys="expandedKeys" @toggle="onTreeToggle" class="w-full" />
      </div>
      <template #footer>
        <div class="flex justify-content-between gap-2">
          <Button label="Cancel" icon="pi pi-times" severity="danger" text @click="menuDialogVisible = false" />
          <Button label="Save" icon="pi pi-check" @click="saveMenuAssign" :loading="savingMenus" />
        </div>
      </template>
    </Dialog>

    <!-- Import Dialog -->
    <Dialog v-model:visible="importDialogVisible" header="Import Roles" :modal="true" :style="{ width: '500px' }">
      <div class="p-fluid">
        <div class="field">
          <label for="importProject">Project *</label>
          <Select id="importProject" v-model="importForm.projectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Select project (or Global)" @change="onImportProjectChange" />
        </div>
        <div class="field">
          <label for="importDomain">Domain (optional)</label>
          <Select id="importDomain" v-model="importForm.domainId" :options="domainOptions" optionLabel="name" optionValue="id" placeholder="Select domain (leave blank for project level)" clearable :disabled="!importForm.projectId || importForm.projectId === null" />
        </div>
        <div class="field">
          <label for="roleFile">Role JSON File</label>
          <FileUpload mode="basic" accept=".json" :auto="false" chooseLabel="Choose File" @select="onFileSelect" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-content-between gap-2">
          <Button label="Cancel" icon="pi pi-times" severity="danger" text @click="importDialogVisible = false" />
          <Button label="Upload" icon="pi pi-upload" @click="uploadRoles" :loading="uploading" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { useToast } from "primevue/usetoast";
import Card from "primevue/card";
import Button from "primevue/button";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import Select from "primevue/select";
import Tree from "primevue/tree";
import FileUpload from 'primevue/fileupload';

import api from "@/api";

const toast = useToast();

// 角色表单数据（使用 reactive）
const roleForm = reactive({
  projectId: null,
  domainId: null,
  name: '',
  description: ''
})

// 项目、域数据
const projects = ref([]);
const domains = ref([]);
const selectedProjectId = ref(null);
const selectedDomainId = ref(null);

// 角色列表
const roleList = ref([]);
const loading = ref(false);

// 角色表单
const roleDialogVisible = ref(false);
const roleDialogTitle = ref("Add Role");
const submittingRole = ref(false);
const currentRoleId = ref(null);

// 菜单分配相关（保持不变）
const menuDialogVisible = ref(false);
const menuTree = ref([]);
const selectedMenuKeys = ref({});
const expandedKeys = ref({});
const savingMenus = ref(false);
const currentAssignRoleId = ref(null);

// 导入相关
const importDialogVisible = ref(false)
const importForm = reactive({
  projectId: null,
  domainId: null,
  file: null
})
const uploading = ref(false)

// 计算属性
const projectOptions = computed(() => {
  return [{ id: null, name: 'Global (all projects)' }, ...projects.value]
})

// 计算域选项（基于选中的项目）
const domainOptions = computed(() => {
  if (!roleForm.projectId) return [];
  return domains.value.filter((d) => d.projectId === roleForm.projectId);
});

// 获取项目列表
const fetchProjects = async () => {
  try {
    const data = await api.get("/projects");
    projects.value = data;
  } catch (err) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load projects",
      life: 3000,
    });
  }
};

// 获取域列表（按项目）
const fetchDomains = async (projectId) => {
  if (!projectId) return;
  try {
    const data = await api.get(`/domains/by-project/${projectId}`);
    domains.value = [{ id: null, name: "Global (all domains)" }, ...data];
  } catch (err) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load domains",
      life: 3000,
    });
  }
};

// 获取角色列表（按项目/域过滤）
const fetchData = async () => {
  loading.value = true;
  try {
    const params = {};
    if (selectedDomainId.value) {
      params.domainId = selectedDomainId.value;
    } else if (selectedProjectId.value) {
      params.projectId = selectedProjectId.value;
    } else {
      // 默认：只显示全局角色（project_id IS NULL）
      params.projectId = null;
    }
    const data = await api.get("/roles", { params });
    roleList.value = data;
  } catch (err) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load roles",
      life: 3000,
    });
  } finally {
    loading.value = false;
  }
};

// 项目变更时重新加载域列表和角色列表
const onProjectChange = async () => {
  selectedDomainId.value = null;
  if (selectedProjectId.value) {
    await fetchDomains(selectedProjectId.value);
  } else {
    domains.value = [];
  }
  fetchData();
};

// 获取项目名称（用于表格显示）
const getProjectName = (projectId) => {
  const project = projects.value.find((p) => p.id === projectId);
  return project ? project.name : "";
};

// 获取域名称（用于表格显示）
const getDomainName = (domainId) => {
  const domain = domains.value.find((d) => d.id === domainId);
  return domain ? domain.name : "";
};

// 重置角色表单
const resetRoleForm = () => {
  roleForm.projectId = selectedProjectId.value || null
  roleForm.domainId = null
  roleForm.name = ''
  roleForm.description = ''
  currentRoleId.value = null
  if (roleForm.projectId) {
    fetchDomains(roleForm.projectId)
  }
};

// 新增角色
const handleAdd = () => {
  resetRoleForm();
  roleDialogTitle.value = "Add Role";
  roleDialogVisible.value = true;
};

// 编辑角色
const handleEdit = (row) => {
  resetRoleForm();
  roleDialogTitle.value = "Edit Role";
  currentRoleId.value = row.id;
  roleForm.projectId = row.projectId;
  roleForm.domainId = row.domainId;
  roleForm.name = row.name;
  roleForm.description = row.description || "";
  // 编辑时加载对应的域列表
  if (roleForm.projectId) {
    fetchDomains(roleForm.projectId);
  }
  roleDialogVisible.value = true;
};

// 删除角色
const handleDelete = (row) => {
  if (confirm(`Are you sure to delete role "${row.name}"?`)) {
    api
      .delete(`/roles/${row.id}`)
      .then(() => {
        toast.add({
          severity: "success",
          summary: "Success",
          detail: "Role deleted",
          life: 3000,
        });
        fetchData();
      })
      .catch((err) => {
        toast.add({
          severity: "error",
          summary: "Error",
          detail: err.message || "Delete failed",
          life: 3000,
        });
      });
  }
};

// 提交表单（利用浏览器原生校验，无需手动 validate）
const submitRoleForm = async () => {
  // 业务规则：如果选择了域，则必须选择项目
  if (roleForm.domainId && !roleForm.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Please select a project when domain is chosen', life: 3000 })
    return
  }
  submittingRole.value = true
  try {
    const payload = {
      projectId: roleForm.projectId || null,
      domainId: roleForm.domainId || null,
      name: roleForm.name,
      description: roleForm.description
    }
    if (currentRoleId.value) {
      await api.put(`/roles/${currentRoleId.value}`, payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Role updated', life: 3000 })
    } else {
      await api.post('/roles', payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'Role added', life: 3000 })
    }
    roleDialogVisible.value = false
    fetchData()
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Operation failed'
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 })
  } finally {
    submittingRole.value = false
  }
}

// 获取菜单树（用于分配菜单）
const fetchMenuTree = async (projectId, domainId) => {
  try {
    const params = {};
    if (projectId !== null && projectId !== undefined) {
      params.projectId = projectId;
    }
    if (domainId !== null && domainId !== undefined) {
      params.domainId = domainId;
    }

    const data = await api.get("/menus/tree", { params });
    const convertToTreeNodes = (menus) => {
      if (!menus || !Array.isArray(menus)) return [];
      return menus.map((menu) => ({
        key: menu.id.toString(),
        label: menu.name,
        data: menu,
        children: menu.children ? convertToTreeNodes(menu.children) : undefined,
      }));
    };
    menuTree.value = convertToTreeNodes(data);
    // 展开所有节点
    const expandAll = (nodes) => {
      nodes.forEach((node) => {
        expandedKeys.value[node.key] = true;
        if (node.children && node.children.length) expandAll(node.children);
      });
    };
    expandAll(menuTree.value);
  } catch (err) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load menus",
      life: 3000,
    });
  }
};

// 打开分配菜单对话框
const handleAssignMenus = async (row) => {
  currentAssignRoleId.value = row.id;
  if (menuTree.value.length === 0) {
    await fetchMenuTree(row.projectId, row.domainId);
  }
  try {
    const menuIds = await api.get(`/roles/${row.id}/menus`);
    const keys = {};
    menuIds.forEach((id) => {
      keys[id.toString()] = { checked: true, partialChecked: false };
    });
    selectedMenuKeys.value = keys;
    menuDialogVisible.value = true;
  } catch (err) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load assigned menus",
      life: 3000,
    });
  }
};

// 树节点展开/折叠
const onTreeToggle = (event) => {
  expandedKeys.value = event.value;
};

const onMenuDialogClose = () => {
  menuTree.value = [];
  selectedMenuKeys.value = {};
};

// 保存菜单分配
const saveMenuAssign = async () => {
  savingMenus.value = true;
  try {
    const menuIds = Object.keys(selectedMenuKeys.value)
      .filter((key) => selectedMenuKeys.value[key]?.checked === true)
      .map((key) => parseInt(key, 10));
    await api.put(`/roles/${currentAssignRoleId.value}/menus`, menuIds);
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "Menus assigned successfully",
      life: 3000,
    });
    menuDialogVisible.value = false;
  } catch (err) {
    const errorMsg =
      err.response?.data?.message || err.message || "Failed to assign menus";
    toast.add({
      severity: "error",
      summary: "Error",
      detail: errorMsg,
      life: 3000,
    });
  } finally {
    savingMenus.value = false;
  }
};

// 角色表单项目变更时加载域列表
const onRoleProjectChange = () => {
  roleForm.domainId = null;
  if (roleForm.projectId) {
    fetchDomains(roleForm.projectId);
  }
};

// ---------- 导入角色 ----------
const onImportProjectChange = () => {
  importForm.domainId = null
  if (importForm.projectId && importForm.projectId !== null) {
    fetchDomains(importForm.projectId)
  } else {
    domains.value = []
  }
}

const onFileSelect = (event) => {
  importForm.file = event.files[0]
}

const handleImport = () => {
  importForm.projectId = null
  importForm.domainId = null
  importForm.file = null
  importDialogVisible.value = true
}

const uploadRoles = async () => {
  if (!importForm.projectId && importForm.projectId !== 0) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Project is required', life: 3000 })
    return
  }
  if (!importForm.file) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Please select a JSON file', life: 3000 })
    return
  }
  if (importForm.domainId && !importForm.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Cannot select domain without project', life: 3000 })
    return
  }
  if (importForm.projectId === null && importForm.domainId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Cannot select domain for global role', life: 3000 })
    return
  }
  const formData = new FormData()
  formData.append('file', importForm.file)
  formData.append('projectId', importForm.projectId)
  if (importForm.domainId) {
    formData.append('domainId', importForm.domainId)
  }
  uploading.value = true
  try {
    await api.post('/roles/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    toast.add({ severity: 'success', summary: 'Success', detail: 'Roles imported successfully', life: 3000 })
    importDialogVisible.value = false
    fetchData()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: err.message || 'Import failed', life: 3000 })
  } finally {
    uploading.value = false
  }
}

onMounted(async () => {
  await fetchProjects();
  fetchData();
});
</script>

<style scoped>
.role-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.field {
  margin-bottom: 1rem;
}

.p-message {
  margin-top: 0.25rem;
  padding: 0.25rem 0.5rem;
}
</style>
