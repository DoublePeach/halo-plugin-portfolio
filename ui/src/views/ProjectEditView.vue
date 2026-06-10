<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  VAlert,
  VButton,
  VCard,
  VLoading,
  VPageHeader,
  VSpace,
  Toast,
} from '@halo-dev/components'
import FormSection from '@/components/FormSection.vue'
import ProjectPreviewCard from '@/components/ProjectPreviewCard.vue'
import { listPortfolioOptions } from '@/api/portfolio-option'
import { createProject, getProject, updateProject } from '@/api/portfolio-project'
import { toDateInput, toInstant } from '@/utils/date'
import { isValidPortfolioName, withUpdateMetadata } from '@/utils/portfolio'
import type { PortfolioOption, PortfolioProject, PortfolioProjectSpec } from '@/types/portfolio'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const options = ref<PortfolioOption[]>([])
const originalProject = ref<PortfolioProject | null>(null)

const portfolioName = computed(() => route.params.name as string)
const projectName = computed(() => route.params.projectName as string | undefined)
const isEdit = computed(() => Boolean(projectName.value))

const formState = ref<PortfolioProjectSpec>({
  portfolioName: portfolioName.value,
  title: '',
  summary: '',
  postName: '',
  coverImage: '',
  gallery: [],
  domain: '',
  techStack: [],
  source: '',
  featured: false,
  priority: 0,
  startDate: '',
  endDate: '',
  published: false,
})

const domainOptions = computed(() =>
  options.value
    .filter((o) => o.spec.type === 'DOMAIN')
    .map((o) => ({ label: o.spec.label, value: o.spec.value })),
)
const sourceOptions = computed(() =>
  options.value
    .filter((o) => o.spec.type === 'SOURCE')
    .map((o) => ({ label: o.spec.label, value: o.spec.value })),
)
const techStackOptions = computed(() =>
  options.value
    .filter((o) => o.spec.type === 'TECH_STACK')
    .map((o) => ({ label: o.spec.label, value: o.spec.value })),
)

async function fetchOptions() {
  const result = await listPortfolioOptions(portfolioName.value)
  options.value = result.items
}

async function fetchProject() {
  if (!projectName.value) return
  const project = await getProject(projectName.value)
  originalProject.value = project
  formState.value = {
    ...project.spec,
    portfolioName: portfolioName.value,
    startDate: toDateInput(project.spec.startDate),
    endDate: toDateInput(project.spec.endDate),
    gallery: project.spec.gallery ?? [],
    techStack: project.spec.techStack ?? [],
  }
}

