<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>Register</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="Username" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="Email" prefix-icon="Message" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="Password" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="Confirm Password" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="authStore.isLoading" @click="handleRegister" style="width: 100%"> Register </el-button>
        </el-form-item>
        <div style="text-align: center">Already have an account? <router-link to="/login">Login now</router-link></div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();
const router = useRouter();
const formRef = ref(null);

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
});

const validatePass2 = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match'));
  } else {
    callback();
  }
};

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Invalid email format', trigger: 'blur' },
  ],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' },
  ],
};

const handleRegister = async () => {
  await formRef.value?.validate();
  const result = await authStore.register(form);
  if (result.success) {
    ElMessage.success('Registration successful, please login');
    router.push('/login');
  } else {
    ElMessage.error(result.message);
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.register-card {
  width: 400px;
}
</style>
