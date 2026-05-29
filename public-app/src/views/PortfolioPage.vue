<script setup lang="ts">
import { computed } from 'vue'
import PortfolioHero from '@/components/PortfolioHero.vue'
import ViewSwitcher from '@/components/ViewSwitcher.vue'
import TimelineView from '@/components/TimelineView.vue'
import FeaturedView from '@/components/FeaturedView.vue'
import GroupView from '@/components/GroupView.vue'
import ProjectDetailModal from '@/components/ProjectDetailModal.vue'
import { usePortfolioStore } from '@/stores/portfolio'

const store = usePortfolioStore()

const groupBy = computed(() => {
  const view = store.activeView
  if (view === 'domain' || view === 'techStack' || view === 'source') {
    return view
  }
  return null
})
</script>

<template>
  <div class="min-h-screen bg-pf-bg">
    <PortfolioHero />

    <main class="mx-auto max-w-6xl px-4 py-8 md:px-8 md:py-10">
      <ViewSwitcher />

      <div class="mt-8" role="tabpanel">
        <TimelineView v-if="store.activeView === 'timeline'" />
        <FeaturedView v-else-if="store.activeView === 'featured'" />
        <GroupView v-else-if="groupBy" :group-by="groupBy" />
      </div>
    </main>

    <ProjectDetailModal />
  </div>
</template>
