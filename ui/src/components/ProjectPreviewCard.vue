<script setup lang="ts">
import { VTag } from '@halo-dev/components'
import { getDomainLabel } from '@/constants/options'
import type { PortfolioProjectSpec } from '@/types/portfolio'

const props = defineProps<{
  spec: PortfolioProjectSpec
  tagsText: string
}>()

function formatDate(value?: string) {
  if (!value) {
    return '时间待定'
  }
  return new Date(value).toLocaleDateString('zh-CN')
}

function parseTags(value: string) {
  return value
    .split(/[,，]/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 4)
}
</script>

<template>
  <div class="pf-preview-card">
    <div class="pf-preview-card__cover">
      <img v-if="spec.coverImage" :src="spec.coverImage" :alt="spec.title || '封面预览'" />
      <div v-else class="pf-preview-card__placeholder">
        {{ (spec.title || '项').slice(0, 1) }}
      </div>
    </div>
    <div class="pf-preview-card__body">
      <div class="flex-between">
        <h4 class="pf-preview-card__title">{{ spec.title || '未命名项目' }}</h4>
        <span class="pf-preview-card__date">{{ formatDate(spec.startDate) }}</span>
      </div>
      <p class="pf-preview-card__summary">{{ spec.summary || '暂无简介，填写后将在此预览。' }}</p>
      <div class="pf-preview-card__tags">
        <VTag v-if="spec.featured">核心项目</VTag>
        <VTag v-if="spec.domain">{{ getDomainLabel(spec.domain) }}</VTag>
        <VTag v-for="tag in parseTags(tagsText)" :key="tag">{{ tag }}</VTag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.flex-between {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.pf-preview-card__date {
  font-size: 0.75rem;
  color: var(--pf-text-muted);
  white-space: nowrap;
}
</style>
