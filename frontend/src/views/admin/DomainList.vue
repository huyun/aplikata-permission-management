<template>
  <div class="domain-container">
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <span>Domain Management</span>
          <Button label="Add Domain" icon="pi pi-plus" @click="handleAdd" />
        </div>
      </template>
      <template #content>
        <div class="toolbar">
          <Select v-model="selectedProjectId" :options="projects" optionLabel="name" optionValue="id" placeholder="Filter by Project" clearable class="w-48" @change="fetchData" />
          <Button label="Refresh" icon="pi pi-refresh" @click="fetchData" class="ml-2" />
        </div>

        <DataTable :value="domainList" :loading="loading" class="w-full" stripedRows showGridlines>
          <Column field="id" header="ID" style="width: 80px" />
          <Column field="name" header="Domain Name" />
          <Column header="Project">
            <template #body="slotProps">
              {{ getProjectName(slotProps.data.projectId) }}
            </template>
          </Column>
          <Column header="Actions" style="width: 150px">
            <template #body="slotProps">
              <div class="flex gap-2">
                <Button icon="pi pi-pencil" text rounded @click="handleEdit(slotProps.data)" />
                <Button icon="pi pi-trash" text rounded severity="danger" @click="handleDelete(slotProps.data)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Add/Edit Dialog -->
    <Dialog v-model:visible="dialogVisible" :header="dialogTitle" :modal="true" :style="{ width: '600px' }">
      <form @submit.prevent="submitForm">
        <div class="p-fluid">
          <div class="field">
            <label for="projectId">Project </label>

            <Select id="projectId" v-model="form.projectId" :options="projects" optionLabel="name" optionValue="id" placeholder="Select a project" :invalid="submitted && !form.projectId" class="w-full" required />
          </div>

          <div class="field">
            <label for="name">Domain Name</label>
            <InputText id="name" class="w-full" v-model="form.name" autocomplete="off" required />
          </div>
          <div class="flex col-12 justify-content-between gap-2">
            <Button label="Cancel" class="p-button-outlined" icon="pi pi-times" severity="danger" @click="dialogVisible = false" />
            <Button icon="pi pi-save" label="Confirm" type="submit" :loading="submitting" />
          </div>
        </div>
      </form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import api from '@/api';

const toast = useToast();

const projects = ref([]);
const domainList = ref([]);
const loading = ref(false);
const selectedProjectId = ref(null);

const dialogVisible = ref(false);
const dialogTitle = ref('Add Domain');
const submitting = ref(false);
const submitted = ref(false);
const currentId = ref(null);

const form = reactive({
  projectId: null,
  name: '',
});

const fetchProjects = async () => {
  try {
    const data = await api.get('/projects');
    projects.value = data;
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects', life: 3000 });
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    if (selectedProjectId.value) {
      const data = await api.get(`/domains/by-project/${selectedProjectId.value}`);
      domainList.value = data;
    } else {
      const data = await api.get('/domains');
      domainList.value = data;
    }
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load domains', life: 3000 });
  } finally {
    loading.value = false;
  }
};

const getProjectName = (projectId) => {
  const project = projects.value.find((p) => p.id === projectId);
  return project ? project.name : '';
};

const resetForm = () => {
  form.projectId = selectedProjectId.value || null;
  form.name = '';
  currentId.value = null;
};

const handleAdd = () => {
  resetForm();
  dialogTitle.value = 'Add Domain';
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  resetForm();
  dialogTitle.value = 'Edit Domain';
  currentId.value = row.id;
  form.projectId = row.projectId;
  form.name = row.name;
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  if (confirm(`Are you sure to delete domain "${row.name}"?`)) {
    api
      .delete(`/domains/${row.id}`)
      .then(() => {
        toast.add({ severity: 'success', summary: 'Success', detail: 'Domain deleted', life: 3000 });
        fetchData();
      })
      .catch((err) => {
        toast.add({ severity: 'error', summary: 'Error', detail: err.message || 'Delete failed', life: 3000 });
      });
  }
};

const submitForm = async () => {
  submitted.value = true
  // 手动校验项目选择器
  if (!form.projectId) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Project is required', life: 3000 })
    return
  }

  submitting.value = true;
  try {
    if (currentId.value) {
      await api.put(`/domains/${currentId.value}`, { projectId: form.projectId, name: form.name });
      toast.add({ severity: 'success', summary: 'Success', detail: 'Domain updated', life: 3000 });
    } else {
      await api.post('/domains', { projectId: form.projectId, name: form.name });
      toast.add({ severity: 'success', summary: 'Success', detail: 'Domain added', life: 3000 });
    }
    dialogVisible.value = false;
    fetchData();
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Operation failed';
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 });
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  await fetchProjects();
  fetchData();
});
</script>

<style scoped>
.domain-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
