import type { AxiosError } from 'axios'
import type { Metadata } from '@/types/portfolio'
import { withUpdateMetadata } from '@/utils/portfolio'

export interface ExtensionResource {
  metadata: Metadata
}

export function isActiveExtension<T extends ExtensionResource>(item: T) {
  return !item.metadata.deletionTimestamp
}

export function filterActiveExtensions<T extends ExtensionResource>(items: T[]) {
  return items.filter(isActiveExtension)
}

export function isConflictError(error: unknown) {
  return (error as AxiosError)?.response?.status === 409
}

export function getApiErrorMessage(error: unknown, fallback = '操作失败') {
  const axiosError = error as AxiosError<{ detail?: string; title?: string }>
  const status = axiosError.response?.status
  const detail = axiosError.response?.data?.detail

  if (detail) {
    return detail
  }
  if (status === 409) {
    return '数据已被他人修改，请刷新后重试'
  }
  if (status === 403) {
    return '没有权限执行此操作'
  }
  if (status === 404) {
    return '资源不存在或已被删除'
  }
  if (status === 500) {
    return '服务器内部错误，请稍后重试'
  }
  return fallback
}

export async function updateWithConflictRetry<T extends ExtensionResource>(
  payload: T,
  updateFn: (item: T) => Promise<T>,
  refetchFn: () => Promise<T>,
): Promise<T> {
  try {
    return await updateFn(payload)
  } catch (error) {
    if (!isConflictError(error)) {
      throw error
    }
    const latest = await refetchFn()
    const name = latest.metadata.name ?? payload.metadata.name
    if (!name) {
      throw error
    }
    return updateFn({
      ...payload,
      metadata: withUpdateMetadata(latest.metadata, name),
    })
  }
}

export function debounceByKey(
  key: string,
  fn: () => void,
  delayMs: number,
  timers: Map<string, ReturnType<typeof setTimeout>>,
) {
  const existing = timers.get(key)
  if (existing) {
    clearTimeout(existing)
  }
  timers.set(
    key,
    setTimeout(() => {
      timers.delete(key)
      fn()
    }, delayMs),
  )
}
