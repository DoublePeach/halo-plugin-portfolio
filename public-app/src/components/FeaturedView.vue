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
  <section class="space-y-8 md:space-y-10">
    <p class="max-w-2xl text-sm leading-relaxed text-pf-text-muted">
      精选
      <span class="font-medium text-pf-text">4–6</span>
      个最具代表性的核心项目，用于快速建立能力印象。
    </p>

    <div v-if="loading" class="grid gap-4 md:grid-cols-2 md:gap-5 xl:grid-cols-3">
      <div v-for="i in 6" :key="i" class="pf-skeleton aspect-[4/3] w-full" />
    </div>

    <div v-else-if="projects.length === 0" class="pf-empty-state">
      <p class="text-sm text-pf-text-muted">暂无核心项目</p>
      <p class="mt-1 text-xs text-pf-text-subtle">请在后台将项目标记为「核心展示」</p>
    </div>

    <div
      v-else
      class="grid auto-rows-fr gap-4 md:grid-cols-2 md:gap-5 xl:grid-cols-3"
    >
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
