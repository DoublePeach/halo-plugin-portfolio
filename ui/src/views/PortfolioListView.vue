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
  <div v-else class="portfolio-admin p-6 max-w-7xl mx-auto">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 mb-2">作品集管理</h1>
        <p class="text-gray-500 text-sm">创建并管理多个独立作品集，每个作品集拥有独立路由与项目列表</p>
      </div>
      <VButton type="primary" @click="handleCreate" class="shadow-sm">
        <template #icon><RiAddLine /></template>
        新建作品集
      </VButton>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <StatCard label="作品集总数" :value="portfolios.length" hint="已创建的作品集数量" tone="primary" />
      <StatCard label="公开访问" :value="publicCount" hint="允许前台访问的作品集" tone="success" />
      <StatCard label="项目总数" :value="totalProjects" hint="所有作品集项目合计" tone="accent" />
    </div>

    <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100 mb-6">
      <div class="relative max-w-md">
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <RiSearchLine class="text-gray-400" />
        </div>
        <input
          v-model="keyword"
          class="block w-full pl-10 pr-3 py-2 border border-gray-200 rounded-md leading-5 bg-gray-50 placeholder-gray-400 focus:outline-none focus:bg-white focus:ring-1 focus:ring-blue-500 focus:border-blue-500 sm:text-sm transition duration-150 ease-in-out"
          type="search"
          placeholder="搜索作品集名称或路由..."
        />
      </div>
    </div>

    <div v-if="filteredPortfolios.length === 0" class="bg-white rounded-lg shadow-sm border border-gray-100 p-16 text-center">
      <VEmpty
        :title="portfolios.length ? '没有匹配的作品集' : '暂无作品集'"
        :description="portfolios.length ? '尝试调整搜索关键词' : '创建第一个作品集，开始管理你的项目经历'"
      >
        <template #actions>
          <VButton type="primary" @click="handleCreate">新建作品集</VButton>
        </template>
      </VEmpty>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
      <div
        v-for="portfolio in filteredPortfolios"
        :key="portfolio.metadata.name"
        class="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow duration-200 flex flex-col"
      >
        <div class="p-6 flex-1">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-lg font-semibold text-gray-800 line-clamp-1" :title="portfolio.spec.displayName">
              {{ portfolio.spec.displayName }}
            </h3>
            <VStatusDot
              :state="portfolio.spec.publicView ? 'success' : 'warning'"
              :text="portfolio.spec.publicView ? '公开' : '私有'"
              class="shrink-0 ml-2"
            />
          </div>
          
          <div class="mb-4">
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
              /{{ portfolio.spec.slug }}
            </span>
          </div>
          
          <p class="text-sm text-gray-500 line-clamp-2 mb-6 h-10">
            {{ portfolio.spec.description || '暂无描述' }}
          </p>
          
          <div class="flex items-center space-x-4 text-sm text-gray-600 bg-gray-50 p-3 rounded-md">
            <div class="flex flex-col">
              <span class="text-xs text-gray-400 mb-1">项目数</span>
              <span class="font-semibold">{{ portfolio.status?.projectCount ?? 0 }}</span>
            </div>
            <div class="w-px h-8 bg-gray-200"></div>
            <div class="flex flex-col">
              <span class="text-xs text-gray-400 mb-1">优先级</span>
              <span class="font-semibold">{{ portfolio.spec.priority || 0 }}</span>
            </div>
          </div>
        </div>
        
        <div class="bg-gray-50 px-6 py-4 border-t border-gray-100 flex justify-between items-center gap-2">
          <div class="flex gap-2">
            <VButton size="sm" @click="handleEdit(portfolio.metadata.name!)">编辑</VButton>
            <VButton size="sm" type="danger" @click="handleDelete(portfolio)">删除</VButton>
          </div>
          <VButton size="sm" type="primary" @click="handleOpen(portfolio.metadata.name!)">管理项目</VButton>
        </div>
      </div>
    </div>
  </div>
</template>
