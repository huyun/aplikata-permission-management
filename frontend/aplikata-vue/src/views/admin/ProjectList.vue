<template>
  <div class="project-container">
    <Card>
      <template #title>
        <div class="flex justify-content-between align-items-center">
          <span>Project Management</span>
          <Button label="Add Project" icon="pi pi-plus" @click="handleAdd" />
        </div>
      </template>
      <template #content>
        <div class="toolbar">
          <Button label="Refresh" icon="pi pi-refresh" @click="fetchData" />
        </div>

        <DataTable :value="projectList" :loading="loading" class="w-full" stripedRows showGridlines>
          <Column field="id" header="ID" />
          <Column field="name" header="Name" />
          <Column header="Actions">
            <template #body="slotProps">
              <div class="flex gap-2 w-2">
                <Button icon="pi pi-pencil" text rounded @click="handleEdit(slotProps.data)" />
                <Button icon="pi pi-trash" text rounded severity="danger" @click="handleDelete(slotProps.data)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Add/Edit Dialog -->
    <Dialog v-model:visible="dialogVisible" :header="dialogTitle" :modal="true" :style="{ width: '500px' }">
      <form @submit.prevent="submitForm">
        <div class="p-fluid">
          <div class="field">
            <label for="name">Project Name</label>
            <InputText id="name" v-model="form.name" autocomplete="off" class="w-full" required />
          </div>
        </div>

        <div class="flex col-12 justify-content-between gap-2">
          <Button label="Cancel" icon="pi pi-times" class="p-button-outlined" severity="danger" @click="dialogVisible = false" />
          <Button label="Confirm" icon="pi pi-save" @click="submitForm" :loading="submitting" type="submit" />
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
import Message from 'primevue/message';
import api from '@/api';

const toast = useToast();

const projectList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('Add Project');
const submitting = ref(false);
const currentId = ref(null);

const form = reactive({
  name: '',
});

const errors = reactive({
  name: '',
});

const fetchData = async () => {
  loading.value = true;
  try {
    const data = await api.get('/projects');
    projectList.value = data;
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load projects', life: 3000 });
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  form.name = '';
  errors.name = '';
  currentId.value = null;
};

const handleAdd = () => {
  resetForm();
  dialogTitle.value = 'Add Project';
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  resetForm();
  dialogTitle.value = 'Edit Project';
  currentId.value = row.id;
  form.name = row.name;
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  if (confirm(`Are you sure to delete project "${row.name}"?`)) {
    api
      .delete(`/projects/${row.id}`)
      .then(() => {
        toast.add({ severity: 'success', summary: 'Success', detail: 'Project deleted', life: 3000 });
        fetchData();
      })
      .catch((err) => {
        toast.add({ severity: 'error', summary: 'Error', detail: err.message || 'Delete failed', life: 3000 });
      });
  }
};

const validateForm = () => {
  let isValid = true;
  errors.name = '';
  if (!form.name.trim()) {
    errors.name = 'Project name is required';
    isValid = false;
  }
  return isValid;
};

const submitForm = async () => {
  if (!validateForm()) return;
  submitting.value = true;
  try {
    if (currentId.value) {
      await api.put(`/projects/${currentId.value}`, { name: form.name });
      toast.add({ severity: 'success', summary: 'Success', detail: 'Project updated', life: 3000 });
    } else {
      await api.post('/projects', { name: form.name });
      toast.add({ severity: 'success', summary: 'Success', detail: 'Project added', life: 3000 });
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

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.project-container {
  padding: 20px;
}

.toolbar {
  margin-bottom: 20px;
}

.field {
  margin-bottom: 1rem;
}

.p-error {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
}
</style>
