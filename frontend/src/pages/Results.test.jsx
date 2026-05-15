import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import * as client from '../api/client.js'
import Results from './Results'

vi.mock('../api/client.js')

const wrapper = ({ children }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
    <MemoryRouter initialEntries={['/sessions/sess-1/results']}>
      <Routes><Route path="/sessions/:id/results" element={children} /></Routes>
    </MemoryRouter>
  </QueryClientProvider>
)

const feedback = {
  sessionId: 'sess-1',
  overallScore: 82,
  weakAreas: ['Concurrency', 'System Design'],
  improvementSuggestions: ['Practice lock-free data structures'],
  detailedFeedback: 'Strong communication. Needs deeper technical depth.',
}

describe('Results', () => {
  it('triggers evaluate and shows overall score', async () => {
    vi.mocked(client.evaluate).mockResolvedValue(feedback)
    render(<Results />, { wrapper })
    fireEvent.click(screen.getByRole('button', { name: /get feedback/i }))
    expect(await screen.findByText('82')).toBeInTheDocument()
  })

  it('displays weak areas after evaluation', async () => {
    vi.mocked(client.evaluate).mockResolvedValue(feedback)
    render(<Results />, { wrapper })
    fireEvent.click(screen.getByRole('button', { name: /get feedback/i }))
    expect(await screen.findByText('Concurrency')).toBeInTheDocument()
  })

  it('displays detailed feedback text', async () => {
    vi.mocked(client.evaluate).mockResolvedValue(feedback)
    render(<Results />, { wrapper })
    fireEvent.click(screen.getByRole('button', { name: /get feedback/i }))
    expect(await screen.findByText(/Strong communication/)).toBeInTheDocument()
  })
})
