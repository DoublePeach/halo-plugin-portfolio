<script setup lang="ts">
import { usePortfolioStore } from '@/stores/portfolio'
import type { ViewMode } from '@/types/portfolio'

const store = usePortfolioStore()

const tabs: Array<{ key: ViewMode; label: string }> = [
  { key: 'timeline', label: 'Index' },
  { key: 'featured', label: 'Featured' },
  { key: 'domain', label: 'Domain' },
  { key: 'techStack', label: 'Tech Stack' },
]
</script>

<template>
  <nav
    class="flex gap-2 overflow-x-auto pb-4 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
    role="tablist"
    aria-label="View Switcher"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      role="tab"
      class="pf-nav-tab whitespace-nowrap"
      :class="{ 'pf-nav-tab--active': store.activeView === tab.key }"
      :aria-selected="store.activeView === tab.key"
      @click="store.activeView = tab.key"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>
