import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PortfolioProject, ViewMode } from '@/types/portfolio'

export const usePortfolioStore = defineStore('portfolio', () => {
  const activeView = ref<ViewMode>('timeline')
  const timelineGranularity = ref<'year' | 'month'>('year')
  const timelineOrder = ref<'asc' | 'desc'>('desc')
  const selectedProject = ref<PortfolioProject | null>(null)
  const detailOpen = ref(false)

  function openDetail(project: PortfolioProject) {
    selectedProject.value = project
    detailOpen.value = true
  }

  function closeDetail() {
    detailOpen.value = false
    selectedProject.value = null
  }

  return {
    activeView,
    timelineGranularity,
    timelineOrder,
    selectedProject,
    detailOpen,
    openDetail,
    closeDetail,
  }
})
