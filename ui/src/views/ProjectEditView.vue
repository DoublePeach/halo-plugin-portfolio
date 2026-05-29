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
  VSwitch,
  Toast,
} from '@halo-dev/components'
import FormSection from '@/components/FormSection.vue'
import ProjectPreviewCard from '@/components/ProjectPreviewCard.vue'
import TagInput from '@/components/TagInput.vue'
import { createProject, getProject, updateProject } from '@/api/portfolio'
import { DOMAIN_OPTIONS, SOURCE_OPTIONS, TECH_STACK_OPTIONS } from '@/constants/options'
import type { PortfolioProject, PortfolioProjectSpec } from '@/types/portfolio'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const projectName = computed(() => route.params.name as string | undefined)
const isEdit = computed(() => Boolean(projectName.value))

const formState = ref<PortfolioProjectSpec>({
  title: '',
  summary: '',
  description: '',
  coverImage: '',
  gallery: [],
  tags: [],
  domain: 'java',
  techStack: [],
  source: 'company',
  sourceDetail: '',
  featured: false,
  priority: 0,
  startDate: '',
  endDate: '',
  published: false,
})

const tagsInput = ref('')
const galleryInput = ref('')
const techStackInput = ref('')

function toDateInput(value?: string) {
  if (!value) {
    return ''
  }
  return value.slice(0, 10)
}

function toInstant(value?: string) {
  if (!value) {
    return undefined
  }
  return `${value}T00:00:00Z`
}

