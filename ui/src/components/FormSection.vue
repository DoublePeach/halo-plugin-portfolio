<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section class="pf-section">
    <header v-if="title || description" class="pf-section__header">
      <h3 v-if="title" class="pf-section__title">{{ title }}</h3>
      <p v-if="description" class="pf-section__desc">{{ description }}</p>
    </header>
    <div class="pf-section__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.pf-section + .pf-section {
  margin-top: 0.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--pf-border);
}

.pf-section__header {
  margin-bottom: 0.25rem;
}

.pf-section__body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.pf-section__body :deep(.formkit-outer) {
  margin-bottom: 0;
}
</style>
