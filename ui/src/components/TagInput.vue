<script setup lang="ts">
import { computed } from 'vue'
import RiCloseLine from '~icons/ri/close-line'

const model = defineModel<string>({ default: '' })

const props = defineProps<{
  label: string
  placeholder?: string
  hint?: string
  suggestions?: string[]
}>()

const tags = computed({
  get() {
    return model.value
      .split(/[,，]/)
      .map((item) => item.trim())
      .filter(Boolean)
  },
  set(values: string[]) {
    model.value = values.join(', ')
  },
})

function addTag(value: string) {
  const normalized = value.trim()
  if (!normalized || tags.value.includes(normalized)) {
    return
  }
  tags.value = [...tags.value, normalized]
}

function removeTag(index: number) {
  tags.value = tags.value.filter((_, itemIndex) => itemIndex !== index)
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter' && event.key !== ',') {
    return
  }
  event.preventDefault()
  const input = event.target as HTMLInputElement
  addTag(input.value)
  input.value = ''
}

function handleBlur(event: FocusEvent) {
  const input = event.target as HTMLInputElement
  if (!input.value.trim()) {
    return
  }
  addTag(input.value)
  input.value = ''
}
</script>

<template>
  <div class="pf-field pf-tag-input">
    <label class="pf-field__label">{{ label }}</label>
    <div v-if="tags.length" class="pf-tag-input__chips">
      <span v-for="(tag, index) in tags" :key="`${tag}-${index}`" class="pf-chip">
        {{ tag }}
        <button
          type="button"
          class="pf-chip__remove"
          :aria-label="`移除标签 ${tag}`"
          @click="removeTag(index)"
        >
          <RiCloseLine />
        </button>
      </span>
    </div>
    <input
      class="pf-control"
      type="text"
      :placeholder="placeholder"
      @keydown="handleKeydown"
      @blur="handleBlur"
    />
    <p v-if="hint" class="pf-field__hint">{{ hint }}</p>
    <div v-if="suggestions?.length" class="pf-tag-input__suggestions">
      <button
        v-for="item in suggestions"
        :key="item"
        type="button"
        class="pf-chip pf-chip--suggestion"
        @click="addTag(item)"
      >
        + {{ item }}
      </button>
    </div>
  </div>
</template>
