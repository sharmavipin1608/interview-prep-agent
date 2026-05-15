import { describe, it, expect, vi } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import * as client from '../api/client.js'

vi.mock('../api/client.js')

const wrapper = ({ children }) => (
  React.createElement(
    QueryClientProvider,
    { client: new QueryClient({ defaultOptions: { queries: { retry: false } } }) },
    children
  )
)

describe('useSessions', () => {
  it('returns sessions from API', async () => {
    const { useSessions } = await import('./useSessions.js')
    const sessions = [{ id: '1', companyName: 'Stripe', status: 'COMPLETED' }]
    vi.mocked(client.getSessions).mockResolvedValue(sessions)

    const { result } = renderHook(() => useSessions(), { wrapper })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(sessions)
  })
})

describe('useWeakAreas', () => {
  it('returns weak areas from API', async () => {
    const { useWeakAreas } = await import('./useWeakAreas.js')
    vi.mocked(client.getWeakAreas).mockResolvedValue(['System Design', 'Concurrency'])

    const { result } = renderHook(() => useWeakAreas(), { wrapper })
    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toEqual(['System Design', 'Concurrency'])
  })
})
