<template>
  <div>
    <h2>Welcome, {{ authStore.user?.username || 'User' }}!</h2>
    <p>dashboard content</p>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
</script>
