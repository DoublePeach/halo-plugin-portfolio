<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  hint?: string
  tone?: 'default' | 'primary' | 'success' | 'warning' | 'accent'
}>()
</script>

<template>
  <div class="pf-stat-card" :class="tone !== 'default' ? `pf-stat-card--${tone}` : undefined">
    <p class="pf-stat-card__label">{{ label }}</p>
    <p class="pf-stat-card__value">{{ value }}</p>
    <p v-if="hint" class="pf-stat-card__hint">{{ hint }}</p>
  </div>
</template>
