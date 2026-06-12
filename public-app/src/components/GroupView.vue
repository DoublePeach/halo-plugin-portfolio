<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { fetchGrouped } from '@/api/portfolio'
import { usePortfolioStore } from '@/stores/portfolio'
import type { GroupSection, PortfolioProject, ViewMode } from '@/types/portfolio'
import ProjectCard from '@/components/ProjectCard.vue'

const props = defineProps<{
  groupBy: Exclude<ViewMode, 'timeline' | 'featured'>
}>()

const store = usePortfolioStore()
const loading = ref(false)
const sections = ref<GroupSection[]>([])

async function loadGrouped() {
  loading.value = true
  try {
    const data = await fetchGrouped(props.groupBy)
    sections.value = data.sections
  } finally {
    loading.value = false
  }
}

function handleSelect(project: PortfolioProject) {
  store.openDetail(project)
}

watch(
  () => props.groupBy,
  () => loadGrouped(),
  { immediate: true },
)

onMounted(loadGrouped)
</script>

<template>
  <section class="space-y-20 md:space-y-32">
    <div v-if="loading" class="space-y-12">
      <div v-for="i in 3" :key="i" class="space-y-5">
        <div class="pf-skeleton h-5 w-36" />
        <div class="flex flex-col">
          <div v-for="j in 3" :key="j" class="pf-skeleton h-32 w-full mb-6" />
        </div>
      </div>
    </div>

    <div v-else-if="sections.length === 0" class="pf-empty-state">
      <div class="w-16 h-16 mb-4 rounded-full bg-pf-bg-muted flex items-center justify-center text-pf-text-subtle">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="7" height="7" x="3" y="3" rx="1"/><rect width="7" height="7" x="14" y="3" rx="1"/><rect width="7" height="7" x="14" y="14" rx="1"/><rect width="7" height="7" x="3" y="14" rx="1"/></svg>
      </div>
      <p class="text-sm font-medium text-pf-text-muted">No categorized data</p>
    </div>

    <div v-for="section in sections" v-else :key="section.key" class="space-y-8">
      <div class="pf-section-label">
        <h3 class="pf-section-label__title">{{ section.label }}</h3>
        <span class="pf-section-label__count">{{ section.projects.length }} Projects</span>
      </div>
      <div class="flex flex-col">
        <ProjectCard
          v-for="project in section.projects"
          :key="project.name"
          :project="project"
          @click="handleSelect(project)"
        />
      </div>
    </div>
  </section>
</template>
