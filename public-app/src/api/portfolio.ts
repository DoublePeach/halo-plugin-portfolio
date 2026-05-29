import axios from 'axios'
import type {
  GroupedProjectsResponse,
  PortfolioProject,
  TimelineResponse,
} from '@/types/portfolio'

const http = axios.create({
  baseURL: '/',
  timeout: 15000,
})

const API_BASE = '/apis/api.portfolio.plugin.halo.run/v1alpha1'

export async function fetchFeaturedProjects() {
  const { data } = await http.get<PortfolioProject[]>(`${API_BASE}/projects/featured`)
  return data
}

export async function fetchTimeline(granularity: 'year' | 'month', order: 'asc' | 'desc') {
  const { data } = await http.get<TimelineResponse>(`${API_BASE}/projects/timeline`, {
    params: { granularity, order },
  })
  return data
}

export async function fetchGrouped(groupBy: 'domain' | 'techStack' | 'source') {
  const { data } = await http.get<GroupedProjectsResponse>(`${API_BASE}/projects/grouped`, {
    params: { groupBy },
  })
  return data
}

export async function fetchProjectDetail(name: string) {
  const { data } = await http.get<PortfolioProject>(`${API_BASE}/projects/${name}`)
  return data
}
