import { axiosInstance } from '@halo-dev/api-client'
import type { ExtensionList, PortfolioOption, PortfolioOptionType } from '@/types/portfolio'
import { PORTFOLIO_OPTIONS_API } from '@/types/portfolio'

export async function listPortfolioOptions(portfolioName: string, type?: PortfolioOptionType) {
  const fieldSelector = [`spec.portfolioName=${portfolioName}`]
  if (type) {
    fieldSelector.push(`spec.type=${type}`)
  }
  const { data } = await axiosInstance.get<ExtensionList<PortfolioOption>>(PORTFOLIO_OPTIONS_API, {
    params: {
      page: 1,
      size: 500,
      fieldSelector,
    },
  })
  data.items.sort((a, b) => (a.spec.sortOrder ?? 0) - (b.spec.sortOrder ?? 0))
  return data
}

export async function createPortfolioOption(option: PortfolioOption) {
  const { data } = await axiosInstance.post<PortfolioOption>(PORTFOLIO_OPTIONS_API, option)
  return data
}

export async function updatePortfolioOption(option: PortfolioOption) {
  const { data } = await axiosInstance.put<PortfolioOption>(
    `${PORTFOLIO_OPTIONS_API}/${option.metadata.name!}`,
    option,
  )
  return data
}

export async function deletePortfolioOption(name: string) {
  await axiosInstance.delete(`${PORTFOLIO_OPTIONS_API}/${name}`)
}

export function slugify(value: string) {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 48)
}
