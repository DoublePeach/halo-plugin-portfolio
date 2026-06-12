<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { marked } from 'marked'
import { fetchProjectDetail } from '@/api/portfolio'
import { getDomainLabel } from '@/constants/labels'
import { usePortfolioStore } from '@/stores/portfolio'
import type { PortfolioProject } from '@/types/portfolio'

const store = usePortfolioStore()
const loading = ref(false)
const detail = ref<PortfolioProject | null>(null)
const activeImage = ref('')
const dialogRef = ref<HTMLElement | null>(null)

const renderedDescription = computed(() => {
  if (!detail.value?.description) {
    return '<p class="text-pf-text-muted font-light">No detailed description provided.</p>'
  }
  return marked.parse(detail.value.description)
})

const dateRange = computed(() => {
  if (!detail.value) return ''
  const start = detail.value.startDate
    ? new Date(detail.value.startDate).getFullYear()
    : ''
  const end = detail.value.endDate
    ? new Date(detail.value.endDate).getFullYear()
    : 'Present'
  if (!start) return ''
  if (start === end) return `${start}`
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
        class="fixed inset-0 z-50 flex items-center justify-center bg-pf-bg/95 backdrop-blur-sm p-4 md:p-12"
        role="presentation"
        @click.self="closeModal"
        @keydown="trapFocus"
      >
        <div
          ref="dialogRef"
          class="modal-panel flex max-h-full w-full max-w-7xl flex-col bg-pf-surface border border-pf-border overflow-hidden"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="detail ? 'project-detail-title' : undefined"
        >
          <div class="flex shrink-0 items-center justify-between border-b border-pf-border px-6 py-6 md:px-12 md:py-8">
            <h2
              id="project-detail-title"
              class="text-2xl md:text-4xl font-light tracking-tight text-pf-text uppercase"
              style="font-family: var(--pf-font-display)"
            >
              {{ detail?.title || 'Project Detail' }}
            </h2>
            <button
              type="button"
              class="text-pf-text-muted hover:text-pf-text transition-colors"
              aria-label="Close"
              @click="closeModal"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div v-if="loading" class="flex flex-1 items-center justify-center p-32">
            <p class="text-sm uppercase tracking-widest text-pf-text-subtle">Loading</p>
          </div>

          <div v-else-if="detail" class="overflow-y-auto">
            <div class="grid lg:grid-cols-[1.5fr_1fr] min-h-[50vh]">
              <div
                v-if="activeImage || detail.gallery?.length"
                class="border-b lg:border-b-0 lg:border-r border-pf-border bg-pf-bg p-6 md:p-12 flex flex-col justify-between"
              >
                <img
                  v-if="activeImage"
                  :src="activeImage"
                  :alt="detail.title"
                  class="w-full object-cover filter grayscale hover:grayscale-0 transition-all duration-1000 max-h-[60vh]"
                />
                <div v-if="detail.gallery?.length" class="mt-8 flex flex-wrap gap-4">
                  <button
                    v-for="image in detail.gallery"
                    :key="image"
                    type="button"
                    class="overflow-hidden border transition-all duration-pf"
                    :class="
                      activeImage === image
                        ? 'border-pf-text filter grayscale-0'
                        : 'border-pf-border filter grayscale hover:grayscale-0'
                    "
                    @click="activeImage = image"
                  >
                    <img :src="image" alt="" class="h-20 w-32 object-cover" loading="lazy" />
                  </button>
                </div>
              </div>

              <div class="p-6 md:p-12 bg-pf-surface">
                <div class="mb-12 flex flex-col gap-6 border-b border-pf-border pb-8">
                  <div class="flex items-center gap-4 text-xs uppercase tracking-widest text-pf-text-subtle">
                    <span v-if="detail.domain">{{ getDomainLabel(detail.domain) }}</span>
                    <span v-if="dateRange" class="w-8 h-px bg-pf-border"></span>
                    <span v-if="dateRange">{{ dateRange }}</span>
                  </div>

                  <div v-if="detail.tags?.length || detail.techStack?.length" class="flex flex-wrap gap-x-4 gap-y-2">
                    <span v-for="tag in detail.tags || []" :key="tag" class="text-xs uppercase tracking-widest text-pf-text">{{ tag }}</span>
                    <span v-for="tech in detail.techStack || []" :key="tech" class="text-xs uppercase tracking-widest text-pf-text-muted">{{ tech }}</span>
                  </div>
                </div>

                <article class="prose-pf prose-sm md:prose-base font-light" v-html="renderedDescription" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 400ms ease;
}

.modal-fade-enter-active .modal-panel,
.modal-fade-leave-active .modal-panel {
  transition: transform 400ms cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from .modal-panel {
  transform: translateY(2rem);
}
</style>
