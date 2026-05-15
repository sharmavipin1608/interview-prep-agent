import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import StatCard from './StatCard'
import StatusPill from './StatusPill'
import ScoreBar from './ScoreBar'
import ChatMessage from './ChatMessage'

describe('StatCard', () => {
  it('renders label and value', () => {
    render(<StatCard label="Total Sessions" value={12} />)
    expect(screen.getByText('Total Sessions')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
  })
  it('renders sub text when provided', () => {
    render(<StatCard label="Score" value={74} sub="↑ +6 this week" />)
    expect(screen.getByText('↑ +6 this week')).toBeInTheDocument()
  })
})

describe('StatusPill', () => {
  it('renders ACTIVE status as In Progress', () => {
    render(<StatusPill status="ACTIVE" />)
    expect(screen.getByText('In Progress')).toBeInTheDocument()
  })
  it('renders COMPLETED status as Scored', () => {
    render(<StatusPill status="COMPLETED" />)
    expect(screen.getByText('Scored')).toBeInTheDocument()
  })
})

describe('ScoreBar', () => {
  it('renders score number', () => {
    render(<ScoreBar score={82} />)
    expect(screen.getByText('82')).toBeInTheDocument()
  })
  it('renders dash when score is null', () => {
    render(<ScoreBar score={null} />)
    expect(screen.getByText('—')).toBeInTheDocument()
  })
})

describe('ChatMessage', () => {
  it('renders AI message content', () => {
    render(<ChatMessage role="ai" content="Tell me about yourself." />)
    expect(screen.getByText('Tell me about yourself.')).toBeInTheDocument()
  })
  it('renders user message content', () => {
    render(<ChatMessage role="user" content="I have 5 years of experience." />)
    expect(screen.getByText('I have 5 years of experience.')).toBeInTheDocument()
  })
})
