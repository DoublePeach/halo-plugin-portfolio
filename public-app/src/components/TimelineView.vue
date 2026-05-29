<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { fetchTimeline } from '@/api/portfolio'
import { usePortfolioStore } from '@/stores/portfolio'
import type { PortfolioProject, TimelineGroup } from '@/types/portfolio'
import StackedTimelineNode from '@/components/StackedTimelineNode.vue'

const store = usePortfolioStore()
const loading = ref(false)
const groups = ref<TimelineGroup[]>([])

async function loadTimeline() {
  loading.value = true
  try {
    const data = await fetchTimeline(store.timelineGranularity, store.timelineOrder)
    groups.value = data.groups
  } finally {
    loading.value = false
  }
}

function handleSelect(project: PortfolioProject) {
  store.openDetail(project)
}

watch(
  () => [store.timelineGranularity, store.timelineOrder],
  () => loadTimeline(),
)

onMounted(loadTimeline)
</script>

<template>
  <section class="space-y-8">
    <div class="flex flex-wrap items-center gap-2">
      <button
        type="button"
        class="pf-pill"
        :class="store.timelineGranularity === 'year' ? 'pf-pill-active' : 'pf-pill-inactive'"
        @click="store.timelineGranularity = 'year'"
      >
        按年
      </button>
      <button
        type="button"
        class="pf-pill"
        :class="store.timelineGranularity === 'month' ? 'pf-pill-active' : 'pf-pill-inactive'"
        @click="store.timelineGranularity = 'month'"
      >
        按月
      </button>
      <span class="mx-1 hidden h-5 w-px bg-pf-border sm:inline" aria-hidden="true" />
      <button
        type="button"
        class="pf-pill"
        :class="store.timelineOrder === 'desc' ? 'pf-pill-active' : 'pf-pill-inactive'"
        @click="store.timelineOrder = 'desc'"
      >
        时间降序
      </button>
      <button
        type="button"
        class="pf-pill"
        :class="store.timelineOrder === 'asc' ? 'pf-pill-active' : 'pf-pill-inactive'"
        @click="store.timelineOrder = 'asc'"
      >
        时间正序
      </button>
    </div>

    <div v-if="loading" class="space-y-6">
      <div v-for="i in 4" :key="i" class="pf-skeleton h-28 w-full" />
    </div>

    <div v-else-if="groups.length === 0" class="rounded-pf border border-dashed border-pf-border py-16 text-center">
      <p class="text-pf-text-muted">暂无项目时间线数据</p>
    </div>

    <div v-else class="timeline-rail relative">
      <div
        class="timeline-rail__line absolute bottom-0 left-[4.5rem] top-0 hidden w-0.5 bg-gradient-to-b from-pf-primary via-pf-accent/50 to-transparent md:block"
        aria-hidden="true"
      />

      <div
        v-for="group in groups"
        :key="group.key"
        class="timeline-rail__row relative grid gap-4 pb-12 last:pb-0 md:grid-cols-[7.5rem_minmax(0,1fr)] md:gap-8"
      >
        <div class="timeline-rail__label flex items-start gap-3 md:sticky md:top-24 md:flex-col md:items-center md:gap-2">
          <span
            class="relative z-10 flex h-3 w-3 shrink-0 rounded-full bg-pf-primary shadow-[0_0_0_4px_var(--pf-primary-soft)]"
            aria-hidden="true"
          />
          <span class="text-sm font-semibold text-pf-primary md:text-center">{{ group.label }}</span>
        </div>

        <div class="timeline-rail__content min-w-0 pl-6 md:pl-0">
          <StackedTimelineNode :projects="group.projects" @select="handleSelect" />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
@media (max-width: 767px) {
  .timeline-rail__row::before {
    content: '';
    position: absolute;
    left: 0.35rem;
    top: 0.5rem;
    bottom: 0;
    width: 2px;
    background: linear-gradient(to bottom, var(--pf-primary), transparent);
  }
}
</style>