function splitLines(value: string) {
  return value
    .split(/[\n,]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

async function fetchProject() {
  if (!projectName.value) {
    return
  }
  loading.value = true
  try {
    const project = await getProject(projectName.value)
    formState.value = {
      ...project.spec,
      startDate: toDateInput(project.spec.startDate),
      endDate: toDateInput(project.spec.endDate),
    }
    tagsInput.value = (project.spec.tags || []).join(', ')
    galleryInput.value = (project.spec.gallery || []).join('\n')
    techStackInput.value = (project.spec.techStack || []).join(', ')
  } catch (error) {
    console.error(error)
    Toast.error('加载项目失败')
  } finally {
    loading.value = false
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
    const payload: PortfolioProject = {
      apiVersion: 'portfolio.plugin.halo.run/v1alpha1',
      kind: 'PortfolioProject',
      metadata: isEdit.value
        ? { name: projectName.value! }
        : { generateName: 'portfolio-project-' },
      spec: {
        ...formState.value,
        tags: splitLines(tagsInput.value.replace(/[,，]/g, ',')),
        gallery: splitLines(galleryInput.value),
        techStack: splitLines(techStackInput.value.replace(/[,，]/g, ',')),
        startDate: toInstant(formState.value.startDate),
        endDate: toInstant(formState.value.endDate),
      },
    }

    if (isEdit.value) {
      await updateProject(payload)
      Toast.success(publish ? '已保存并发布' : '更新成功')
    } else {
      await createProject(payload)
      Toast.success(publish ? '已创建并发布' : '创建成功')
    }
    router.push({ name: 'PortfolioProjects' })
  } catch (error) {
    console.error(error)
    Toast.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetchProject)
</script>

<template>
  <VLoading v-if="loading" />
  <div v-else class="portfolio-admin">
    <VPageHeader :title="isEdit ? '编辑项目' : '新建项目'" description="填写项目内容并预览前台卡片展示效果">
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
          <FormSection title="基本信息" description="标题与简介会出现在项目卡片和时间线节点中。">
            <label class="pf-field">
              <span class="pf-field__label">项目标题 *</span>
              <input
                v-model="formState.title"
                class="pf-control"
                placeholder="例如：企业级 RAG 知识库平台"
              />
            </label>

            <label class="pf-field">
              <span class="pf-field__label">项目简介</span>
              <textarea
                v-model="formState.summary"
                class="pf-control pf-control--textarea"
                rows="3"
                placeholder="一句话概括项目价值，建议 40-80 字"
              />
            </label>

            <div class="portfolio-admin__grid-2">
              <label class="pf-field">
                <span class="pf-field__label">领域</span>
                <select v-model="formState.domain" class="pf-control">
                  <option v-for="item in DOMAIN_OPTIONS" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </option>
                </select>
              </label>
              <label class="pf-field">
                <span class="pf-field__label">来源</span>
                <select v-model="formState.source" class="pf-control">
                  <option v-for="item in SOURCE_OPTIONS" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </option>
                </select>
              </label>
            </div>

            <label class="pf-field">
              <span class="pf-field__label">来源说明</span>
              <input
                v-model="formState.sourceDetail"
                class="pf-control"
                placeholder="例如：某互联网公司 · 后端负责人"
              />
            </label>
          </FormSection>
        </VCard>

        <VCard>
          <FormSection
            title="详细内容"
            description="支持 Markdown，前台详情弹窗会渲染此字段。"
          >
            <label class="pf-field">
              <span class="pf-field__label">项目详情（Markdown）</span>
              <textarea
                v-model="formState.description"
                class="pf-control pf-control--textarea"
                rows="12"
                placeholder="## 项目背景&#10;## 你的职责&#10;## 技术亮点&#10;## 业务成果"
              />
            </label>
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="媒体资源" description="封面图用于卡片展示，图集用于详情弹窗轮播。">
            <label class="pf-field">
              <span class="pf-field__label">封面图 URL</span>
              <input
                v-model="formState.coverImage"
                class="pf-control"
                placeholder="https://example.com/cover.jpg"
              />
              <p class="pf-field__hint">建议 16:10 比例，前台卡片会自动裁剪。</p>
            </label>
            <div v-if="formState.coverImage" class="pf-cover-preview">
              <img :src="formState.coverImage" alt="封面预览" loading="lazy" />
            </div>

            <label class="pf-field">
              <span class="pf-field__label">详情图集</span>
              <textarea
                v-model="galleryInput"
                class="pf-control pf-control--textarea"
                rows="4"
                placeholder="每行一个图片 URL"
              />
            </label>
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="分类与标签" description="标签会显示在卡片上，技术栈用于前台按栈分组。">
            <TagInput
              v-model="tagsInput"
              label="项目标签"
              placeholder="输入后按 Enter 添加"
              hint="例如：微服务、高并发、LLM"
            />

            <TagInput
              v-model="techStackInput"
              label="技术栈"
              placeholder="输入后按 Enter 添加"
              :suggestions="TECH_STACK_OPTIONS"
              hint="点击推荐标签可快速添加"
            />
          </FormSection>
        </VCard>
      </div>

      <aside class="portfolio-admin__sidebar">
        <VCard>
          <FormSection title="发布设置" description="控制项目在前台的可见性与展示权重。">
            <div class="pf-switch-row">
              <div class="pf-switch-row__info">
                <p class="pf-switch-row__title">核心展示项目</p>
                <p class="pf-switch-row__desc">出现在首页核心项目区，建议 4-6 个</p>
              </div>
              <VSwitch v-model="formState.featured" />
            </div>
            <div class="pf-switch-row">
              <div class="pf-switch-row__info">
                <p class="pf-switch-row__title">公开发布</p>
                <p class="pf-switch-row__desc">关闭后仅后台可见，前台不展示</p>
              </div>
              <VSwitch v-model="formState.published" />
            </div>
            <label class="pf-field">
              <span class="pf-field__label">展示优先级</span>
              <input
                v-model.number="formState.priority"
                class="pf-control"
                type="number"
                min="0"
                max="999"
              />
              <p class="pf-field__hint">数值越大排序越靠前，核心项目按此字段排序。</p>
            </label>
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="时间线" description="用于前台按年月分组展示。">
            <div class="portfolio-admin__grid-2">
              <label class="pf-field">
                <span class="pf-field__label">开始日期</span>
                <input v-model="formState.startDate" class="pf-control" type="date" />
              </label>
              <label class="pf-field">
                <span class="pf-field__label">结束日期</span>
                <input v-model="formState.endDate" class="pf-control" type="date" />
              </label>
            </div>
          </FormSection>
        </VCard>

        <VCard>
          <FormSection title="前台预览" description="模拟公开页项目卡片效果。">
            <ProjectPreviewCard :spec="formState" :tags-text="tagsInput" />
          </FormSection>
        </VCard>

        <VAlert
          type="info"
          title="编辑提示"
          description="保存后可在 /portfolio 公开页查看最终呈现。Markdown 详情建议包含背景、职责、亮点与成果。"
        />
      </aside>
    </div>
  </div>
</template>
