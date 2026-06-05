export function isValidPortfolioName(name?: string) {
  return Boolean(name && !name.startsWith(':') && name !== 'create')
}
