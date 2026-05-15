import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Layout from './Layout'

const wrapper = ({ children }) => (
  <QueryClientProvider client={new QueryClient()}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
)

describe('Layout', () => {
  it('renders sidebar nav links', () => {
    render(<Layout />, { wrapper })
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText(/New Interview/i)).toBeInTheDocument()
  })

  it('toggles dark mode when theme button is clicked', () => {
    render(<Layout />, { wrapper })
    const toggle = screen.getByRole('button', { name: /toggle.*theme/i })
    expect(document.documentElement.getAttribute('data-theme')).not.toBe('dark')
    fireEvent.click(toggle)
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
    fireEvent.click(toggle)
    expect(document.documentElement.getAttribute('data-theme')).not.toBe('dark')
  })
})
