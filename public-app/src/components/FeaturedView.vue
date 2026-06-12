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
      <p class="text-sm font-medium uppercase tracking-[0.2em] text-pf-text-subtle">No featured projects</p>
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
