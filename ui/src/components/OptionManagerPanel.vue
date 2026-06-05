<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { VButton, VCard, VEmpty, VLoading, VSpace, Dialog, Toast } from '@halo-dev/components'
import RiAddLine from '~icons/ri/add-line'
import FormSection from '@/components/FormSection.vue'
import {
  createPortfolioOption,
  deletePortfolioOption,
  listPortfolioOptions,
  slugify,
  updatePortfolioOption,
} from '@/api/portfolio-option'
import { isValidPortfolioName } from '@/utils/portfolio'
import type { PortfolioOption, PortfolioOptionType } from '@/types/portfolio'

const props = defineProps<{
  portfolioName: string
}>()

const loading = ref(false)
const saving = ref(false)
const options = ref<PortfolioOption[]>([])

const sections: { type: PortfolioOptionType; title: string; description: string }[] = [
  { type: 'TECH_STACK', title: '技术栈', description: '项目表单中的多选技术栈选项' },
  { type: 'SOURCE', title: '项目来源', description: '项目表单中的单选来源选项' },
  { type: 'DOMAIN', title: '项目领域', description: '项目表单中的单选领域选项' },
]

const optionsByType = computed(() => {
  const map: Record<PortfolioOptionType, PortfolioOption[]> = {
    TECH_STACK: [],
    SOURCE: [],
    DOMAIN: [],
  }
  for (const option of options.value) {
    map[option.spec.type]?.push(option)
  }
  for (const type of Object.keys(map) as PortfolioOptionType[]) {
    map[type].sort((a, b) => (a.spec.sortOrder ?? 0) - (b.spec.sortOrder ?? 0))
  }
  return map
})

const draftForms = ref<Record<PortfolioOptionType, { label: string; value: string; sortOrder: number }>>({
  TECH_STACK: { label: '', value: '', sortOrder: 0 },
  SOURCE: { label: '', value: '', sortOrder: 0 },
  DOMAIN: { label: '', value: '', sortOrder: 0 },
})

async function fetchOptions() {
  if (!isValidPortfolioName(props.portfolioName)) {
    return
  }
  loading.value = true
  try {
    const result = await listPortfolioOptions(props.portfolioName)
    options.value = result.items
  } catch (error) {
    console.error(error)
    Toast.error('加载选项失败')
  } finally {
    loading.value = false
  }
}

function autoValue(type: PortfolioOptionType) {
  const draft = draftForms.value[type]
  if (!draft.value && draft.label) {
    draft.value = slugify(draft.label)
  }
}

async function handleAdd(type: PortfolioOptionType) {
  const draft = draftForms.value[type]
  if (!draft.label.trim()) {
    Toast.warning('请填写显示文本')
    return
  }
  const value = (draft.value || slugify(draft.label)).trim()
  if (!value) {
    Toast.warning('请填写选项值')
    return
  }
  const exists = options.value.some((o) => o.spec.type === type && o.spec.value === value)
  if (exists) {
    Toast.warning('该选项值已存在')
    return
  }

  saving.value = true
  try {
    await createPortfolioOption({
      apiVersion: 'portfolio.plugin.halo.run/v1alpha1',
      kind: 'PortfolioOption',
      metadata: { generateName: 'portfolio-option-' },
      spec: {
        portfolioName: props.portfolioName,
        type,
        label: draft.label.trim(),
        value,
        sortOrder: draft.sortOrder ?? optionsByType.value[type].length * 10,
      },
    })
    draft.label = ''
    draft.value = ''
    draft.sortOrder = 0
    Toast.success('添加成功')
    await fetchOptions()
  } catch (error) {
    console.error(error)
    Toast.error('添加失败')
  } finally {
    saving.value = false
  }
}

async function handleUpdate(option: PortfolioOption) {
  saving.value = true
  try {
    await updatePortfolioOption(option)
    Toast.success('已保存')
  } catch (error) {
    console.error(error)
    Toast.error('保存失败')
    await fetchOptions()
  } finally {
    saving.value = false
  }
}

function handleDelete(option: PortfolioOption) {
  Dialog.warning({
    title: '删除选项',
    description: `确定删除「${option.spec.label}」吗？已引用该值的项目不会自动更新。`,
    onConfirm: async () => {
      try {
        await deletePortfolioOption(option.metadata.name!)
        Toast.success('删除成功')
        await fetchOptions()
      } catch (error) {
        console.error(error)
        Toast.error('删除失败')
      }
    },
  })
}

watch(() => props.portfolioName, fetchOptions, { immediate: true })
onMounted(fetchOptions)
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="option-manager">
    <VCard v-for="section in sections" :key="section.type" class="option-manager__section">
      <FormSection :title="section.title" :description="section.description">
        <div v-if="optionsByType[section.type].length" class="option-table">
          <div class="option-table__head">
            <span>显示文本</span>
            <span>选项值</span>
            <span>排序</span>
            <span>操作</span>
          </div>
          <div
            v-for="option in optionsByType[section.type]"
            :key="option.metadata.name"
            class="option-table__row"
          >
            <input v-model="option.spec.label" class="pf-control" @change="handleUpdate(option)" />
            <input
              v-model="option.spec.value"
              class="pf-control"
              disabled
              title="选项值创建后不可修改"
            />
            <input
              v-model.number="option.spec.sortOrder"
              class="pf-control"
              type="number"
              min="0"
              @change="handleUpdate(option)"
            />
            <VButton size="sm" type="danger" @click="handleDelete(option)">删除</VButton>
          </div>
        </div>
        <VEmpty v-else title="暂无选项" description="添加第一个选项供项目表单使用" />

        <div class="option-add">
          <input
            v-model="draftForms[section.type].label"
            class="pf-control"
            placeholder="显示文本，如 Java"
            @input="autoValue(section.type)"
          />
          <input
            v-model="draftForms[section.type].value"
            class="pf-control"
            placeholder="选项值（自动生成）"
          />
          <input
            v-model.number="draftForms[section.type].sortOrder"
            class="pf-control"
            type="number"
            min="0"
            placeholder="排序"
          />
          <VButton :loading="saving" @click="handleAdd(section.type)">
            <template #icon><RiAddLine /></template>
            添加
          </VButton>
        </div>
      </FormSection>
    </VCard>
  </div>
</template>

<style scoped>
.option-manager {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-table__head,
.option-table__row {
  display: grid;
  grid-template-columns: 1.2fr 1fr 5rem 5rem;
  gap: 0.5rem;
  align-items: center;
}

.option-table__head {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--pf-text-muted);
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--pf-border);
}

.option-table__row {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--pf-border);
}

.option-add {
  display: grid;
  grid-template-columns: 1.2fr 1fr 5rem auto;
  gap: 0.5rem;
  align-items: center;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px dashed var(--pf-border);
}

@media (max-width: 768px) {
  .option-table__head,
  .option-table__row,
  .option-add {
    grid-template-columns: 1fr;
  }
}
</style>
