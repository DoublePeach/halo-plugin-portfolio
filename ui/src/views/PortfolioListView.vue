<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
  VStatusDot,
  VTag,
  Dialog,
  Toast,
} from '@halo-dev/components'
import RiAddLine from '~icons/ri/add-line'
import RiSearchLine from '~icons/ri/search-line'
import StatCard from '@/components/StatCard.vue'
import { listPortfolioOptions } from '@/api/portfolio-option'
import { listProjects } from '@/api/portfolio-project'
import { deletePortfolio, listPortfolios } from '@/api/portfolio'
import { filterActiveExtensions, getApiErrorMessage } from '@/utils/extension'
import type { Portfolio } from '@/types/portfolio'

const router = useRouter()
const loading = ref(false)
const portfolios = ref<Portfolio[]>([])
const keyword = ref('')

const publicCount = computed(
  () => portfolios.value.filter((item) => item.spec.publicView).length,
)
const totalProjects = computed(() =>
  portfolios.value.reduce((sum, item) => sum + (item.status?.projectCount ?? 0), 0),
)

const filteredPortfolios = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) {
    return portfolios.value
  }
  return portfolios.value.filter((portfolio) => {
    const haystack = [portfolio.spec.displayName, portfolio.spec.slug, portfolio.spec.description]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(query)
  })
})

async function fetchPortfolios(silent = false) {
  if (!silent) {
    loading.value = true
  }
  try {
    const result = await listPortfolios()
    portfolios.value = result.items
  } catch (error) {
    console.error(error)
    Toast.error(getApiErrorMessage(error, '加载作品集列表失败'))
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

function handleCreate() {
  router.push({ name: 'PortfolioCreate' })
}

function handleOpen(name: string) {
  router.push({ name: 'PortfolioDetail', params: { name } })
}

function handleEdit(name: string) {
  router.push({ name: 'PortfolioEdit', params: { name } })
}

async function getPortfolioChildCounts(name: string) {
  const [projectResult, optionResult] = await Promise.all([
    listProjects(name),
    listPortfolioOptions(name),
  ])
  return {
    projectCount: filterActiveExtensions(projectResult.items).length,
    optionCount: filterActiveExtensions(optionResult.items).length,
  }
}

async function handleDelete(portfolio: Portfolio) {
  const name = portfolio.metadata.name!
  try {
    const { projectCount, optionCount } = await getPortfolioChildCounts(name)
    if (projectCount > 0 || optionCount > 0) {
      Toast.warning(
        `无法删除：该作品集下仍有 ${projectCount} 个项目、${optionCount} 个选项，请先迁移或删除后再试。`,
      )
      return
    }
  } catch (error) {
    console.error(error)
    Toast.error(getApiErrorMessage(error, '无法校验作品集关联数据'))
    return
  }

  Dialog.warning({
    title: '删除作品集',
    description: `确定删除「${portfolio.spec.displayName}」吗？此操作不可恢复。`,
    onConfirm: async () => {
      try {
        await deletePortfolio(name)
        portfolios.value = portfolios.value.filter((item) => item.metadata.name !== name)
        Toast.success('删除成功')
        fetchPortfolios(true).catch(console.error)
      } catch (error) {
        console.error(error)
        Toast.error(getApiErrorMessage(error, '删除失败'))
      }
    },
  })
}

onMounted(() => fetchPortfolios())
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="portfolio-admin">
    <VPageHeader title="作品集管理" description="创建并管理多个独立作品集，每个作品集拥有独立路由与项目列表">
      <template #actions>
        <VButton type="primary" @click="handleCreate">
          <template #icon><RiAddLine /></template>
          新建作品集
        </VButton>
      </template>
    </VPageHeader>

    <div class="portfolio-admin__stats">
      <StatCard label="作品集" :value="portfolios.length" hint="已创建的作品集数量" tone="primary" />
      <StatCard label="公开" :value="publicCount" hint="允许前台访问" tone="success" />
      <StatCard label="项目总数" :value="totalProjects" hint="所有作品集项目合计" tone="accent" />
    </div>

    <VCard class="portfolio-admin__toolbar" :body-style="{ padding: '1rem' }">
      <div class="portfolio-admin__filters">
        <label class="portfolio-admin__search pf-field !mb-0">
          <div class="search-input">
            <RiSearchLine class="search-input__icon" />
            <input
              v-model="keyword"
              class="pf-control search-input__control"
              type="search"
              placeholder="搜索名称或路由..."
            />
          </div>
        </label>
      </div>
    </VCard>

    <VCard v-if="filteredPortfolios.length === 0" :body-style="{ padding: '3rem 1rem' }">
      <VEmpty
        :title="portfolios.length ? '没有匹配的作品集' : '暂无作品集'"
        :description="portfolios.length ? '尝试调整搜索关键词' : '创建第一个作品集，开始管理你的项目经历'"
      >
        <template #actions>
          <VButton type="primary" @click="handleCreate">新建作品集</VButton>
        </template>
      </VEmpty>
    </VCard>

    <VCard v-else :body-style="{ padding: '0' }">
      <div
        v-for="portfolio in filteredPortfolios"
        :key="portfolio.metadata.name"
        class="pf-list-row"
      >
        <div class="pf-list-row__content">
          <div class="pf-list-row__header">
            <h3 class="pf-list-row__title">{{ portfolio.spec.displayName }}</h3>
            <VTag>/{{ portfolio.spec.slug }}</VTag>
            <VStatusDot
              :state="portfolio.spec.publicView ? 'success' : 'warning'"
              :text="portfolio.spec.publicView ? '公开' : '私有'"
            />
          </div>
          <p class="pf-list-row__desc">
            {{ portfolio.spec.description || '暂无描述' }}
          </p>
          <div class="pf-list-row__meta">
            <span>项目数：<strong class="text-pf-text">{{ portfolio.status?.projectCount ?? 0 }}</strong></span>
            <span v-if="portfolio.spec.priority">优先级：<strong class="text-pf-text">{{ portfolio.spec.priority }}</strong></span>
          </div>
        </div>
        <div class="pf-list-row__actions">
          <VButton @click="handleEdit(portfolio.metadata.name!)">编辑</VButton>
          <VButton type="danger" @click="handleDelete(portfolio)">删除</VButton>
          <VButton type="primary" @click="handleOpen(portfolio.metadata.name!)">管理项目</VButton>
        </div>
      </div>
    </VCard>
  </div>
</template>
