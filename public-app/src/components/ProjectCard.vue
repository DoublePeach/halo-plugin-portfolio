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
    class="group pf-card-interactive flex flex-col md:flex-row border-b border-pf-border py-8 md:py-12 gap-6 md:gap-12"
    @click="$emit('click')"
  >
    <div
      class="relative overflow-hidden bg-pf-bg-muted w-full md:w-2/5 shrink-0"
      :class="featured ? 'aspect-[4/3]' : 'aspect-[16/9]'"
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition-transform duration-1000 group-hover:scale-105 filter grayscale hover:grayscale-0"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full items-center justify-center text-4xl font-light text-pf-text-subtle"
        style="font-family: var(--pf-font-display)"
      >
        {{ project.title.slice(0, 1) }}
      </div>
    </div>

    <div class="flex flex-col justify-center flex-1">
      <div class="flex items-center gap-4 mb-4">
        <span class="pf-eyebrow">{{ formatDate(project.startDate) }}</span>
        <span class="w-8 h-px bg-pf-border"></span>
        <span v-if="project.domain" class="pf-eyebrow">{{ getDomainLabel(project.domain) }}</span>
      </div>

      <h3
        class="font-light tracking-tight text-pf-text mb-4 transition-colors group-hover:text-pf-text-muted"
        style="font-family: var(--pf-font-display)"
        :class="featured ? 'text-3xl md:text-5xl' : 'text-2xl md:text-4xl'"
      >
        {{ project.title }}
      </h3>

      <p
        class="text-base leading-relaxed text-pf-text-muted font-light max-w-2xl"
        :class="featured ? 'line-clamp-3' : 'line-clamp-2'"
      >
        {{ project.summary || 'A creative endeavor.' }}
      </p>

      <div
        v-if="project.tags?.length"
        class="mt-8 flex flex-wrap gap-x-4 gap-y-2"
      >
        <span v-for="tag in (project.tags || []).slice(0, featured ? 4 : 3)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>
