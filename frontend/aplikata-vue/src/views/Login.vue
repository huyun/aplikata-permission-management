<template>
  <div class="login-container">
    <Card class="login-card">
      <template #title>
        <h2>Login</h2>
      </template>
      <template #content>
        <form @submit.prevent="handleLogin">
          <div class="field">
            <label for="username" class="block">Username</label>
            <InputText id="username" v-model="form.username" :class="{ 'p-invalid': errors.username }" autocomplete="username" placeholder="Enter your username" />
            <small v-if="errors.username" class="p-error">{{ errors.username }}</small>
          </div>

          <div class="field">
            <label for="password" class="block">Password</label>
            <Password id="password" v-model="form.password" :feedback="false" toggleMask :class="{ 'p-invalid': errors.password }" placeholder="Enter your password" autocomplete="current-password" />
            <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
          </div>

          <Button type="submit" label="Login" icon="pi pi-sign-in" :loading="authStore.isLoading" class="w-full mt-3" />
          <div class="register-link">
            Don't have an account?
            <router-link to="/register">Register now</router-link>
          </div>
        </form>
      </template>
    </Card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import Password from 'primevue/password';
import Button from 'primevue/button';

const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

const form = reactive({
  username: '',
  password: '',
});

const errors = ref({
  username: '',
  password: '',
});

const validateForm = () => {
  let isValid = true;
  errors.value = { username: '', password: '' };
  if (!form.username.trim()) {
    errors.value.username = 'Username is required';
    isValid = false;
  }
  if (!form.password) {
    errors.value.password = 'Password is required';
    isValid = false;
  }
  return isValid;
};

const handleLogin = async () => {
  if (!validateForm()) return;

  const result = await authStore.login(form);
  if (result.success) {
    toast.add({
      severity: 'success',
      summary: 'Login Successful',
      detail: 'Welcome back!',
      life: 3000,
    });
    const firstPath = authStore.getFirstMenuPath();
    router.push(firstPath || '/dashboard');
  } else {
    toast.add({
      severity: 'error',
      summary: 'Login Failed',
      detail: result.message || 'Invalid credentials',
      life: 5000,
    });
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f6f9;
}

.login-card {
  width: 400px;
  padding: 20px;
}

.field {
  margin-bottom: 1.5rem;
}

label {
  margin-bottom: 0.5rem;
  display: block;
}

/* 统一输入框宽度 */
.field :deep(.p-inputtext),
.field :deep(.p-password) {
  width: 100%;
}

.p-error {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
}

.register-link {
  text-align: center;
  margin-top: 1rem;
}

.register-link a {
  color: #3b82f6;
  text-decoration: none;
}
</style>
