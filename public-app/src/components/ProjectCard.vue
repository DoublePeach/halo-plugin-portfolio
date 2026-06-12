<script setup lang="ts">
import { getDomainLabel } from '@/constants/labels'
import type { PortfolioProject } from '@/types/portfolio'

defineProps<{
  project: PortfolioProject
  featured?: boolean
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
    class="group pf-card-interactive flex flex-col md:flex-row overflow-hidden"
    @click="$emit('click')"
  >
    <div
      class="relative overflow-hidden bg-pf-bg-muted w-full md:w-5/12 shrink-0"
      :class="featured ? 'aspect-[4/3] md:aspect-auto' : 'aspect-[16/9] md:aspect-auto'"
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
        class="flex h-full min-h-[240px] items-center justify-center text-6xl font-light text-pf-text-subtle bg-gradient-to-br from-pf-bg-muted to-pf-border"
        style="font-family: var(--pf-font-display)"
      >
        {{ project.title.slice(0, 1) }}
      </div>
      
      <!-- Overlay gradient -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
    </div>

    <div class="flex flex-col justify-center flex-1 p-8 md:p-12">
      <div class="flex items-center gap-3 mb-6">
        <span class="inline-flex items-center rounded-full bg-pf-bg-muted px-3 py-1 text-sm font-medium text-pf-text-muted">
          {{ formatDate(project.startDate) }}
        </span>
        <span v-if="project.domain" class="inline-flex items-center rounded-full bg-pf-primary-soft px-3 py-1 text-sm font-medium text-pf-primary">
          {{ getDomainLabel(project.domain) }}
        </span>
      </div>

      <h3
        class="font-medium tracking-tight text-pf-text mb-4 transition-colors group-hover:text-pf-primary"
        style="font-family: var(--pf-font-display)"
        :class="featured ? 'text-3xl md:text-5xl' : 'text-2xl md:text-3xl'"
      >
        {{ project.title }}
      </h3>

      <p
        class="text-base md:text-lg leading-relaxed text-pf-text-muted max-w-2xl"
        :class="featured ? 'line-clamp-3' : 'line-clamp-2'"
      >
        {{ project.summary || 'A creative endeavor.' }}
      </p>

      <div
        v-if="project.tags?.length"
        class="mt-8 flex flex-wrap gap-2"
      >
        <span v-for="tag in (project.tags || []).slice(0, featured ? 5 : 3)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
        <span v-if="(project.tags || []).length > (featured ? 5 : 3)" class="pf-tag">
          +{{ (project.tags || []).length - (featured ? 5 : 3) }}
        </span>
      </div>
    </div>
  </article>
</template>
