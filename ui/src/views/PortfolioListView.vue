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
import { deletePortfolio, listPortfolios } from '@/api/portfolio'
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

async function fetchPortfolios() {
  loading.value = true
  try {
    const result = await listPortfolios()
    portfolios.value = result.items
  } catch (error) {
    console.error(error)
    Toast.error('加载作品集列表失败')
  } finally {
    loading.value = false
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

async function handleDelete(portfolio: Portfolio) {
  Dialog.warning({
    title: '删除作品集',
    description: `确定删除「${portfolio.spec.displayName}」吗？其下项目与选项字典将失去归属，请先确认已迁移数据。`,
    onConfirm: async () => {
      try {
        await deletePortfolio(portfolio.metadata.name!)
        Toast.success('删除成功')
        await fetchPortfolios()
      } catch (error) {
        console.error(error)
        Toast.error('删除失败')
      }
    },
  })
}

onMounted(fetchPortfolios)
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
              placeholder="名称、路由编码..."
            />
          </div>
        </label>
      </div>
    </VCard>

    <VCard v-if="filteredPortfolios.length === 0">
      <VEmpty
        :title="portfolios.length ? '没有匹配的作品集' : '暂无作品集'"
        :description="portfolios.length ? '尝试调整搜索关键词' : '创建第一个作品集，开始管理你的项目经历'"
      >
        <template #actions>
          <VButton type="primary" @click="handleCreate">新建作品集</VButton>
        </template>
      </VEmpty>
    </VCard>

    <VCard v-else>
      <div
        v-for="portfolio in filteredPortfolios"
        :key="portfolio.metadata.name"
        class="pf-portfolio-row"
      >
        <div class="pf-portfolio-row__main">
          <div class="pf-portfolio-row__headline">
            <h3 class="pf-portfolio-row__title">{{ portfolio.spec.displayName }}</h3>
            <VSpace>
              <VTag>/{{ portfolio.spec.slug }}</VTag>
              <VStatusDot
                :state="portfolio.spec.publicView ? 'success' : 'warning'"
                :text="portfolio.spec.publicView ? '公开' : '私有'"
              />
            </VSpace>
          </div>
          <p class="pf-portfolio-row__desc">
            {{ portfolio.spec.description || '暂无描述' }}
          </p>
          <div class="pf-portfolio-row__meta">
            <span>项目数：{{ portfolio.status?.projectCount ?? 0 }}</span>
            <span v-if="portfolio.spec.priority">优先级：{{ portfolio.spec.priority }}</span>
          </div>
        </div>
        <div class="pf-portfolio-row__actions">
          <VButton size="sm" type="primary" @click="handleOpen(portfolio.metadata.name!)">
            进入管理
          </VButton>
          <VButton size="sm" @click="handleEdit(portfolio.metadata.name!)">编辑</VButton>
          <VButton size="sm" type="danger" @click="handleDelete(portfolio)">删除</VButton>
        </div>
      </div>
    </VCard>
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

.pf-portfolio-row {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1rem 0;
  border-bottom: 1px solid var(--pf-border);
}

.pf-portfolio-row:last-child {
  border-bottom: none;
}

.pf-portfolio-row__headline {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
}

.pf-portfolio-row__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.pf-portfolio-row__desc {
  margin: 0.375rem 0 0;
  color: var(--pf-text-muted);
  font-size: 0.875rem;
}

.pf-portfolio-row__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 0.5rem;
  font-size: 0.8125rem;
  color: var(--pf-text-muted);
}

.pf-portfolio-row__actions {
  display: flex;
  flex-shrink: 0;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style>
