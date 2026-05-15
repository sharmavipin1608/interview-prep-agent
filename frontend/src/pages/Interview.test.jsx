import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import * as client from '../api/client.js'
import Interview from './Interview'

vi.mock('../api/client.js')
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => ({
  ...(await importOriginal()),
  useNavigate: () => mockNavigate,
}))

const brief = {
  companyName: 'Stripe', companySummary: 'Payments company',
  techStack: ['Ruby'], cultureSignals: '', recentNews: '', likelyInterviewTopics: [],
}

const wrapper = ({ children }) => (
  <QueryClientProvider client={new QueryClient()}>
    <MemoryRouter initialEntries={['/sessions/sess-1']}>
      <Routes><Route path="/sessions/:id" element={children} /></Routes>
    </MemoryRouter>
  </QueryClientProvider>
)

beforeEach(() => {
  localStorage.setItem('brief:sess-1', JSON.stringify(brief))
})

describe('Interview', () => {
  it('renders company name from localStorage brief', () => {
    render(<Interview />, { wrapper })
    expect(screen.getAllByText('Stripe').length).toBeGreaterThanOrEqual(1)
  })

  it('displays AI reply after sending a message', async () => {
    vi.mocked(client.chat).mockResolvedValue({ reply: 'Tell me more.' })
    render(<Interview />, { wrapper })

    await userEvent.type(screen.getByPlaceholderText(/type your answer/i), 'Hello')
    fireEvent.click(screen.getByRole('button', { name: /send/i }))

    expect(await screen.findByText('Tell me more.')).toBeInTheDocument()
  })

  it('navigates to results when Evaluate is clicked', async () => {
    render(<Interview />, { wrapper })
    fireEvent.click(screen.getByRole('button', { name: /evaluate/i }))
    expect(mockNavigate).toHaveBeenCalledWith('/sessions/sess-1/results')
  })
})
