<template>
  <div class="user-container">
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <span>User Management</span>
          <Button label="Add User" icon="pi pi-plus" @click="handleAdd" />
        </div>
      </template>
      <template #content>
        <div class="toolbar">
          <Select v-model="selectedProjectId" :options="projects" optionLabel="name" optionValue="id" placeholder="Filter by Project" clearable class="w-48" @change="onProjectChange" />
          <Select v-if="selectedProjectId" v-model="selectedDomainId" :options="domains" optionLabel="name" optionValue="id" placeholder="Filter by Domain" clearable class="w-48 ml-2" @change="fetchData" />
          <Button label="Refresh" icon="pi pi-refresh" @click="fetchData" class="ml-2" />
        </div>

        <DataTable :value="userList" :loading="loading" class="w-full" stripedRows showGridlines paginator :rows="10" :rowsPerPageOptions="[5, 10, 20]">
          <Column field="id" header="ID" style="width: 80px" />
          <Column field="username" header="Username" />
          <Column field="email" header="Email" />
          <Column header="Project">
            <template #body="{ data }">
              <span v-if="data.projectId === null">-</span>
              <span v-else>{{ getProjectName(data.projectId) }}</span>
            </template>
          </Column>
          <Column header="Domain">
            <template #body="{ data }">
              <span v-if="data.domainId === null">-</span>
              <span v-else>{{ getDomainName(data.domainId) }}</span>
            </template>
          </Column>
          <Column header="Roles">
            <template #body="{ data }">
              <span v-for="role in data.roleNames" :key="role" class="role-badge">
                {{ role }}
              </span>
            </template>
          </Column>
          <Column header="Actions" style="width: 200px">
            <template #body="{ data }">
              <div class="flex gap-2">
                <Button icon="pi pi-pencil" text rounded @click="handleEdit(data)" />
                <Button icon="pi pi-users" text rounded @click="handleAssignRoles(data)" />
                <Button icon="pi pi-trash" text rounded severity="danger" @click="handleDelete(data)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Add/Edit User Dialog -->
    <Dialog v-model:visible="userDialogVisible" :header="userDialogTitle" :modal="true" :style="{ width: '500px' }">
      <form @submit.prevent="submitUserForm">
        <div class="p-fluid">
          <!-- 项目选择（可选） -->
          <div class="field">
            <label for="project">Project (optional)</label>
            <Select id="project" v-model="userForm.projectId" :options="projectOptions" optionLabel="name" optionValue="id" placeholder="Select project (or leave blank for super admin)" clearable @change="onUserFormProjectChange" class="w-full" />
          </div>

          <!-- 域选择（仅当项目非空时显示，且必填） -->
          <div class="field" v-if="userForm.projectId">
            <label for="domain">Domain *</label>
            <Select id="domain" v-model="userForm.domainId" :options="domainOptions" optionLabel="name" optionValue="id" placeholder="Select Domain" required class="w-full" />
          </div>

          <!-- 用户名（必填） -->
          <div class="field">
            <label for="username">Username</label>
            <InputText id="username" v-model="userForm.username" autocomplete="off" required class="w-full" />
          </div>

          <!-- 邮箱（必填） -->
          <div class="field">
            <label for="email">Email</label>
            <InputText id="email" v-model="userForm.email" autocomplete="off" type="email" required class="w-full" />
          </div>

          <!-- 密码（新增时必填，编辑时可选） -->
          <div class="field">
            <label for="password">Password</label>
            <InputText id="password" v-model="userForm.password" type="password" autocomplete="off" :required="!currentUserId" class="w-full" />
            <small v-if="currentUserId" class="text-400">Leave blank to keep current password</small>
          </div>
        </div>
        <div class="flex justify-content-between gap-2 mt-3">
          <Button label="Cancel" icon="pi pi-times" severity="danger" @click="userDialogVisible = false" class="p-button-outlined" />
          <Button type="submit" label="Confirm" :loading="submittingUser" />
        </div>
      </form>
    </Dialog>

    <!-- Assign Roles Dialog -->
    <Dialog v-model:visible="roleDialogVisible" header="Assign Roles" :modal="true" :style="{ width: '500px' }">
      <div class="p-fluid">
        <div class="field">
          <label>Roles</label>
          <Listbox v-model="selectedRoleIds" :options="availableRoles" optionLabel="name" optionValue="id" multiple class="w-full" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-content-between gap-2">
          <Button label="Cancel" icon="pi pi-times" severity="danger" text @click="roleDialogVisible = false" />
          <Button label="Save" icon="pi pi-check" @click="saveRoleAssignment" :loading="savingRoles" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useToast } from 'primevue/usetoast'
import Card from 'primevue/card'
import Button from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Listbox from 'primevue/listbox'
import api from '@/api'
import { useDeleteConfirm } from '@/composables/useDeleteConfirm'

const toast = useToast()
const { confirmDelete } = useDeleteConfirm()

// 数据
const projects = ref([])
const domains = ref([])
const selectedProjectId = ref(null)
const selectedDomainId = ref(null)
const userList = ref([])
const loading = ref(false)

// 用户表单
const userDialogVisible = ref(false)
const userDialogTitle = ref('Add User')
const submittingUser = ref(false)
const currentUserId = ref(null)
const userForm = reactive({
  projectId: null,
  domainId: null,
  username: '',
  email: '',
  password: ''
})

