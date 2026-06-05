<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VButton, VLoading, VPageHeader, VSpace, VTabItem, VTabs, Toast } from '@halo-dev/components'
import OptionManagerPanel from '@/components/OptionManagerPanel.vue'
import ProjectListPanel from '@/components/ProjectListPanel.vue'
import FormSection from '@/components/FormSection.vue'
import { getPortfolio, updatePortfolio } from '@/api/portfolio'
import { isValidPortfolioName } from '@/utils/portfolio'
import type { Portfolio, PortfolioSpec } from '@/types/portfolio'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('projects')
const portfolio = ref<Portfolio | null>(null)
const settingsForm = ref<PortfolioSpec | null>(null)

const portfolioName = computed(() => route.params.name as string)

async function fetchPortfolio() {
  if (!isValidPortfolioName(portfolioName.value)) {
    Toast.error('无效的作品集地址')
    router.replace({ name: 'Portfolios' })
    return
  }
  loading.value = true
  try {
    portfolio.value = await getPortfolio(portfolioName.value)
    settingsForm.value = { ...portfolio.value.spec }
  } catch (error) {
    console.error(error)
    Toast.error('加载作品集失败')
    router.push({ name: 'Portfolios' })
  } finally {
    loading.value = false
  }
}

async function handleSaveSettings() {
  if (!portfolio.value || !settingsForm.value) return
  saving.value = true
  try {
    await updatePortfolio({
      ...portfolio.value,
      spec: { ...settingsForm.value },
    })
    Toast.success('已保存')
    await fetchPortfolio()
  } catch (error) {
    console.error(error)
    Toast.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchPortfolio)
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else-if="portfolio && settingsForm" class="portfolio-admin">
    <VPageHeader
      :title="portfolio.spec.displayName"
      :description="`路由 /${portfolio.spec.slug} · ${portfolio.status?.projectCount ?? 0} 个项目`"
    >
      <template #actions>
        <VSpace>
          <VButton @click="router.push({ name: 'Portfolios' })">返回列表</VButton>
          <VButton
            v-if="activeTab === 'settings'"
            type="primary"
            :loading="saving"
            @click="handleSaveSettings"
          >
            保存设置
          </VButton>
        </VSpace>
      </template>
    </VPageHeader>

    <VTabs v-model:active-id="activeTab" type="outline" class="portfolio-detail__tabs">
      <VTabItem id="projects" label="项目列表">
        <ProjectListPanel :portfolio-name="portfolioName" />
      </VTabItem>
      <VTabItem id="options" label="选项字典">
        <OptionManagerPanel :portfolio-name="portfolioName" />
      </VTabItem>
      <VTabItem id="settings" label="基本信息">
        <div class="portfolio-detail__settings">
          <FormSection title="作品集设置" description="修改名称与公开状态，路由编码创建后不可更改">
            <FormKit
              v-model="settingsForm.displayName"
              type="text"
              label="作品集名称"
              validation="required"
            />
            <FormKit v-model="settingsForm.slug" type="text" label="路由编码" disabled />
            <FormKit v-model="settingsForm.description" type="textarea" label="描述" rows="3" />
            <FormKit v-model="settingsForm.priority" type="number" label="展示优先级" min="0" />
            <FormKit v-model="settingsForm.publicView" type="checkbox" label="设为公开" />
          </FormSection>
        </div>
      </VTabItem>
    </VTabs>
  </div>
</template>

<style scoped>
.portfolio-detail__tabs {
  margin-top: 1rem;
}

.portfolio-detail__settings {
  max-width: 42rem;
  padding-top: 1rem;
}
</style>
