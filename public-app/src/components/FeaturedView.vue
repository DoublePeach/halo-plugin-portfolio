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
  <section class="space-y-6">
    <div class="rounded-pf border border-pf-border bg-pf-bg-muted px-4 py-3">
      <p class="text-sm text-pf-text-muted">
        建议展示 <strong class="text-pf-text">4–6</strong> 个最具代表性的核心项目，用于首屏快速建立能力印象。
      </p>
    </div>

    <div v-if="loading" class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
      <div v-for="i in 6" :key="i" class="pf-skeleton aspect-[4/3] w-full" />
    </div>

    <div
      v-else-if="projects.length === 0"
      class="rounded-pf border border-dashed border-pf-border py-16 text-center"
    >
      <p class="text-pf-text-muted">暂无核心项目，请在后台将项目标记为「核心展示」</p>
    </div>

    <div v-else class="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
      <ProjectCard
        v-for="project in projects"
        :key="project.name"
        :project="project"
        @click="store.openDetail(project)"
      />
    </div>
  </section>
</template>
