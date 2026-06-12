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
  if (!value) return 'TBD'
  return new Date(value).getFullYear().toString()
}
</script>

<template>
  <article
    class="timeline-node-card group pf-card-interactive flex overflow-hidden transition-all duration-300"
    :class="compact ? 'flex-row gap-4 p-3' : 'flex-col sm:flex-row gap-6 sm:gap-8'"
    @click="$emit('click')"
  >
    <div
      class="relative shrink-0 overflow-hidden bg-pf-bg-muted rounded-pf-lg"
      :class="
        compact
          ? 'h-20 w-28'
          : 'aspect-[16/10] w-full sm:w-64'
      "
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-3xl font-light text-pf-text-subtle bg-gradient-to-br from-pf-bg-muted to-pf-border"
        style="font-family: var(--pf-font-display)"
      >
        {{ project.title.slice(0, 1) }}
      </div>
    </div>

    <div
      class="flex min-w-0 flex-1 flex-col justify-center py-2 pr-4 sm:pr-6"
    >
      <div class="flex items-start justify-between gap-3 mb-2">
        <h3
          class="font-medium tracking-tight text-pf-text group-hover:text-pf-primary transition-colors"
          style="font-family: var(--pf-font-display)"
          :class="compact ? 'text-lg' : 'text-xl sm:text-2xl'"
        >
          {{ project.title }}
        </h3>
        <time class="shrink-0 inline-flex items-center rounded-full bg-pf-bg-muted px-2.5 py-0.5 text-xs font-medium text-pf-text-muted">
          {{ formatDate(project.startDate) }}
        </time>
      </div>
      
      <p
        class="text-sm sm:text-base leading-relaxed text-pf-text-muted"
        :class="compact ? 'line-clamp-1' : 'line-clamp-2'"
      >
        {{ project.summary || 'A creative endeavor.' }}
      </p>
      
      <div
        v-if="!compact && (project.domain || project.tags?.length)"
        class="mt-4 flex flex-wrap gap-2"
      >
        <span v-if="project.domain" class="pf-tag-accent">
          {{ getDomainLabel(project.domain) }}
        </span>
        <span v-for="tag in (project.tags || []).slice(0, 2)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>
