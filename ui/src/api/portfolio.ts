import { axiosInstance } from '@halo-dev/api-client'
import type { PortfolioProject, PortfolioProjectList } from '@/types/portfolio'
import { API_BASE } from '@/constants/options'

export async function listProjects() {
  const { data } = await axiosInstance.get<PortfolioProjectList>(API_BASE, {
    params: {
      page: 1,
      size: 200,
      sort: 'spec.startDate,desc',
    },
  })
  return data
}

export async function getProject(name: string) {
  const { data } = await axiosInstance.get<PortfolioProject>(`${API_BASE}/${name}`)
  return data
}

export async function createProject(project: PortfolioProject) {
  const { data } = await axiosInstance.post<PortfolioProject>(API_BASE, project)
  return data
}

export async function updateProject(project: PortfolioProject) {
  const { data } = await axiosInstance.put<PortfolioProject>(
    `${API_BASE}/${project.metadata.name!}`,
    project,
  )
  return data
}

export async function deleteProject(name: string) {
  await axiosInstance.delete(`${API_BASE}/${name}`)
}
