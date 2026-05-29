<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  VAlert,
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
import { deleteProject, listProjects } from '@/api/portfolio'
import {
  DOMAIN_OPTIONS,
  SOURCE_OPTIONS,
  getDomainLabel,
  getSourceLabel,
} from '@/constants/options'
import type { PortfolioProject } from '@/types/portfolio'

type StatusFilter = 'all' | 'published' | 'draft' | 'featured'

const router = useRouter()
const loading = ref(false)
const projects = ref<PortfolioProject[]>([])
const keyword = ref('')
const domainFilter = ref('all')
const sourceFilter = ref('all')
const statusFilter = ref<StatusFilter>('all')

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

    if (domainFilter.value !== 'all' && spec.domain !== domainFilter.value) {
      return false
    }
    if (sourceFilter.value !== 'all' && spec.source !== sourceFilter.value) {
      return false
    }
    if (statusFilter.value === 'published' && !spec.published) {
      return false
    }
    if (statusFilter.value === 'draft' && spec.published) {
      return false
    }
    if (statusFilter.value === 'featured' && !spec.featured) {
      return false
    }
    if (!query) {
      return true
    }

    const haystack = [
      spec.title,
      spec.summary,
      spec.sourceDetail,
      ...(spec.tags || []),
      ...(spec.techStack || []),
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    return haystack.includes(query)
  })
})

