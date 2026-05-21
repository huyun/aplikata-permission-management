<script setup>
import { ref, reactive } from 'vue';

import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import api from '@/api';
import { requiredRule } from '@/utils/validators';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);

const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

// Custom validator to check if new password and confirm match
const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('Passwords do not match'));
  } else {
    callback();
  }
};

// Password strength rule (optional, adjust as needed)
const validatePassword = (rule, value, callback) => {
  if (value.length < 6) {
    callback(new Error('Password must be at least 6 characters'));
  } else {
    callback();
  }
};

const rules = {
  currentPassword: [requiredRule('Current Password')],
  newPassword: [requiredRule('New Password'), { validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [requiredRule('Confirm Password'), { validator: validateConfirm, trigger: 'blur' }],
};

const handleSubmit = async () => {
  await formRef.value?.validate();
  submitting.value = true;
  try {
    // Send request to backend
    const response = await api.put('/users/password', {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    });
    ElMessage.success('Password updated successfully, please login again');
    authStore.logout();
    router.push('/login');
  } catch (error) {
    ElMessage.error(error.message || 'Failed to update password');
  } finally {
    submitting.value = false;
  }
};

const resetForm = () => {
  formRef.value?.resetFields();
};
</script>
<template>
  <div class="change-password-container">
    <h2>Change Password</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" status-icon>
      <!-- Current Password -->
      <el-form-item label="Current Password" prop="currentPassword">
        <el-input v-model="form.currentPassword" type="password" placeholder="Enter current password" show-password autocomplete="off" />
      </el-form-item>

      <!-- New Password -->
      <el-form-item label="New Password" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" placeholder="Enter new password" show-password autocomplete="new-password" />
      </el-form-item>

      <!-- Confirm New Password -->
      <el-form-item label="Confirm Password" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" placeholder="Confirm new password" show-password autocomplete="new-password" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting"> Update Password </el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.change-password-container {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
}
</style>
