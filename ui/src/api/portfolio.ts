import { axiosInstance } from '@halo-dev/api-client'
import type { ExtensionList, Portfolio } from '@/types/portfolio'
import { PORTFOLIOS_API } from '@/types/portfolio'

export async function listPortfolios() {
  const { data } = await axiosInstance.get<ExtensionList<Portfolio>>(PORTFOLIOS_API, {
    params: {
      page: 1,
      size: 200,
      sort: 'spec.priority,desc',
    },
  })
  return data
}

export async function getPortfolio(name: string) {
  const { data } = await axiosInstance.get<Portfolio>(`${PORTFOLIOS_API}/${name}`)
  return data
}

export async function createPortfolio(portfolio: Portfolio) {
  const { data } = await axiosInstance.post<Portfolio>(PORTFOLIOS_API, portfolio)
  return data
}

export async function updatePortfolio(portfolio: Portfolio) {
  const { data } = await axiosInstance.put<Portfolio>(
    `${PORTFOLIOS_API}/${portfolio.metadata.name!}`,
    portfolio,
  )
  return data
}

export async function deletePortfolio(name: string) {
  await axiosInstance.delete(`${PORTFOLIOS_API}/${name}`)
}
