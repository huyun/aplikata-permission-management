<template>
  <div class="profile-container">
    <Card class="profile-card">
      <template #title>
        <h2>User Profile</h2>
      </template>
      <template #content>
        <div class="p-fluid">
          <!-- 用户名（只读） -->
          <div class="grid mb-3 align-items-center">
            <div class="col-fixed w-2 text-right">
              <label for="username">Username</label>
            </div>
            <div class="col">
              <InputText id="username" v-model="form.username" disabled class="w-full" />
            </div>
          </div>

          <!-- 邮箱（可编辑） -->
          <div class="grid mb-3 align-items-center">
            <div class="col-fixed w-2 text-right">
              <label for="email">Email</label>
            </div>
            <div class="col">
              <InputText id="email" v-model="form.email" class="w-full" />
              <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="grid">
            <div class="col-fixed w-20"></div>
            <div class="col">
              <Button label="Update Profile" icon="pi pi-save" @click="handleUpdate" :loading="updating" />
            </div>
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import api from '@/api';

const authStore = useAuthStore();
const toast = useToast();

const updating = ref(false);
const form = reactive({
  username: '',
  email: '',
});
const errors = reactive({
  email: '',
});

// 加载用户信息（从 store 或后端）
const loadUserProfile = () => {
  // 优先从 store 获取
  if (authStore.user) {
    form.username = authStore.user.username || '';
    form.email = authStore.user.email || '';
  } else {
    // 如果 store 中没有（理论上登录后应有），可以调用接口
    api
      .get('/user/profile')
      .then((res) => {
        form.username = res.username;
        form.email = res.email;
      })
      .catch((err) => {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load profile', life: 3000 });
      });
  }
};

// 邮箱校验
const validateEmail = (email) => {
  const re = /^[^\s@]+@([^\s@]+\.)+[^\s@]+$/;
  return re.test(email);
};

const handleUpdate = async () => {
  errors.email = '';
  if (!form.email.trim()) {
    errors.email = 'Email is required';
    return;
  }
  if (!validateEmail(form.email)) {
    errors.email = 'Please enter a valid email address';
    return;
  }

  updating.value = true;
  try {
    const updatedUser = await api.put('/users/profile', { email: form.email });
    console.log('Updated user:', updatedUser);
    // 更新 store 和 localStorage
    authStore.user = { ...authStore.user, ...updatedUser };
    localStorage.setItem('user', JSON.stringify(authStore.user));

    toast.add({ severity: 'success', summary: 'Success', detail: 'Profile updated successfully', life: 3000 });
  } catch (err) {
    const errorMsg = err.response?.data?.message || err.message || 'Update failed';
    toast.add({ severity: 'error', summary: 'Error', detail: errorMsg, life: 3000 });
  } finally {
    updating.value = false;
  }
};

onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
.profile-container {
  display: flex;
  justify-content: center;
  align-items: start;
  min-height: 100%;
  padding: 20px;
}
.profile-card {
  width: 600px;
  max-width: 90%;
}
</style>
