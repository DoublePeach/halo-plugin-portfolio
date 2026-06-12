<script setup lang="ts">
import type { PortfolioProject } from '@/types/portfolio'

defineProps<{
  project: PortfolioProject
  compact?: boolean
}>()

defineEmits<{
  click: []
}>()

function formatDate(value?: string) {
  if (!value) return 'TBD'
  return new Date(value).getFullYear().toString()
}
</script>

<template>
  <article
    class="timeline-node-card group pf-card-interactive flex overflow-hidden border-b border-pf-border last:border-0 pb-8 transition-opacity hover:opacity-70"
    :class="compact ? 'flex-row gap-6 p-4 border-0' : 'flex-col sm:flex-row gap-8 sm:gap-12'"
    @click="$emit('click')"
  >
    <div
      class="relative shrink-0 overflow-hidden bg-pf-bg-muted filter grayscale"
      :class="
        compact
          ? 'h-24 w-32'
          : 'aspect-[16/10] w-full sm:w-56'
      "
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition-transform duration-1000 group-hover:scale-105"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-3xl font-light text-pf-text-subtle"
        style="font-family: var(--pf-font-display)"
      >
        {{ project.title.slice(0, 1) }}
      </div>
    </div>

    <div
      class="flex min-w-0 flex-1 flex-col justify-center"
    >
      <div class="flex items-start justify-between gap-3 mb-2">
        <h3
          class="font-light tracking-tight text-pf-text"
          style="font-family: var(--pf-font-display)"
          :class="compact ? 'text-xl' : 'text-2xl'"
        >
          {{ project.title }}
        </h3>
        <time class="shrink-0 text-xs tabular-nums text-pf-text-subtle uppercase tracking-widest mt-1">
          {{ formatDate(project.startDate) }}
        </time>
      </div>
      
      <p
        class="text-base leading-relaxed text-pf-text-muted font-light"
        :class="compact ? 'line-clamp-1' : 'line-clamp-2'"
      >
        {{ project.summary || 'A creative endeavor.' }}
      </p>
      
      <div
        v-if="!compact && (project.domain || project.tags?.length)"
        class="mt-6 flex flex-wrap gap-x-4 gap-y-2"
      >
        <span v-for="tag in (project.tags || []).slice(0, 3)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>
