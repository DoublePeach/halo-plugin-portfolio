import type { PortfolioOption, PortfolioOptionType, PortfolioProject } from '@/types/portfolio'

export function countOptionReferences(projects: PortfolioProject[], option: PortfolioOption) {
  const { type, value } = option.spec
  return projects.filter((project) => referencesOption(project, type, value)).length
}

export function referencesOption(
  project: PortfolioProject,
  type: PortfolioOptionType,
  value: string,
) {
  const spec = project.spec
  if (type === 'DOMAIN') {
    return spec.domain === value
  }
  if (type === 'SOURCE') {
    return spec.source === value
  }
  return spec.techStack?.includes(value) ?? false
}

export function formatOptionReferenceHint(count: number) {
  if (count <= 0) {
    return ''
  }
  return `仍有 ${count} 个项目引用该选项，请先修改项目后再删除。`
}
