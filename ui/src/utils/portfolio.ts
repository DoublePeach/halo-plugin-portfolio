import type { Metadata } from '@/types/portfolio'

export function isValidPortfolioName(name?: string) {
  return Boolean(name && !name.startsWith(':') && name !== 'create')
}

export function buildFieldSelector(...conditions: string[]) {
  return conditions.join(',')
}

export function withUpdateMetadata(metadata: Metadata, name: string): Metadata {
  return {
    name,
    version: metadata.version,
    creationTimestamp: metadata.creationTimestamp,
  }
}

export function isSlugTaken(slug: string, portfolios: { metadata: { name?: string }; spec: { slug: string } }[], excludeName?: string) {
  return portfolios.some(
    (portfolio) => portfolio.spec.slug === slug && portfolio.metadata.name !== excludeName,
  )
}
