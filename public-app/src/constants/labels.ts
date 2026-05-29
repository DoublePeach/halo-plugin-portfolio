export const DOMAIN_LABELS: Record<string, string> = {
  java: 'Java 传统项目',
  middleware: '中间件轮子',
  'ai-vibe': 'VibeCoding',
  'ai-rag': 'RAG',
  'ai-agent': 'Agent',
  'ai-enterprise': '企业 AI',
}

export const SOURCE_LABELS: Record<string, string> = {
  university: '大学在校期间',
  intern: '大学实习期间',
  company: '企业项目',
  oss: '开源项目',
}

export function getDomainLabel(value?: string) {
  if (!value) return ''
  return DOMAIN_LABELS[value] || value
}

export function getSourceLabel(value?: string) {
  if (!value) return ''
  return SOURCE_LABELS[value] || value
}
