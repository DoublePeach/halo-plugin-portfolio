<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VButton, VCard, VLoading, VPageHeader, VSpace, Toast } from '@halo-dev/components'
import FormSection from '@/components/FormSection.vue'
import { createPortfolio, getPortfolio, listPortfolios, updatePortfolioWithRetry } from '@/api/portfolio'
import { getApiErrorMessage } from '@/utils/extension'
import { isSlugTaken, withUpdateMetadata } from '@/utils/portfolio'
import type { Portfolio, PortfolioSpec } from '@/types/portfolio'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const originalPortfolio = ref<Portfolio | null>(null)

const portfolioName = computed(() => route.params.name as string | undefined)
const isEdit = computed(() => Boolean(portfolioName.value))

const formState = ref<PortfolioSpec>({
  displayName: '',
  slug: '',
  description: '',
  publicView: true,
  priority: 0,
})

function slugify(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .replace(/-{2,}/g, '-')
}

function handleNameInput() {
  if (!isEdit.value && !formState.value.slug) {
    formState.value.slug = slugify(formState.value.displayName)
  }
}

async function fetchPortfolio() {
  if (!portfolioName.value) {
    return
  }
  loading.value = true
  try {
    const portfolio = await getPortfolio(portfolioName.value)
    originalPortfolio.value = portfolio
    formState.value = { ...portfolio.spec }
  } catch (error) {
    console.error(error)
    Toast.error(getApiErrorMessage(error, '加载作品集失败'))
  } finally {
    loading.value = false
  }
}

async function validateSlugUnique() {
  const result = await listPortfolios()
  if (isSlugTaken(formState.value.slug, result.items, portfolioName.value)) {
    Toast.warning('路由编码已被其他作品集使用，请更换')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!formState.value.displayName?.trim()) {
    Toast.warning('请填写作品集名称')
    return
  }
  if (!formState.value.slug?.trim()) {
    Toast.warning('请填写路由编码')
    return
  }
  if (!/^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/.test(formState.value.slug)) {
    Toast.warning('路由编码仅支持小写字母、数字和连字符')
    return
  }

  saving.value = true
  try {
    if (!(await validateSlugUnique())) {
      return
    }

    const payload: Portfolio = {
      apiVersion: 'portfolio.plugin.halo.run/v1alpha1',
      kind: 'Portfolio',
      metadata: isEdit.value
        ? withUpdateMetadata(originalPortfolio.value!.metadata, portfolioName.value!)
        : { generateName: 'portfolio-' },
      spec: { ...formState.value },
      status: isEdit.value ? originalPortfolio.value?.status : undefined,
    }

    if (isEdit.value) {
      await updatePortfolioWithRetry(payload)
      Toast.success('更新成功')
      router.push({ name: 'PortfolioDetail', params: { name: portfolioName.value! } })
    } else {
      const created = await createPortfolio(payload)
      Toast.success('创建成功')
      router.push({ name: 'PortfolioDetail', params: { name: created.metadata.name! } })
    }
  } catch (error) {
    console.error(error)
    Toast.error(getApiErrorMessage(error, '保存失败'))
  } finally {
    saving.value = false
  }
}

onMounted(fetchPortfolio)
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="portfolio-admin">
    <VPageHeader :title="isEdit ? '编辑作品集' : '新建作品集'" description="配置作品集名称、路由编码与公开状态">
      <template #actions>
        <VSpace>
          <VButton @click="router.back()">返回</VButton>
          <VButton type="primary" :loading="saving" @click="handleSubmit">保存</VButton>
        </VSpace>
      </template>
    </VPageHeader>

    <VCard class="portfolio-admin__form-card">
      <FormSection title="基本信息" description="路由编码将用于前台访问路径，例如 /portfolio/{slug}">
        <FormKit
          v-model="formState.displayName"
          type="text"
          label="作品集名称"
          validation="required"
          @input="handleNameInput"
        />
        <FormKit
          v-model="formState.slug"
          type="text"
          label="路由编码"
          help="仅小写字母、数字、连字符，用于 URL，创建后不可修改"
          validation="required|matches:/^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/"
          :disabled="isEdit"
        />
        <FormKit
          v-model="formState.description"
          type="textarea"
          label="描述"
          rows="3"
        />
        <FormKit
          v-model="formState.priority"
          type="number"
          label="展示优先级"
          help="数值越大排序越靠前"
          min="0"
        />
        <FormKit
          v-model="formState.publicView"
          type="checkbox"
          label="设为公开"
          help="公开后可通过前台路由访问"
        />
      </FormSection>
    </VCard>
  </div>
</template>
