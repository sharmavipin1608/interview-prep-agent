import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import * as client from '../api/client.js'
import NewInterview from './NewInterview'

vi.mock('../api/client.js')
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => ({
  ...(await importOriginal()),
  useNavigate: () => mockNavigate,
}))

const wrapper = ({ children }) => (
  <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
)

describe('NewInterview', () => {
  it('renders the research form fields', () => {
    render(<NewInterview />, { wrapper })
    expect(screen.getByPlaceholderText(/company/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/job title/i)).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/job description/i)).toBeInTheDocument()
  })

  it('shows company brief after successful research', async () => {
    const brief = {
      companyName: 'Stripe', companySummary: 'Payments company',
      techStack: ['Ruby', 'Go'], cultureSignals: 'High ownership',
      recentNews: 'IPO rumours', likelyInterviewTopics: ['System Design'],
    }
    vi.mocked(client.research).mockResolvedValue({ sessionId: 'res-1', companyBrief: brief })
    render(<NewInterview />, { wrapper })

    await userEvent.type(screen.getByPlaceholderText(/company/i), 'Stripe')
    await userEvent.type(screen.getByPlaceholderText(/job title/i), 'Senior SWE')
    await userEvent.type(screen.getByPlaceholderText(/job description/i), 'Build payments infrastructure')
    fireEvent.click(screen.getByRole('button', { name: /research/i }))

    expect(await screen.findByText('Payments company')).toBeInTheDocument()
  })

  it('navigates to interview after starting session', async () => {
    const brief = {
      companyName: 'Stripe', companySummary: 'Payments',
      techStack: [], cultureSignals: '', recentNews: '', likelyInterviewTopics: [],
    }
    vi.mocked(client.research).mockResolvedValue({ sessionId: 'res-1', companyBrief: brief })
    vi.mocked(client.startSession).mockResolvedValue('sess-42')
    render(<NewInterview />, { wrapper })

    await userEvent.type(screen.getByPlaceholderText(/company/i), 'Stripe')
    await userEvent.type(screen.getByPlaceholderText(/job title/i), 'Senior SWE')
    await userEvent.type(screen.getByPlaceholderText(/job description/i), 'Build payments infrastructure')
    fireEvent.click(screen.getByRole('button', { name: /research/i }))
    fireEvent.click(await screen.findByRole('button', { name: /start interview/i }))

    await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/sessions/sess-42'))
  })
})
