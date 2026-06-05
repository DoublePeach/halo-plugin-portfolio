import { axiosInstance } from '@halo-dev/api-client'
import type { ExtensionList, PortfolioProject } from '@/types/portfolio'
import { PORTFOLIO_PROJECTS_API } from '@/types/portfolio'

export async function listProjects(portfolioName: string) {
  const { data } = await axiosInstance.get<ExtensionList<PortfolioProject>>(
    PORTFOLIO_PROJECTS_API,
    {
      params: {
        page: 1,
        size: 500,
        sort: 'spec.startDate,desc',
        fieldSelector: [`spec.portfolioName=${portfolioName}`],
      },
    },
  )
  return data
}

export async function getProject(name: string) {
  const { data } = await axiosInstance.get<PortfolioProject>(`${PORTFOLIO_PROJECTS_API}/${name}`)
  return data
}

export async function createProject(project: PortfolioProject) {
  const { data } = await axiosInstance.post<PortfolioProject>(PORTFOLIO_PROJECTS_API, project)
  return data
}

export async function updateProject(project: PortfolioProject) {
  const { data } = await axiosInstance.put<PortfolioProject>(
    `${PORTFOLIO_PROJECTS_API}/${project.metadata.name!}`,
    project,
  )
  return data
}

export async function deleteProject(name: string) {
  await axiosInstance.delete(`${PORTFOLIO_PROJECTS_API}/${name}`)
}
