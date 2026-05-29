import { onMounted, onUnmounted, ref } from 'vue'

export function useClarityTheme() {
  const isDark = ref(false)

  function syncFromDom() {
    isDark.value = document.documentElement.classList.contains('dark')
  }

  let observer: MutationObserver | null = null
  let mediaQuery: MediaQueryList | null = null

  onMounted(() => {
    syncFromDom()
    observer = new MutationObserver(syncFromDom)
    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class'],
    })
    mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', syncFromDom)
  })

  onUnmounted(() => {
    observer?.disconnect()
    mediaQuery?.removeEventListener('change', syncFromDom)
  })

  return { isDark }
}
