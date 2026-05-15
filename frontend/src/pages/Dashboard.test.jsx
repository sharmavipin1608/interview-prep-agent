import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import * as client from '../api/client.js'
import Dashboard from './Dashboard'

vi.mock('../api/client.js')

const wrapper = ({ children }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
)

const sessions = [
  { id: '1', companyName: 'Stripe',  jobTitle: 'Senior SWE', status: 'COMPLETED', overallScore: 82, createdAt: '2026-05-14T10:00:00Z' },
  { id: '2', companyName: 'Google',  jobTitle: 'Staff SWE',  status: 'ACTIVE',    overallScore: null, createdAt: '2026-05-15T09:00:00Z' },
]

describe('Dashboard', () => {
  it('renders company names from sessions', async () => {
    vi.mocked(client.getSessions).mockResolvedValue(sessions)
    vi.mocked(client.getWeakAreas).mockResolvedValue([])
    render(<Dashboard />, { wrapper })
    expect(await screen.findByText('Stripe')).toBeInTheDocument()
    expect(await screen.findByText('Google')).toBeInTheDocument()
  })

  it('shows total session count in stat card', async () => {
    vi.mocked(client.getSessions).mockResolvedValue(sessions)
    vi.mocked(client.getWeakAreas).mockResolvedValue([])
    render(<Dashboard />, { wrapper })
    const matches = await screen.findAllByText('2')
    expect(matches.length).toBeGreaterThanOrEqual(1)
  })

  it('renders weak area chips when returned from API', async () => {
    vi.mocked(client.getSessions).mockResolvedValue([])
    vi.mocked(client.getWeakAreas).mockResolvedValue(['System Design', 'Concurrency'])
    render(<Dashboard />, { wrapper })
    const matches = await screen.findAllByText('System Design')
    expect(matches.length).toBeGreaterThanOrEqual(1)
  })
})
