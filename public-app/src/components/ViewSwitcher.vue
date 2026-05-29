<script setup lang="ts">
import { usePortfolioStore } from '@/stores/portfolio'
import type { ViewMode } from '@/types/portfolio'

const store = usePortfolioStore()

const tabs: Array<{ key: ViewMode; label: string }> = [
  { key: 'timeline', label: '时间线' },
  { key: 'featured', label: '核心项目' },
  { key: 'domain', label: '按领域' },
  { key: 'techStack', label: '按技术栈' },
  { key: 'source', label: '按来源' },
]
</script>

<template>
  <nav
    class="flex flex-wrap gap-2 border-b border-pf-border pb-4"
    role="tablist"
    aria-label="作品集视图切换"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      role="tab"
      class="pf-pill"
      :class="store.activeView === tab.key ? 'pf-pill-active' : 'pf-pill-inactive'"
      :aria-selected="store.activeView === tab.key"
      @click="store.activeView = tab.key"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>
