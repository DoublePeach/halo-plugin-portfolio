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
        <VTag v-if="spec.domain">{{ getLabel('DOMAIN', spec.domain) }}</VTag>
        <VTag v-for="tech in (spec.techStack || []).slice(0, 3)" :key="tech">
          {{ getLabel('TECH_STACK', tech) }}
        </VTag>
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
