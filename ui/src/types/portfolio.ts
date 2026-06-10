export interface Metadata {
  name?: string
  generateName?: string
  creationTimestamp?: string
  deletionTimestamp?: string
  version?: number
}

export interface ExtensionList<T> {
  page: number
  size: number
  total: number
  items: T[]
  first: boolean
  last: boolean
  hasNext: boolean
  hasPrevious: boolean
  totalPages: number
}

export interface PortfolioSpec {
  displayName: string
  slug: string
  description?: string
  cover?: string
  publicView?: boolean
  priority?: number
}

export interface PortfolioStatus {
  projectCount?: number
}

export interface Portfolio {
  apiVersion: 'portfolio.plugin.halo.run/v1alpha1'
  kind: 'Portfolio'
  metadata: Metadata
  spec: PortfolioSpec
  status?: PortfolioStatus
}

export type PortfolioOptionType = 'TECH_STACK' | 'SOURCE' | 'DOMAIN'

export interface PortfolioOptionSpec {
  portfolioName: string
  type: PortfolioOptionType
  value: string
  label: string
  sortOrder?: number
}

export interface PortfolioOption {
  apiVersion: 'portfolio.plugin.halo.run/v1alpha1'
  kind: 'PortfolioOption'
  metadata: Metadata
  spec: PortfolioOptionSpec
}

export interface PortfolioProjectSpec {
  portfolioName: string
  title: string
  summary?: string
  description?: string
  postName?: string
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

export const API_GROUP = '/apis/portfolio.plugin.halo.run/v1alpha1'
export const PORTFOLIOS_API = `${API_GROUP}/portfolios`
export const PORTFOLIO_OPTIONS_API = `${API_GROUP}/portfoliooptions`
export const PORTFOLIO_PROJECTS_API = `${API_GROUP}/portfolioprojects`
