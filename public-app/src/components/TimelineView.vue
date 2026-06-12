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
  <section class="space-y-10">
    <div class="flex flex-wrap items-center gap-2">
      <button
        type="button"
        class="pf-chip"
        :class="{ 'pf-chip--active': store.timelineGranularity === 'year' }"
        @click="store.timelineGranularity = 'year'"
      >
        按年
      </button>
      <button
        type="button"
        class="pf-chip"
        :class="{ 'pf-chip--active': store.timelineGranularity === 'month' }"
        @click="store.timelineGranularity = 'month'"
      >
        按月
      </button>
      <span class="mx-1 hidden h-4 w-px bg-pf-border-strong sm:inline" aria-hidden="true" />
      <button
        type="button"
        class="pf-chip"
        :class="{ 'pf-chip--active': store.timelineOrder === 'desc' }"
        @click="store.timelineOrder = 'desc'"
      >
        最新优先
      </button>
      <button
        type="button"
        class="pf-chip"
        :class="{ 'pf-chip--active': store.timelineOrder === 'asc' }"
        @click="store.timelineOrder = 'asc'"
      >
        最早优先
      </button>
    </div>

    <div v-if="loading" class="space-y-8">
      <div v-for="i in 4" :key="i" class="pf-skeleton h-32 w-full" />
    </div>

    <div v-else-if="groups.length === 0" class="pf-empty-state">
      <div class="w-16 h-16 mb-4 rounded-full bg-pf-bg-muted flex items-center justify-center text-pf-text-subtle">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
      </div>
      <p class="text-sm font-medium text-pf-text-muted">No timeline data available</p>
    </div>

    <div v-else class="timeline-rail">
      <div
        v-for="(group, groupIndex) in groups"
        :key="group.key"
        class="timeline-rail__row relative grid gap-6 pb-14 last:pb-0 md:grid-cols-[6.5rem_minmax(0,1fr)] md:gap-10 md:pb-16"
      >
        <div
          class="timeline-rail__label md:sticky md:top-28 md:self-start"
          :class="{ 'timeline-rail__label--first': groupIndex === 0 }"
        >
          <time class="block text-sm font-medium tabular-nums tracking-tight text-pf-text md:text-right">
            {{ group.label }}
          </time>
        </div>

        <div class="timeline-rail__content relative min-w-0 pl-8 md:pl-0">
          <span
            class="timeline-rail__dot absolute left-0 top-1.5 hidden h-1.5 w-1.5 rounded-full bg-pf-text md:block"
            aria-hidden="true"
          />
          <StackedTimelineNode :projects="group.projects" @select="handleSelect" />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.timeline-rail {
  position: relative;
}

.timeline-rail::before {
  content: '';
  position: absolute;
  left: 6.5rem;
  top: 0.375rem;
  bottom: 0;
  width: 1px;
  background: linear-gradient(
    to bottom,
    var(--pf-border-strong),
    var(--pf-border) 80%,
    transparent
  );
  transform: translateX(-50%);
  display: none;
}

@media (min-width: 768px) {
  .timeline-rail::before {
    display: block;
  }
}

@media (max-width: 767px) {
  .timeline-rail__content::before {
    content: '';
    position: absolute;
    left: 0.1875rem;
    top: 0.375rem;
    bottom: -3.5rem;
    width: 1px;
    background: var(--pf-border-strong);
  }

  .timeline-rail__row:last-child .timeline-rail__content::before {
    bottom: 0;
  }

  .timeline-rail__dot {
    display: block !important;
    left: 0;
  }
}

.timeline-rail__dot {
  transform: translateX(-50%);
  left: 0;
}
</style>
