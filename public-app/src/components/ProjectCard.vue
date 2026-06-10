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
  if (!value) return '时间待定'
  return new Date(value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short' })
}
</script>

<template>
  <article
    class="group pf-card-interactive flex h-full flex-col overflow-hidden"
    :class="featured ? 'md:col-span-2 md:row-span-2' : ''"
    @click="$emit('click')"
  >
    <div
      class="relative overflow-hidden bg-pf-bg-soft"
      :class="featured ? 'aspect-[16/9] md:aspect-[21/10]' : 'aspect-[16/10]'"
    >
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition duration-500 group-hover:scale-[1.02]"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full items-center justify-center text-3xl font-light text-pf-text-subtle md:text-4xl"
      >
        {{ project.title.slice(0, 1) }}
      </div>
      <div
        class="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent opacity-0 transition-opacity duration-pf group-hover:opacity-100"
        aria-hidden="true"
      />
      <span
        v-if="project.featured"
        class="absolute left-4 top-4 rounded border border-white/20 bg-black/40 px-2.5 py-1 text-[10px] font-medium uppercase tracking-wider text-white backdrop-blur-sm"
      >
        核心
      </span>
    </div>

    <div class="flex flex-1 flex-col p-5 md:p-6" :class="featured ? 'md:p-8' : ''">
      <div class="flex items-start justify-between gap-4">
        <h3
          class="font-semibold tracking-[-0.02em] text-pf-text"
          :class="featured ? 'text-xl md:text-2xl' : 'text-base md:text-lg'"
        >
          {{ project.title }}
        </h3>
        <time class="shrink-0 pt-1 text-[11px] tabular-nums text-pf-text-subtle">
          {{ formatDate(project.startDate) }}
        </time>
      </div>

      <p
        class="mt-2.5 flex-1 text-sm leading-relaxed text-pf-text-muted"
        :class="featured ? 'line-clamp-3 md:text-[0.9375rem]' : 'line-clamp-2'"
      >
        {{ project.summary || '暂无简介' }}
      </p>

      <div
        v-if="project.domain || project.tags?.length"
        class="mt-4 flex flex-wrap gap-1.5 border-t border-pf-border pt-4"
      >
        <span v-if="project.domain" class="pf-tag-accent">{{ getDomainLabel(project.domain) }}</span>
        <span v-for="tag in (project.tags || []).slice(0, featured ? 5 : 3)" :key="tag" class="pf-tag">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>
