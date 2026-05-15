import { useState, useEffect } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'
import styles from './Layout.module.css'

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [isDark, setIsDark] = useState(
    () => localStorage.getItem('theme') === 'dark'
  )

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }, [isDark])

  return (
    <div className={styles.shell}>
      <div
        className={[styles.overlay, sidebarOpen ? styles.overlayOpen : ''].join(' ')}
        onClick={() => setSidebarOpen(false)}
      />
      <div className={[styles.sidebarWrap, sidebarOpen ? styles.sidebarOpen : ''].join(' ')}>
        <Sidebar onClose={() => setSidebarOpen(false)} />
      </div>
      <div className={styles.main}>
        <Topbar
          onMenuClick={() => setSidebarOpen(true)}
          isDark={isDark}
          onThemeToggle={() => setIsDark(d => !d)}
        />
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
