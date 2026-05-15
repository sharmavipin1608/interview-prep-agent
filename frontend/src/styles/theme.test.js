import { describe, it, expect, beforeEach } from 'vitest'

describe('theme CSS variables', () => {
  beforeEach(() => {
    document.documentElement.removeAttribute('data-theme')
  })

  it('applies dark theme attribute to html element', () => {
    document.documentElement.setAttribute('data-theme', 'dark')
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark')
  })

  it('removes dark theme when attribute is cleared', () => {
    document.documentElement.setAttribute('data-theme', 'dark')
    document.documentElement.removeAttribute('data-theme')
    expect(document.documentElement.getAttribute('data-theme')).toBeNull()
  })
})