// 角色分配
const roleDialogVisible = ref(false)
const availableRoles = ref([])
const selectedRoleIds = ref([])
const savingRoles = ref(false)
const currentAssignUserId = ref(null)

// 项目选项（添加空选项）
const projectOptions = computed(() => {
  return [{ id: null, name: 'Super Admin (no project/domain)' }, ...projects.value]
})

// 计算属性：域选项（基于选中的项目）
const domainOptions = computed(() => {
  if (!userForm.projectId) return []
  return domains.value.filter(d => d.projectId === userForm.projectId)
})

// 获取项目列表
const fetchProjects = async () => {
  try {
    const data = await api.get('/projects')
    projects.value = data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects', life: 3000 })
  }
}

// 获取域列表（按项目）
const fetchDomains = async (projectId) => {
  if (!projectId) return
  try {
    const data = await api.get(`/domains/by-project/${projectId}`)
    domains.value = data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load domains', life: 3000 })
  }
}

// 获取用户列表（可筛选项目/域）
const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedDomainId.value) {
      params.domainId = selectedDomainId.value
    } else if (selectedProjectId.value) {
      params.projectId = selectedProjectId.value
    }
    const data = await api.get('/users', { params })
    userList.value = data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load users', life: 3000 })
  } finally {
    loading.value = false
  }
}

// 项目筛选变化
const onProjectChange = async () => {
  selectedDomainId.value = null
  if (selectedProjectId.value) {
    await fetchDomains(selectedProjectId.value)
  } else {
    domains.value = []
  }
  fetchData()
}

// 获取项目名称（用于表格显示）
const getProjectName = (projectId) => {
  const project = projects.value.find(p => p.id === projectId)
  return project ? project.name : ''
}

// 获取域名称（用于表格显示）
const getDomainName = (domainId) => {
  const domain = domains.value.find(d => d.id === domainId)
  return domain ? domain.name : ''
}

// 重置用户表单
const resetUserForm = () => {
  userForm.projectId = null
  userForm.domainId = null
  userForm.username = ''
  userForm.email = ''
  userForm.password = ''
  currentUserId.value = null
}

// 新增用户
const handleAdd = () => {
  resetUserForm()
  userDialogTitle.value = 'Add User'
  userDialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  resetUserForm()
  userDialogTitle.value = 'Edit User'
  currentUserId.value = row.id
  userForm.projectId = row.projectId
  userForm.domainId = row.domainId
  userForm.username = row.username
  userForm.email = row.email
  // 如果编辑的是普通用户，需要加载对应的域列表
  if (row.projectId) {
    fetchDomains(row.projectId)
  }
  userDialogVisible.value = true
}

// 删除用户（使用通用确认对话框）
const handleDelete = (row) => {
  confirmDelete(row.username, () => api.delete(`/users/${row.id}`), {
    successMessage: 'User deleted successfully',
    errorMessage: 'Failed to delete user'
  })
}

// 表单内项目变化时，清空域并加载域列表
const onUserFormProjectChange = () => {
  userForm.domainId = null
  if (userForm.projectId) {
    fetchDomains(userForm.projectId)
  }
}

// 提交用户表单（浏览器原生校验 + 业务规则）
const submitUserForm = async () => {
  // 业务规则校验
  if (userForm.projectId && !userForm.domainId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Domain is required when project is selected', life: 3000 })
    return
  }
  if (!userForm.projectId && userForm.domainId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Cannot select domain without project', life: 3000 })
    return
  }

  submittingUser.value = true
  try {
    const payload = {
      projectId: userForm.projectId || null,
      domainId: userForm.domainId || null,
      username: userForm.username,
      email: userForm.email,
      password: userForm.password || undefined
    }
    if (currentUserId.value) {
      await api.put(`/users/${currentUserId.value}`, payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'User updated', life: 3000 })
    } else {
      await api.post('/users', payload)
      toast.add({ severity: 'success', summary: 'Success', detail: 'User added', life: 3000 })
    }
    userDialogVisible.value = false
    fetchData()
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Operation failed'
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 })
  } finally {
    submittingUser.value = false
  }
}

// 获取可用角色列表（可以根据项目或域过滤，这里简单获取所有）
const fetchAvailableRoles = async () => {
  try {
    const data = await api.get('/roles')
    availableRoles.value = data
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load roles', life: 3000 })
  }
}

// 打开分配角色对话框
const handleAssignRoles = async (row) => {
  currentAssignUserId.value = row.id
  // 获取该用户已有的角色ID列表
  try {
    const roleIds = await api.get(`/users/${row.id}/roles`)
    selectedRoleIds.value = roleIds
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load user roles', life: 3000 })
    return
  }
  await fetchAvailableRoles()
  roleDialogVisible.value = true
}

// 保存角色分配
const saveRoleAssignment = async () => {
  savingRoles.value = true
  try {
    await api.put(`/users/${currentAssignUserId.value}/roles`, selectedRoleIds.value)
    toast.add({ severity: 'success', summary: 'Success', detail: 'Roles assigned successfully', life: 3000 })
    roleDialogVisible.value = false
    fetchData() // 刷新列表显示新角色
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Failed to assign roles'
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 })
  } finally {
    savingRoles.value = false
  }
}

onMounted(async () => {
  await fetchProjects()
  fetchData()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.role-badge {
  display: inline-block;
  background-color: #e9ecef;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  margin-right: 5px;
  font-size: 0.75rem;
}

.field {
  margin-bottom: 1rem;
}
</style>