async function fetchProjects() {
  loading.value = true
  try {
    const result = await listProjects()
    projects.value = result.items
  } catch (error) {
    console.error(error)
    Toast.error('加载项目列表失败')
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  router.push({ name: 'PortfolioProjectEdit' })
}

function handleEdit(name: string) {
  router.push({ name: 'PortfolioProjectEdit', params: { name } })
}

async function handleDelete(project: PortfolioProject) {
  Dialog.warning({
    title: '删除项目',
    description: `确定删除「${project.spec.title}」吗？此操作不可恢复。`,
    onConfirm: async () => {
      try {
        await deleteProject(project.metadata.name!)
        Toast.success('删除成功')
        await fetchProjects()
      } catch (error) {
        console.error(error)
        Toast.error('删除失败')
      }
    },
  })
}

function formatDate(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleDateString('zh-CN')
}

function resetFilters() {
  keyword.value = ''
  domainFilter.value = 'all'
  sourceFilter.value = 'all'
  statusFilter.value = 'all'
}

onMounted(fetchProjects)
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="portfolio-admin">
    <div class="portfolio-admin__stack">
      <VPageHeader title="作品集管理" description="管理前台展示的核心项目、时间线与分类视图数据">
        <template #actions>
          <VButton type="primary" @click="handleCreate">
            <template #icon>
              <RiAddLine />
            </template>
            新建项目
          </VButton>
        </template>
      </VPageHeader>

      <div class="portfolio-admin__stats">
        <StatCard label="全部项目" :value="projects.length" hint="当前作品集总量" tone="primary" />
        <StatCard label="已发布" :value="publishedCount" hint="前台可见项目" tone="success" />
        <StatCard label="草稿" :value="draftCount" hint="待完善或未公开" tone="warning" />
        <StatCard label="核心展示" :value="featuredCount" hint="建议控制在 4-6 个" tone="accent" />
      </div>

      <VAlert
        v-if="featuredCount > 6"
        type="warning"
        title="核心项目数量偏多"
        description="前台默认展示 4-6 个核心项目。建议通过优先级和 featured 标记精简展示列表。"
        closable
      />

      <VCard>
        <div class="portfolio-admin__filters">
          <label class="portfolio-admin__search pf-field">
            <span class="pf-field__label">搜索</span>
            <div class="search-input">
              <RiSearchLine class="search-input__icon" />
              <input
                v-model="keyword"
                class="pf-control search-input__control"
                type="search"
                placeholder="标题、标签、技术栈、来源说明..."
              />
            </div>
          </label>

          <label class="portfolio-admin__select pf-field">
            <span class="pf-field__label">领域</span>
            <select v-model="domainFilter" class="pf-control">
              <option value="all">全部领域</option>
              <option v-for="item in DOMAIN_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </label>

          <label class="portfolio-admin__select pf-field">
            <span class="pf-field__label">来源</span>
            <select v-model="sourceFilter" class="pf-control">
              <option value="all">全部来源</option>
              <option v-for="item in SOURCE_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </label>

          <label class="portfolio-admin__select pf-field">
            <span class="pf-field__label">状态</span>
            <select v-model="statusFilter" class="pf-control">
              <option value="all">全部状态</option>
              <option value="published">已发布</option>
              <option value="draft">草稿</option>
              <option value="featured">核心项目</option>
            </select>
          </label>

          <VButton @click="resetFilters">重置筛选</VButton>
        </div>
      </VCard>

      <VCard v-if="filteredProjects.length === 0">
        <VEmpty
          :title="projects.length ? '没有匹配的项目' : '暂无项目'"
          :description="
            projects.length
              ? '尝试调整搜索关键词或筛选条件'
              : '创建第一个作品集项目，开始搭建你的求职展示页'
          "
        >
          <template #actions>
            <VSpace>
              <VButton v-if="projects.length" @click="resetFilters">清除筛选</VButton>
              <VButton type="primary" @click="handleCreate">新建项目</VButton>
            </VSpace>
          </template>
        </VEmpty>
      </VCard>

      <VCard v-else>
        <div class="list-toolbar">
          <span class="list-toolbar__count">共 {{ filteredProjects.length }} 条结果</span>
        </div>

        <div
          v-for="project in filteredProjects"
          :key="project.metadata.name"
          class="pf-project-row"
        >
          <div class="pf-project-row__cover">
            <img
              v-if="project.spec.coverImage"
              :src="project.spec.coverImage"
              :alt="project.spec.title"
              loading="lazy"
            />
            <div v-else class="pf-project-row__placeholder">
              {{ project.spec.title.slice(0, 1) }}
            </div>
          </div>

          <div class="pf-project-row__content">
            <div class="pf-project-row__headline">
              <h3 class="pf-project-row__title">{{ project.spec.title }}</h3>
              <VSpace>
                <VTag v-if="project.spec.featured">核心项目</VTag>
                <VStatusDot
                  :state="project.spec.published ? 'success' : 'warning'"
                  :text="project.spec.published ? '已发布' : '草稿'"
                />
              </VSpace>
            </div>
            <p class="pf-project-row__summary">
              {{ project.spec.summary || '暂无简介' }}
            </p>
            <div class="pf-project-row__meta">
              <span>领域：{{ getDomainLabel(project.spec.domain) }}</span>
              <span>来源：{{ getSourceLabel(project.spec.source) }}</span>
              <span>时间：{{ formatDate(project.spec.startDate) }}</span>
              <span v-if="project.spec.priority">优先级：{{ project.spec.priority }}</span>
            </div>
            <div v-if="project.spec.tags?.length" class="pf-project-row__tags">
              <VTag v-for="tag in project.spec.tags.slice(0, 4)" :key="tag">{{ tag }}</VTag>
            </div>
          </div>

          <div class="pf-project-row__actions">
            <VButton size="sm" @click="handleEdit(project.metadata.name!)">编辑</VButton>
            <VButton size="sm" type="danger" @click="handleDelete(project)">删除</VButton>
          </div>
        </div>
      </VCard>
    </div>
  </div>
</template>

<style scoped>
.search-input {
  position: relative;
}

.search-input__icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 1rem;
  height: 1rem;
  color: var(--pf-text-muted);
  pointer-events: none;
}

.search-input__control {
  padding-left: 2.25rem;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.list-toolbar__count {
  font-size: 0.8125rem;
  color: var(--pf-text-muted);
}

.pf-project-row__headline {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
}

.pf-project-row__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-top: 0.5rem;
}
</style>
