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
        class="fixed inset-0 z-50 flex items-center justify-center bg-pf-bg/80 backdrop-blur-md p-4 md:p-8 lg:p-12"
        role="presentation"
        @click.self="closeModal"
        @keydown="trapFocus"
      >
        <div
          ref="dialogRef"
          class="modal-panel flex max-h-full w-full max-w-6xl flex-col bg-pf-surface rounded-pf-xl shadow-pf-lg overflow-hidden border border-pf-border"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="detail ? 'project-detail-title' : undefined"
        >
          <div class="flex shrink-0 items-center justify-between border-b border-pf-border px-6 py-5 md:px-10 md:py-6 bg-pf-bg-soft">
            <h2
              id="project-detail-title"
              class="text-xl md:text-2xl font-medium tracking-tight text-pf-text"
              style="font-family: var(--pf-font-display)"
            >
              {{ detail?.title || 'Project Detail' }}
            </h2>
            <button
              type="button"
              class="rounded-full p-2 text-pf-text-muted hover:bg-pf-border hover:text-pf-text transition-colors"
              aria-label="Close"
              @click="closeModal"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div v-if="loading" class="flex flex-1 items-center justify-center p-32">
            <div class="flex flex-col items-center gap-4">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-pf-border border-t-pf-primary"></div>
              <p class="text-sm font-medium text-pf-text-subtle">Loading project details...</p>
            </div>
          </div>

          <div v-else-if="detail" class="overflow-y-auto flex-1 bg-pf-surface">
            <div class="grid lg:grid-cols-[1.2fr_1fr] min-h-[50vh]">
              <div
                v-if="activeImage || detail.gallery?.length"
                class="border-b lg:border-b-0 lg:border-r border-pf-border bg-pf-bg-muted p-6 md:p-10 flex flex-col"
              >
                <div class="relative rounded-pf-lg overflow-hidden shadow-sm bg-pf-surface flex-1 flex items-center justify-center min-h-[300px]">
                  <img
                    v-if="activeImage"
                    :src="activeImage"
                    :alt="detail.title"
                    class="w-full h-full object-contain max-h-[60vh] transition-opacity duration-300"
                  />
                  <div v-else class="text-pf-text-subtle">No image available</div>
                </div>
                
                <div v-if="detail.gallery?.length" class="mt-6 flex gap-3 overflow-x-auto pb-2 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
                  <button
                    v-for="image in detail.gallery"
                    :key="image"
                    type="button"
                    class="shrink-0 overflow-hidden rounded-md border-2 transition-all duration-pf"
                    :class="
                      activeImage === image
                        ? 'border-pf-primary shadow-sm'
                        : 'border-transparent opacity-70 hover:opacity-100'
                    "
                    @click="activeImage = image"
                  >
                    <img :src="image" alt="" class="h-16 w-24 object-cover" loading="lazy" />
                  </button>
                </div>
              </div>

              <div class="p-6 md:p-10 flex flex-col">
                <div class="mb-8 flex flex-col gap-5 border-b border-pf-border pb-8">
                  <div class="flex flex-wrap items-center gap-3">
                    <span v-if="detail.domain" class="inline-flex items-center rounded-full bg-pf-primary-soft px-3 py-1 text-sm font-medium text-pf-primary">
                      {{ getDomainLabel(detail.domain) }}
                    </span>
                    <span v-if="dateRange" class="inline-flex items-center rounded-full bg-pf-bg-muted px-3 py-1 text-sm font-medium text-pf-text-muted">
                      {{ dateRange }}
                    </span>
                  </div>

                  <div v-if="detail.tags?.length || detail.techStack?.length" class="flex flex-wrap gap-2">
                    <span v-for="tag in detail.tags || []" :key="tag" class="pf-tag">
                      {{ tag }}
                    </span>
                    <span v-for="tech in detail.techStack || []" :key="tech" class="pf-tag border border-pf-border bg-transparent">
                      {{ tech }}
                    </span>
                  </div>
                </div>

                <article class="prose prose-slate max-w-none prose-headings:font-display prose-headings:font-medium prose-a:text-pf-primary prose-a:no-underline hover:prose-a:underline" v-html="renderedDescription" />
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
