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
    class="flex gap-6 overflow-x-auto border-b border-pf-border pb-px [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
    role="tablist"
    aria-label="作品集视图切换"
  >
    <button
      v-for="tab in tabs"
      :key="tab.key"
      type="button"
      role="tab"
      class="pf-nav-tab shrink-0 whitespace-nowrap"
      :class="{ 'pf-nav-tab--active': store.activeView === tab.key }"
      :aria-selected="store.activeView === tab.key"
      @click="store.activeView = tab.key"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>
