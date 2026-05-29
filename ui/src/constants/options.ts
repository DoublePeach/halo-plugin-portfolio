export const DOMAIN_OPTIONS = [
  { label: 'Java 传统项目', value: 'java' },
  { label: '中间件轮子', value: 'middleware' },
  { label: 'VibeCoding 项目', value: 'ai-vibe' },
  { label: 'RAG 项目', value: 'ai-rag' },
  { label: 'Agent 项目', value: 'ai-agent' },
  { label: '企业 AI 能力接入', value: 'ai-enterprise' },
]

export const SOURCE_OPTIONS = [
  { label: '大学在校期间', value: 'university' },
  { label: '大学实习期间', value: 'intern' },
  { label: '企业项目', value: 'company' },
  { label: '开源项目参与', value: 'oss' },
]

export const TECH_STACK_OPTIONS = [
  'Java',
  'Spring Cloud',
  'MySQL',
  'Redis',
  'Python',
  'FastAPI',
  'LangChain',
  'LangGraph',
  'LlamaIndex',
  'Vue',
  'TypeScript',
  'Docker',
  'Kubernetes',
]

export const API_BASE = '/apis/portfolio.plugin.halo.run/v1alpha1/portfolioprojects'

export function getDomainLabel(value?: string) {
  return DOMAIN_OPTIONS.find((item) => item.value === value)?.label ?? value ?? '-'
}

export function getSourceLabel(value?: string) {
  return SOURCE_OPTIONS.find((item) => item.value === value)?.label ?? value ?? '-'
}
