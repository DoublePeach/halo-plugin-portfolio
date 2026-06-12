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
      <p class="text-sm uppercase tracking-widest text-pf-text-subtle">No Data</p>
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
