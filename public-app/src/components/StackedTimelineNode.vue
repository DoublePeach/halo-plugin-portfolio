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
  <div v-if="projects.length === 1" class="timeline-node-single">
    <TimelineNodeCard :project="primaryProject" @click="emit('select', primaryProject)" />
  </div>

  <div
    v-else
    class="timeline-stack"
    @mouseenter="handleEnter"
    @mouseleave="handleLeave"
    @click="toggleExpand"
  >
    <p class="mb-6 text-xs uppercase tracking-widest text-pf-text-subtle">
      <span class="text-pf-text font-medium">{{ projects.length }}</span> Projects
      <span class="text-pf-border-strong mx-2">|</span>
      <span>{{ isMobile ? 'Tap' : 'Hover' }} to expand</span>
    </p>

    <div v-if="!expanded" class="timeline-stack__deck relative max-w-4xl cursor-pointer">
      <div
        v-for="(project, index) in stackedProjects"
        :key="project.name"
        class="timeline-stack-card bg-pf-surface border border-pf-border pointer-events-none absolute inset-x-0 top-0"
        :style="{
          transform: `translateY(${(index + 1) * 8}px) scale(${1 - (index + 1) * 0.02})`,
          zIndex: 10 - index,
          opacity: 0.8 - index * 0.1,
        }"
        aria-hidden="true"
      >
        <div class="h-24"></div>
      </div>
      <div class="relative z-20">
        <TimelineNodeCard :project="primaryProject" @click="selectProject(primaryProject)" />
      </div>
    </div>

    <div v-else class="timeline-stack__expanded flex flex-col gap-4">
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
  min-height: 12rem;
}

.timeline-stack-card {
  transition: all 400ms cubic-bezier(0.16, 1, 0.3, 1);
}
</style>
