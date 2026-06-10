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
    class="timeline-node-card group pf-card-interactive flex overflow-hidden"
    :class="compact ? 'flex-row gap-4 p-4' : 'flex-col sm:flex-row'"
    @click="$emit('click')"
  >
    <div
      class="relative shrink-0 overflow-hidden bg-pf-bg-soft"
      :class="
        compact
          ? 'h-[4.5rem] w-[6.5rem] rounded-md'
          : 'aspect-[16/10] w-full sm:w-40 md:w-44 sm:rounded-none'
      "
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition duration-500 group-hover:scale-[1.03]"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-lg font-light text-pf-text-subtle"
      >
        {{ project.title.slice(0, 1) }}
      </div>
      <span
        v-if="project.featured && !compact"
        class="absolute left-3 top-3 rounded border border-pf-border bg-pf-surface/90 px-2 py-0.5 text-[10px] font-medium text-pf-text backdrop-blur-sm"
      >
        核心
      </span>
    </div>

    <div
      class="flex min-w-0 flex-1 flex-col justify-center"
      :class="compact ? '' : 'p-5 sm:py-5 sm:pl-0 sm:pr-6'"
    >
      <div class="flex items-start justify-between gap-3">
        <h3
          class="font-semibold tracking-[-0.02em] text-pf-text"
          :class="compact ? 'text-sm' : 'text-base md:text-[1.0625rem]'"
        >
          {{ project.title }}
        </h3>
        <time class="shrink-0 text-[11px] tabular-nums text-pf-text-subtle">
          {{ formatDate(project.startDate) }}
        </time>
      </div>
      <p
        class="mt-1.5 text-sm leading-relaxed text-pf-text-muted"
        :class="compact ? 'line-clamp-1' : 'line-clamp-2'"
      >
        {{ project.summary || '暂无简介' }}
      </p>
      <div
        v-if="!compact && (project.domain || project.tags?.length)"
        class="mt-3 flex flex-wrap gap-1.5"
      >
        <span v-if="project.domain" class="pf-tag-accent">{{ getDomainLabel(project.domain) }}</span>
        <span v-for="tag in (project.tags || []).slice(0, 3)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.timeline-node-card:hover {
  transform: translateY(-1px);
}
</style>
