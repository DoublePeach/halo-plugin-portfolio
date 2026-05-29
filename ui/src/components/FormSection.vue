<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <section class="pf-section">
    <header class="pf-section__header">
      <h3 class="pf-section__title">{{ title }}</h3>
      <p v-if="description" class="pf-section__desc">{{ description }}</p>
    </header>
    <div class="pf-section__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.pf-section__body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>