async function handleSubmit(publish = false) {
  if (!formState.value.title?.trim()) {
    Toast.warning('请填写项目标题')
    return
  }
  if (publish) {
    formState.value.published = true
  }

  saving.value = true
  try {
    const spec = {
      ...formState.value,
      portfolioName: portfolioName.value,
      startDate: toInstant(formState.value.startDate),
      endDate: toInstant(formState.value.endDate),
      gallery: formState.value.gallery?.filter(Boolean) ?? [],
      techStack: formState.value.techStack ?? [],
      postName: formState.value.postName?.trim() || undefined,
      domain: formState.value.domain?.trim() || undefined,
      source: formState.value.source?.trim() || undefined,
    }
    if (isEdit.value && originalProject.value) {
      spec.description = originalProject.value.spec.description
      spec.tags = originalProject.value.spec.tags
      spec.sourceDetail = originalProject.value.spec.sourceDetail
    }

    const payload: PortfolioProject = {
      apiVersion: 'portfolio.plugin.halo.run/v1alpha1',
      kind: 'PortfolioProject',
      metadata: isEdit.value
        ? withUpdateMetadata(originalProject.value!.metadata, projectName.value!)
        : { generateName: 'portfolio-project-' },
      spec,
    }

    if (isEdit.value) {
      await updateProject(payload)
      Toast.success(publish ? '已保存并发布' : '更新成功')
    } else {
      await createProject(payload)
      Toast.success(publish ? '已创建并发布' : '创建成功')
    }
    router.push({ name: 'PortfolioDetail', params: { name: portfolioName.value } })
  } catch (error) {
    console.error(error)
    Toast.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!isValidPortfolioName(portfolioName.value)) {
    Toast.error('无效的作品集地址')
    router.replace({ name: 'Portfolios' })
    return
  }
  loading.value = true
  try {
    await fetchOptions()
    if (isEdit.value) {
      await fetchProject()
    }
  } catch (error) {
    console.error(error)
    Toast.error('加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="portfolio-admin">
    <VPageHeader :title="isEdit ? '编辑项目' : '新建项目'" description="填写项目信息，封面与图集使用 Halo 附件库">
      <template #actions>
        <VSpace>
          <VButton @click="router.back()">返回</VButton>
          <VButton :loading="saving" @click="handleSubmit(false)">保存草稿</VButton>
          <VButton type="primary" :loading="saving" @click="handleSubmit(true)">保存并发布</VButton>
        </VSpace>
      </template>
    </VPageHeader>

    <div class="portfolio-admin__layout">
      <div class="portfolio-admin__stack">
        <VCard>
          <FormSection title="基本信息" description="标题与简介将展示在项目卡片上">
            <FormKit v-model="formState.title" type="text" label="项目标题" validation="required" />
            <FormKit
              v-model="formState.summary"
              type="textarea"
              label="项目简介"
              rows="3"
              help="建议 40-80 字概括项目价值"
            />
            <FormKit
              v-model="formState.postName"
              type="postSelect"
              label="项目详情"
              help="选择 Halo 文章作为项目详情内容"
              searchable
              clearable
            />
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="媒体资源" description="封面用于卡片展示，图集最多 20 张">
            <FormKit
              v-model="formState.coverImage"
              type="attachment"
              label="封面图"
              :accepts="['image/*']"
              width="100%"
              aspect-ratio="16/9"
            />
            <FormKit
              v-model="formState.gallery"
              type="attachment"
              label="详情图集"
              :accepts="['image/*']"
              multiple
              validation="max:20"
              help="最多上传 20 张图片"
            />
          </FormSection>
        </VCard>

        <VCard>
          <FormSection
            title="分类信息"
            description="选项来自当前作品集的「选项字典」，请先在字典中维护"
          >
            <FormKit
              v-if="domainOptions.length"
              v-model="formState.domain"
              type="select"
              label="项目领域"
              :options="domainOptions"
              clearable
            />
            <VAlert
              v-else
              type="info"
              title="暂无领域选项"
              description="请先在作品集的「选项字典」中添加领域选项"
            />

            <FormKit
              v-if="sourceOptions.length"
              v-model="formState.source"
              type="select"
              label="项目来源"
              :options="sourceOptions"
              clearable
            />
            <VAlert
              v-else
              type="info"
              title="暂无来源选项"
              description="请先在作品集的「选项字典」中添加来源选项"
            />

            <FormKit
              v-if="techStackOptions.length"
              v-model="formState.techStack"
              type="select"
              label="技术栈"
              :options="techStackOptions"
              multiple
              clearable
            />
            <VAlert
              v-else
              type="info"
              title="暂无技术栈选项"
              description="请先在作品集的「选项字典」中添加技术栈选项"
            />
          </FormSection>
        </VCard>
      </div>

      <aside class="portfolio-admin__sidebar">
        <VCard>
          <FormSection title="发布设置">
            <FormKit v-model="formState.featured" type="checkbox" label="设为核心展示项目" />
            <FormKit v-model="formState.published" type="checkbox" label="设为公开" />
            <FormKit
              v-model="formState.priority"
              type="number"
              label="展示优先级"
              help="数值越大排序越靠前"
              min="0"
            />
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="时间线">
            <FormKit v-model="formState.startDate" type="date" label="开始日期" />
            <FormKit v-model="formState.endDate" type="date" label="结束日期" />
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="卡片预览">
            <ProjectPreviewCard :spec="formState" :option-label-map="options" />
          </FormSection>
        </VCard>
      </aside>
    </div>
  </div>
</template>
