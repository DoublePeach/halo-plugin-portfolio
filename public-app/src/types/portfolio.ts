export interface PortfolioProject {
  name: string
  title: string
  summary?: string
  description?: string
  coverImage?: string
  gallery?: string[]
  tags?: string[]
  domain?: string
  techStack?: string[]
  source?: string
  sourceDetail?: string
  featured?: boolean
  priority?: number
  startDate?: string
  endDate?: string
}

export interface TimelineGroup {
  key: string
  label: string
  projects: PortfolioProject[]
}

export interface TimelineResponse {
  granularity: string
  order: string
  groups: TimelineGroup[]
}

export interface GroupSection {
  key: string
  label: string
  projects: PortfolioProject[]
}

export interface GroupedProjectsResponse {
  groupBy: string
  sections: GroupSection[]
}

export type ViewMode = 'timeline' | 'featured' | 'domain' | 'techStack' | 'source'
