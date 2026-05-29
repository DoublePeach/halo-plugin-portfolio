<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import type { PortfolioProject } from '@/types/portfolio'
import TimelineNodeCard from '@/components/TimelineNodeCard.vue'

const props = defineProps<{
  projects: PortfolioProject[]
}>()

const emit = defineEmits<{
  select: [project: PortfolioProject]
}>()

const isMobile = useMediaQuery('(max-width: 768px)')
const expanded = ref(false)

const primaryProject = computed(() => props.projects[0])
const stackedProjects = computed(() => props.projects.slice(1, 4))

function handleEnter() {
  if (!isMobile.value && props.projects.length > 1) {
    expanded.value = true
  }
}

function handleLeave() {
  if (!isMobile.value) {
    expanded.value = false
  }
}

function toggleExpand(event: MouseEvent) {
  if (!isMobile.value || props.projects.length <= 1) return
  const target = event.target as HTMLElement
  if (target.closest('.timeline-node-card')) return
  expanded.value = !expanded.value
}

function selectProject(project: PortfolioProject, event?: MouseEvent) {
  event?.stopPropagation()
  emit('select', project)
}
</script>

<template>
  <div
    v-if="projects.length === 1"
    class="timeline-node-single"
  >
    <TimelineNodeCard :project="primaryProject" @click="emit('select', primaryProject)" />
  </div>

  <div
    v-else
    class="timeline-stack"
    @mouseenter="handleEnter"
    @mouseleave="handleLeave"
    @click="toggleExpand"
  >
    <p class="mb-3 text-xs text-pf-text-subtle">
      {{ projects.length }} 个项目
      <span v-if="isMobile" class="text-pf-primary"> · 点击展开</span>
      <span v-else class="text-pf-primary"> · 悬停展开</span>
    </p>

    <div v-if="!expanded" class="timeline-stack__deck relative max-w-2xl">
      <div
        v-for="(project, index) in stackedProjects"
        :key="project.name"
        class="timeline-stack-card pf-card pointer-events-none absolute inset-x-0 top-0 overflow-hidden opacity-70 shadow-pf"
        :style="{
          transform: `translateY(${(index + 1) * 10}px) scale(${1 - (index + 1) * 0.025})`,
          zIndex: 10 - index,
        }"
        aria-hidden="true"
      >
        <div class="flex gap-3 p-3">
          <div class="h-12 w-16 shrink-0 overflow-hidden rounded bg-pf-bg-soft">
            <img
              v-if="project.coverImage"
              :src="project.coverImage"
              alt=""
              class="h-full w-full object-cover"
            />
            <div
              v-else
              class="flex h-full items-center justify-center text-sm font-bold text-pf-text-subtle"
            >
              {{ project.title.slice(0, 1) }}
            </div>
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-pf-text">{{ project.title }}</p>
            <p class="mt-0.5 line-clamp-1 text-xs text-pf-text-muted">
              {{ project.summary || '暂无简介' }}
            </p>
          </div>
        </div>
      </div>
      <div class="relative z-20">
        <TimelineNodeCard :project="primaryProject" @click="selectProject(primaryProject)" />
      </div>
    </div>

    <div
      v-else
      class="timeline-stack__expanded grid gap-4 sm:grid-cols-2"
    >
      <TimelineNodeCard
        v-for="project in projects"
        :key="project.name"
        :project="project"
        compact
        @click="selectProject(project)"
      />
    </div>
  </div>
</template>

<style scoped>
.timeline-stack__deck {
  min-height: 120px;
}

.timeline-stack-card {
  border-color: var(--pf-border);
}
</style>
