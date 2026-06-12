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
    class="flex gap-8 overflow-x-auto border-b border-pf-border pb-6 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
    role="tablist"
    aria-label="View Switcher"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      role="tab"
      class="text-xs uppercase tracking-[0.2em] font-medium transition-colors duration-pf"
      :class="store.activeView === tab.key ? 'text-pf-text' : 'text-pf-text-subtle hover:text-pf-text-muted'"
      :aria-selected="store.activeView === tab.key"
      @click="store.activeView = tab.key"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>
