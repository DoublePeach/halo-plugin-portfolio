<script setup lang="ts">
import { getDomainLabel } from '@/constants/labels'
import type { PortfolioProject } from '@/types/portfolio'

defineProps<{
  project: PortfolioProject
  compact?: boolean
}>()

defineEmits<{
  click: []
}>()

function formatDate(value?: string) {
  if (!value) return '时间待定'
  return new Date(value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short' })
}
</script>

<template>
  <article
    class="timeline-node-card pf-card-interactive flex overflow-hidden"
    :class="compact ? 'flex-row gap-3 p-3' : 'flex-col sm:flex-row sm:gap-4 sm:p-0'"
    @click="$emit('click')"
  >
    <div
      class="relative shrink-0 overflow-hidden bg-pf-bg-soft"
      :class="compact ? 'h-16 w-24 rounded-md' : 'aspect-[16/10] w-full sm:w-44 md:w-52 sm:rounded-l-pf'"
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition duration-pf group-hover:scale-[1.02]"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-xl font-bold text-pf-text-subtle"
      >
        {{ project.title.slice(0, 1) }}
      </div>
      <span
        v-if="project.featured"
        class="absolute left-2 top-2 rounded-full bg-pf-primary px-2 py-0.5 text-[10px] font-semibold text-white"
      >
        核心
      </span>
    </div>

    <div class="flex min-w-0 flex-1 flex-col justify-center" :class="compact ? '' : 'p-4 sm:py-4 sm:pr-5'">
      <div class="flex items-start justify-between gap-2">
        <h3 class="text-base font-semibold text-pf-text md:text-lg">{{ project.title }}</h3>
        <time class="shrink-0 text-xs text-pf-text-subtle">{{ formatDate(project.startDate) }}</time>
      </div>
      <p class="mt-1.5 line-clamp-2 text-sm leading-relaxed text-pf-text-muted">
        {{ project.summary || '暂无简介' }}
      </p>
      <div v-if="project.domain || project.tags?.length" class="mt-2 flex flex-wrap gap-1.5">
        <span v-if="project.domain" class="pf-tag">{{ getDomainLabel(project.domain) }}</span>
        <span v-for="tag in (project.tags || []).slice(0, 3)" :key="tag" class="pf-tag-muted">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.timeline-node-card {
  min-height: 0;
}
</style>
