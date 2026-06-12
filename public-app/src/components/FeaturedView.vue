<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchFeaturedProjects } from '@/api/portfolio'
import { usePortfolioStore } from '@/stores/portfolio'
import type { PortfolioProject } from '@/types/portfolio'
import ProjectCard from '@/components/ProjectCard.vue'

const store = usePortfolioStore()
const projects = ref<PortfolioProject[]>([])
const loading = ref(false)

async function loadFeatured() {
  loading.value = true
  try {
    projects.value = await fetchFeaturedProjects()
  } finally {
    loading.value = false
  }
}

onMounted(loadFeatured)
</script>

<template>
  <section class="space-y-12">
    <div v-if="loading" class="space-y-12">
      <div v-for="i in 3" :key="i" class="pf-skeleton h-64 w-full" />
    </div>

    <div v-else-if="projects.length === 0" class="pf-empty-state">
      <div class="w-16 h-16 mb-4 rounded-full bg-pf-bg-muted flex items-center justify-center text-pf-text-subtle">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
      </div>
      <p class="text-sm font-medium text-pf-text-muted">No featured projects</p>
    </div>

    <div v-else class="flex flex-col">
      <ProjectCard
        v-for="(project, index) in projects"
        :key="project.name"
        :project="project"
        :featured="index === 0"
        @click="store.openDetail(project)"
      />
    </div>
  </section>
</template>
