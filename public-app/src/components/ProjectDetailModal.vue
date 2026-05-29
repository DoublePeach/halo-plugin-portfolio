<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { marked } from 'marked'
import { fetchProjectDetail } from '@/api/portfolio'
import { getDomainLabel, getSourceLabel } from '@/constants/labels'
import { usePortfolioStore } from '@/stores/portfolio'
import type { PortfolioProject } from '@/types/portfolio'

const store = usePortfolioStore()
const loading = ref(false)
const detail = ref<PortfolioProject | null>(null)
const activeImage = ref('')
const dialogRef = ref<HTMLElement | null>(null)

const renderedDescription = computed(() => {
  if (!detail.value?.description) {
    return '<p>暂无详细说明</p>'
  }
  return marked.parse(detail.value.description)
})

const dateRange = computed(() => {
  if (!detail.value) return ''
  const start = detail.value.startDate
    ? new Date(detail.value.startDate).toLocaleDateString('zh-CN')
    : ''
  const end = detail.value.endDate
    ? new Date(detail.value.endDate).toLocaleDateString('zh-CN')
    : '至今'
  if (!start) return ''
  return `${start} — ${end}`
})

async function loadDetail(name: string) {
  loading.value = true
  try {
    detail.value = await fetchProjectDetail(name)
    activeImage.value = detail.value.coverImage || detail.value.gallery?.[0] || ''
  } finally {
    loading.value = false
  }
}

function closeModal() {
  store.closeDetail()
  detail.value = null
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeModal()
  }
}

function trapFocus(event: KeyboardEvent) {
  if (event.key !== 'Tab' || !dialogRef.value) return
  const focusable = dialogRef.value.querySelectorAll<HTMLElement>(
    'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
  )
  if (focusable.length === 0) return
  const first = focusable[0]
  const last = focusable[focusable.length - 1]
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault()
    first.focus()
  }
}

watch(
  () => store.selectedProject?.name,
  (name) => {
    if (name && store.detailOpen) {
      loadDetail(name)
    }
  },
  { immediate: true },
)

watch(
  () => store.detailOpen,
  (open) => {
    if (open) {
      document.body.style.overflow = 'hidden'
      requestAnimationFrame(() => {
        dialogRef.value?.querySelector<HTMLElement>('button')?.focus()
      })
    } else {
      document.body.style.overflow = ''
    }
  },
)

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="store.detailOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm"
      role="presentation"
      @click.self="closeModal"
      @keydown="trapFocus"
    >
      <div
        ref="dialogRef"
        class="pf-card flex max-h-[90vh] w-full max-w-5xl flex-col overflow-hidden shadow-pf-lg"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="detail ? 'project-detail-title' : undefined"
      >
        <div
          class="sticky top-0 z-10 flex shrink-0 items-center justify-between border-b border-pf-border bg-pf-surface px-6 py-4"
        >
          <div class="min-w-0 pr-4">
            <h2 id="project-detail-title" class="truncate text-2xl font-bold text-pf-text">
              {{ detail?.title || '项目详情' }}
            </h2>
            <p v-if="detail?.sourceDetail" class="mt-1 truncate text-sm text-pf-text-muted">
              {{ detail.sourceDetail }}
            </p>
          </div>
          <button
            type="button"
            class="pf-btn-ghost shrink-0"
            aria-label="关闭项目详情"
            @click="closeModal"
          >
            关闭
          </button>
        </div>

        <div v-if="loading" class="p-10 text-center text-pf-text-muted">加载详情中...</div>

        <div v-else-if="detail" class="overflow-y-auto p-6">
          <div
            v-if="detail.domain || detail.source || dateRange"
            class="mb-6 flex flex-wrap gap-2 text-sm"
          >
            <span v-if="detail.domain" class="pf-tag">{{ getDomainLabel(detail.domain) }}</span>
            <span v-if="detail.source" class="pf-tag-muted">{{ getSourceLabel(detail.source) }}</span>
            <span v-if="dateRange" class="pf-tag-muted">{{ dateRange }}</span>
          </div>

          <div v-if="activeImage || detail.gallery?.length" class="mb-6 space-y-4">
            <img
              v-if="activeImage"
              :src="activeImage"
              :alt="detail.title"
              class="max-h-96 w-full rounded-pf object-cover"
            />
            <div v-if="detail.gallery?.length" class="flex flex-wrap gap-3">
              <button
                v-for="image in detail.gallery"
                :key="image"
                type="button"
                class="cursor-pointer overflow-hidden rounded-pf border-2 border-transparent transition duration-pf hover:border-pf-primary focus-visible:outline focus-visible:outline-2 focus-visible:outline-pf-primary"
                :class="activeImage === image ? '!border-pf-primary' : ''"
                @click="activeImage = image"
              >
                <img :src="image" alt="" class="h-20 w-28 object-cover" loading="lazy" />
              </button>
            </div>
          </div>

          <div class="mb-6 flex flex-wrap gap-2">
            <span v-for="tag in detail.tags || []" :key="tag" class="pf-tag-muted">{{ tag }}</span>
            <span v-for="tech in detail.techStack || []" :key="tech" class="pf-tag">{{ tech }}</span>
          </div>

          <article class="prose-pf" v-html="renderedDescription" />
        </div>
      </div>
    </div>
  </Teleport>
</template>
