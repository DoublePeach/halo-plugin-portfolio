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
  <div class="pf-page-bg min-h-screen relative">
    <!-- Subtle background pattern -->
    <div class="absolute inset-0 -z-10 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMiIgY3k9IjIiIHI9IjEiIGZpbGw9InJnYmEoMTUwLCAxNTAsIDE1MCwgMC4xKSIvPjwvc3ZnPg==')] [mask-image:linear-gradient(to_bottom,white,transparent)]"></div>

    <PortfolioHero />

    <main class="pf-container pb-24 pt-4 md:pt-8">
      <div class="bg-pf-surface/50 backdrop-blur-md sticky top-4 z-30 rounded-full border border-pf-border p-2 shadow-sm mb-10 md:mb-14 max-w-fit mx-auto">
        <ViewSwitcher />
      </div>

      <div class="mt-4" role="tabpanel">
        <Transition name="fade" mode="out-in">
          <TimelineView v-if="store.activeView === 'timeline'" />
          <FeaturedView v-else-if="store.activeView === 'featured'" />
          <GroupView v-else-if="groupBy" :group-by="groupBy" />
        </Transition>
      </div>
    </main>

    <ProjectDetailModal />
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
