<template>
  <Toast />
  <ConfirmDialog />
  <router-view />
</template>

<script setup>
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import { onMounted } from 'vue'
import { markRequiredLabels } from '@/utils/markRequiredLabels'
import { showRequiredMessage } from '@/utils/validation';

const toast = useToast();

onMounted(() => {
  markRequiredLabels()
  // 监听动态内容（如 Dialog 打开）
  const observer = new MutationObserver(() => markRequiredLabels());
  observer.observe(document.body, { childList: true, subtree: true });

  document.addEventListener('invalid', (event) => {
    const target = event.target;
    if (target && target.matches && target.matches('input, select, textarea') && target.hasAttribute('required')) {
      showRequiredMessage(event, toast);
    }
  }, true); // 捕获阶段，确保在默认行为前执行
})

</script>

<style>
/* body {
  font-family: 'Inter', sans-serif;
} */
</style>
