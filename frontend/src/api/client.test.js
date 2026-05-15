import { describe, it, expect, vi } from 'vitest'

vi.mock('axios', () => {
  const instance = {
    get: vi.fn(),
    post: vi.fn(),
    interceptors: { response: { use: vi.fn() } },
  }
  return { default: { create: vi.fn(() => instance), ...instance } }
})

describe('API client unwrap', () => {
  it('unwraps data from ApiResponse envelope', async () => {
    const { unwrap } = await import('./client.js')
    const response = { data: { data: { id: '123' }, error: null, meta: {} } }
    expect(unwrap(response)).toEqual({ id: '123' })
  })

  it('throws when ApiResponse contains an error', async () => {
    const { unwrap } = await import('./client.js')
    const response = { data: { data: null, error: { code: 'NOT_FOUND', message: 'Session not found' }, meta: {} } }
    expect(() => unwrap(response)).toThrow('Session not found')
  })
})
