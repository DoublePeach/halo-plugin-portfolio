export function toDateInput(value?: string) {
  if (!value) {
    return ''
  }
  return value.slice(0, 10)
}

export function toInstant(value?: string) {
  if (!value) {
    return undefined
  }
  return `${value}T00:00:00Z`
}

export function formatDate(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleDateString('zh-CN')
}
