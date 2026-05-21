<template>
  <div class="base-table-container">
    <el-table v-bind="$attrs" :data="data" style="width: 100%" @selection-change="handleSelectionChange">
      <template v-for="(_, name) in $slots" #[name]="slotData">
        <slot :name="name" v-bind="slotData" />
      </template>
    </el-table>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['selection-change']);

const handleSelectionChange = (val) => {
  emit('selection-change', val);
};
</script>

<style scoped>
.base-table-container {
  width: 100%;
  overflow-x: auto;
  border-radius: 4px;
}
</style>
