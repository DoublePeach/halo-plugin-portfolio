export interface Metadata {
  name?: string
  generateName?: string
  creationTimestamp?: string
  version?: number
}

export interface PortfolioProjectSpec {
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
  published?: boolean
}

export interface PortfolioProject {
  apiVersion: 'portfolio.plugin.halo.run/v1alpha1'
  kind: 'PortfolioProject'
  metadata: Metadata
  spec: PortfolioProjectSpec
}

export interface PortfolioProjectList {
  page: number
  size: number
  total: number
  items: PortfolioProject[]
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
  totalPages: number
}
