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
    return '<p class="text-pf-text-muted">暂无详细说明</p>'
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
    <Transition name="modal-fade">
      <div
        v-if="store.detailOpen"
        class="modal-backdrop fixed inset-0 z-50 flex items-end justify-center p-0 sm:items-center sm:p-4 md:p-6"
        role="presentation"
        @click.self="closeModal"
        @keydown="trapFocus"
      >
        <div
          ref="dialogRef"
          class="modal-panel pf-card flex max-h-[92vh] w-full max-w-5xl flex-col overflow-hidden sm:max-h-[88vh]"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="detail ? 'project-detail-title' : undefined"
        >
          <div
            class="flex shrink-0 items-center justify-between border-b border-pf-border px-5 py-4 md:px-8 md:py-5"
          >
            <div class="min-w-0 pr-4">
              <p v-if="detail?.sourceDetail" class="pf-eyebrow mb-2">
                {{ detail.sourceDetail }}
              </p>
              <h2
                id="project-detail-title"
                class="truncate text-xl font-semibold tracking-[-0.02em] text-pf-text md:text-2xl"
              >
                {{ detail?.title || '项目详情' }}
              </h2>
            </div>
            <button
              type="button"
              class="pf-btn-ghost shrink-0 !px-3 !py-2"
              aria-label="关闭项目详情"
              @click="closeModal"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                aria-hidden="true"
              >
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div v-if="loading" class="flex flex-1 items-center justify-center p-16">
            <p class="text-sm text-pf-text-muted">加载中…</p>
          </div>

          <div v-else-if="detail" class="modal-body overflow-y-auto">
            <div class="grid md:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)] md:gap-0">
              <div
                v-if="activeImage || detail.gallery?.length"
                class="border-b border-pf-border bg-pf-bg-muted p-5 md:border-b-0 md:border-r md:p-8"
              >
                <img
                  v-if="activeImage"
                  :src="activeImage"
                  :alt="detail.title"
                  class="w-full rounded-pf-lg object-cover md:max-h-[22rem]"
                />
                <div v-if="detail.gallery?.length" class="mt-4 flex flex-wrap gap-2">
                  <button
                    v-for="image in detail.gallery"
                    :key="image"
                    type="button"
                    class="overflow-hidden rounded-md border transition duration-pf focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-pf-text"
                    :class="
                      activeImage === image
                        ? 'border-pf-text opacity-100'
                        : 'border-pf-border opacity-60 hover:opacity-100'
                    "
                    @click="activeImage = image"
                  >
                    <img :src="image" alt="" class="h-14 w-20 object-cover md:h-16 md:w-24" loading="lazy" />
                  </button>
                </div>
              </div>

              <div class="p-5 md:p-8">
                <div
                  v-if="detail.domain || detail.source || dateRange"
                  class="mb-6 flex flex-wrap gap-2"
                >
                  <span v-if="detail.domain" class="pf-tag-accent">
                    {{ getDomainLabel(detail.domain) }}
                  </span>
                  <span v-if="detail.source" class="pf-tag">
                    {{ getSourceLabel(detail.source) }}
                  </span>
                  <span v-if="dateRange" class="pf-tag">{{ dateRange }}</span>
                </div>

                <div v-if="detail.tags?.length || detail.techStack?.length" class="mb-8 flex flex-wrap gap-1.5">
                  <span v-for="tag in detail.tags || []" :key="tag" class="pf-tag">{{ tag }}</span>
                  <span v-for="tech in detail.techStack || []" :key="tech" class="pf-tag-accent">
                    {{ tech }}
                  </span>
                </div>

                <article class="prose-pf prose-sm md:prose-base" v-html="renderedDescription" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  background: color-mix(in srgb, var(--pf-text) 40%, transparent);
  backdrop-filter: blur(8px);
}

.modal-panel {
  border-radius: var(--pf-radius-xl) var(--pf-radius-xl) 0 0;
  box-shadow: var(--pf-shadow-lg);
}

@media (min-width: 640px) {
  .modal-panel {
    border-radius: var(--pf-radius-xl);
  }
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 220ms ease;
}

.modal-fade-enter-active .modal-panel,
.modal-fade-leave-active .modal-panel {
  transition: transform 220ms cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-panel {
  transform: translateY(1rem);
}

@media (min-width: 640px) {
  .modal-fade-enter-from .modal-panel {
    transform: translateY(0.5rem) scale(0.98);
  }
}
</style>
