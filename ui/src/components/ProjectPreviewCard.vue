<script setup lang="ts">
import { computed } from 'vue'
import { VTag } from '@halo-dev/components'
import { formatDate } from '@/utils/date'
import type { PortfolioOption, PortfolioProjectSpec } from '@/types/portfolio'

const props = defineProps<{
  spec: PortfolioProjectSpec
  optionLabelMap?: PortfolioOption[]
}>()

const labelMap = computed(() => {
  const map = new Map<string, string>()
  for (const option of props.optionLabelMap ?? []) {
    map.set(`${option.spec.type}:${option.spec.value}`, option.spec.label)
  }
  return map
})

function getLabel(type: string, value?: string) {
  if (!value) return ''
  return labelMap.value.get(`${type}:${value}`) ?? value
}
</script>

<template>
  <div class="pf-preview-card">
    <p class="pf-preview-card__label">前台卡片预览</p>
    <div class="pf-preview-card__cover">
      <img v-if="spec.coverImage" :src="spec.coverImage" :alt="spec.title || '封面预览'" />
      <div v-else class="pf-preview-card__placeholder">
        {{ (spec.title || '项').slice(0, 1) }}
      </div>
    </div>
    <div class="pf-preview-card__body">
      <div class="pf-preview-card__header">
        <h4 class="pf-preview-card__title">{{ spec.title || '未命名项目' }}</h4>
        <span class="pf-preview-card__date">{{ formatDate(spec.startDate) }}</span>
      </div>
      <p class="pf-preview-card__summary">{{ spec.summary || '暂无简介，填写后将在此预览。' }}</p>
      <div class="pf-preview-card__tags">
        <VTag v-if="spec.featured" size="sm">核心项目</VTag>
        <VTag v-if="spec.domain" size="sm">{{ getLabel('DOMAIN', spec.domain) }}</VTag>
        <VTag v-for="tech in (spec.techStack || []).slice(0, 3)" :key="tech" size="sm">
          {{ getLabel('TECH_STACK', tech) }}
        </VTag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pf-preview-card__label {
  margin: 0;
  padding: 0.625rem 0.875rem;
  font-size: 0.6875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--pf-text-subtle);
  border-bottom: 1px solid var(--pf-border);
  background: var(--pf-bg-muted);
}

.pf-preview-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
}

.pf-preview-card__date {
  font-size: 0.6875rem;
  color: var(--pf-text-subtle);
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}
</style>
