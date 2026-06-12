<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  VButton,
  VCard,
  VEmpty,
  VLoading,
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
import { deleteProject, listProjects } from '@/api/portfolio-project'
import { formatDate } from '@/utils/date'
import { getApiErrorMessage } from '@/utils/extension'
import { isValidPortfolioName } from '@/utils/portfolio'
import type { PortfolioOption, PortfolioProject } from '@/types/portfolio'

const props = defineProps<{
  portfolioName: string
}>()

const router = useRouter()
const loading = ref(false)
const projects = ref<PortfolioProject[]>([])
const options = ref<PortfolioOption[]>([])
const keyword = ref('')
const statusFilter = ref<'all' | 'published' | 'draft' | 'featured'>('all')

const projectCount = computed(() => projects.value.length)

const optionLabelMap = computed(() => {
  const map = new Map<string, string>()
  for (const option of options.value) {
    map.set(`${option.spec.type}:${option.spec.value}`, option.spec.label)
  }
  return map
})

const publishedCount = computed(
  () => projects.value.filter((item) => item.spec.published).length,
)
const draftCount = computed(() => projects.value.length - publishedCount.value)
const featuredCount = computed(
  () => projects.value.filter((item) => item.spec.featured).length,
)

const filteredProjects = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  return projects.value.filter((project) => {
    const spec = project.spec
    if (statusFilter.value === 'published' && !spec.published) return false
    if (statusFilter.value === 'draft' && spec.published) return false
    if (statusFilter.value === 'featured' && !spec.featured) return false
    if (!query) return true
    const haystack = [spec.title, spec.summary, ...(spec.techStack || [])]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(query)
  })
})

function getLabel(type: string, value?: string) {
  if (!value) return '-'
  return optionLabelMap.value.get(`${type}:${value}`) ?? value
}

async function fetchProjects() {
  const projectResult = await listProjects(props.portfolioName)
  projects.value = projectResult.items
}

async function fetchData(silent = false) {
  if (!isValidPortfolioName(props.portfolioName)) {
    return
  }
  if (!silent) {
    loading.value = true
  }
  try {
    await fetchProjects()
  } catch (error) {
    console.error(error)
    Toast.error(getApiErrorMessage(error, '加载项目列表失败'))
  }
  try {
    const optionResult = await listPortfolioOptions(props.portfolioName)
    options.value = optionResult.items
  } catch (error) {
    console.error(error)
    Toast.warning('加载选项字典失败，领域/来源/技术栈标签可能无法显示')
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

function handleCreate() {
  router.push({
    name: 'PortfolioProjectCreate',
    params: { name: props.portfolioName },
  })
}

function handleEdit(projectName: string) {
  router.push({
    name: 'PortfolioProjectEdit',
    params: { name: props.portfolioName, projectName },
  })
}

function handleDelete(project: PortfolioProject) {
  Dialog.warning({
    title: '删除项目',
    description: `确定删除「${project.spec.title}」吗？此操作不可恢复。`,
    onConfirm: async () => {
      const name = project.metadata.name!
      try {
        await deleteProject(name)
        projects.value = projects.value.filter((item) => item.metadata.name !== name)
        Toast.success('删除成功')
        fetchData(true).catch(console.error)
      } catch (error) {
        console.error(error)
        Toast.error(getApiErrorMessage(error, '删除失败'))
      }
    },
  })
}

watch(() => props.portfolioName, () => fetchData(), { immediate: true })
onMounted(() => fetchData())

defineExpose({ refresh: () => fetchData(true), projectCount })
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="project-list-panel">
    <div class="portfolio-admin__stats">
      <StatCard label="全部项目" :value="projects.length" hint="当前作品集" tone="primary" />
      <StatCard label="已发布" :value="publishedCount" hint="公开可见" tone="success" />
      <StatCard label="草稿" :value="draftCount" hint="未公开" tone="warning" />
      <StatCard label="核心展示" :value="featuredCount" hint="建议 4-6 个" tone="accent" />
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
              placeholder="搜索标题、技术栈..."
            />
          </div>
        </label>
        <label class="portfolio-admin__select pf-field !mb-0">
          <select v-model="statusFilter" class="pf-control">
            <option value="all">所有状态</option>
            <option value="published">已发布</option>
            <option value="draft">草稿</option>
            <option value="featured">核心项目</option>
          </select>
        </label>
        <div class="ml-auto">
          <VButton type="primary" @click="handleCreate">
            <template #icon><RiAddLine /></template>
            新建项目
          </VButton>
        </div>
      </div>
    </VCard>

    <VCard v-if="filteredProjects.length === 0" :body-style="{ padding: '3rem 1rem' }">
      <VEmpty
        :title="projects.length ? '没有匹配的项目' : '暂无项目'"
        :description="projects.length ? '调整筛选条件' : '创建第一个项目'"
      >
        <template #actions>
          <VButton type="primary" @click="handleCreate">新建项目</VButton>
        </template>
      </VEmpty>
    </VCard>

    <VCard v-else :body-style="{ padding: '0' }">
      <div
        v-for="project in filteredProjects"
        :key="project.metadata.name"
        class="pf-list-row"
      >
        <div class="pf-project-row__cover">
          <img
            v-if="project.spec.coverImage"
            :src="project.spec.coverImage"
            :alt="project.spec.title"
            loading="lazy"
          />
          <span v-else>{{ project.spec.title.slice(0, 1) }}</span>
        </div>
        <div class="pf-list-row__content">
          <div class="pf-list-row__header">
            <h3 class="pf-list-row__title">{{ project.spec.title }}</h3>
            <VTag v-if="project.spec.featured">核心</VTag>
            <VStatusDot
              :state="project.spec.published ? 'success' : 'warning'"
              :text="project.spec.published ? '已发布' : '草稿'"
            />
          </div>
          <p class="pf-list-row__desc">{{ project.spec.summary || '暂无简介' }}</p>
          <div class="pf-list-row__meta">
            <span>领域：<strong class="text-pf-text">{{ getLabel('DOMAIN', project.spec.domain) }}</strong></span>
            <span>来源：<strong class="text-pf-text">{{ getLabel('SOURCE', project.spec.source) }}</strong></span>
            <span>时间：<strong class="text-pf-text">{{ formatDate(project.spec.startDate) }}</strong></span>
          </div>
        </div>
        <div class="pf-list-row__actions">
          <VButton @click="handleEdit(project.metadata.name!)">编辑</VButton>
          <VButton type="danger" @click="handleDelete(project)">删除</VButton>
        </div>
      </div>
    </VCard>
  </div>
</template>
