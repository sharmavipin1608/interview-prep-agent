import { useNavigate, useLocation } from 'react-router-dom'
import styles from './Topbar.module.css'

const titles = {
  '/':    'Dashboard',
  '/new': 'New Interview',
}

export function getTitle(pathname) {
  if (titles[pathname]) return titles[pathname]
  if (pathname.endsWith('/results')) return 'Session Results'
  if (pathname.startsWith('/sessions/')) return 'Active Session'
  return 'PrepAgent'
}

export default function Topbar({ onMenuClick, isDark, onThemeToggle }) {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const title = getTitle(pathname)
  return (
    <header className={styles.topbar} aria-label={title}>
      <div className={styles.left}>
        <button className={styles.hamburger} onClick={onMenuClick} aria-label="Open menu">☰</button>
      </div>
      <div className={styles.right}>
        <button
          className={styles.themeToggle}
          onClick={onThemeToggle}
          aria-label="Toggle dark/light theme"
        >
          {isDark ? '☀️' : '🌙'}
        </button>
        <button className="btn btn-primary" onClick={() => navigate('/new')}>＋ New</button>
      </div>
    </header>
  )
}
