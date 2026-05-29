<script setup lang="ts">
import { getDomainLabel } from '@/constants/labels'
import type { PortfolioProject } from '@/types/portfolio'

defineProps<{
  project: PortfolioProject
}>()

defineEmits<{
  click: []
}>()

function formatDate(value?: string) {
  if (!value) return '时间待定'
  return new Date(value).toLocaleDateString('zh-CN')
}
</script>

<template>
  <article class="group pf-card-interactive overflow-hidden" @click="$emit('click')">
    <div class="relative aspect-[16/10] overflow-hidden bg-pf-bg-soft">
      <img
        v-if="project.coverImage"
        :src="project.coverImage"
        :alt="project.title"
        class="h-full w-full object-cover transition duration-pf group-hover:scale-[1.03]"
        loading="lazy"
      />
      <div
        v-else
        class="flex h-full items-center justify-center bg-pf-bg-muted text-4xl font-bold text-pf-text-subtle"
      >
        {{ project.title.slice(0, 1) }}
      </div>
      <span
        v-if="project.featured"
        class="absolute left-3 top-3 rounded-full bg-pf-primary px-3 py-1 text-xs font-semibold text-white"
      >
        核心项目
      </span>
    </div>
    <div class="space-y-3 p-5">
      <div class="flex items-start justify-between gap-3">
        <h3 class="text-lg font-semibold text-pf-text">{{ project.title }}</h3>
        <span class="whitespace-nowrap text-xs text-pf-text-subtle">
          {{ formatDate(project.startDate) }}
        </span>
      </div>
      <p class="line-clamp-2 text-sm leading-6 text-pf-text-muted">
        {{ project.summary || '暂无简介' }}
      </p>
      <div class="flex flex-wrap gap-2">
        <span v-if="project.domain" class="pf-tag">{{ getDomainLabel(project.domain) }}</span>
        <span v-for="tag in (project.tags || []).slice(0, 3)" :key="tag" class="pf-tag-muted">
          {{ tag }}
        </span>
      </div>
    </div>
  </article>
</template>
