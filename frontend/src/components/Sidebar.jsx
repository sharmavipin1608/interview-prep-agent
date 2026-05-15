import { NavLink } from 'react-router-dom'
import styles from './Sidebar.module.css'

export default function Sidebar({ onClose }) {
  const navItem = (to, icon, label) => (
    <NavLink
      to={to}
      end={to === '/'}
      className={({ isActive }) => [styles.navItem, isActive ? styles.active : ''].join(' ')}
      onClick={onClose}
    >
      <span className={styles.icon}>{icon}</span>
      <span className={styles.label}>{label}</span>
    </NavLink>
  )

  return (
    <nav className={styles.sidebar}>
      <div className={styles.logo}>
        <div className={styles.logoText}>PrepAgent</div>
        <div className={styles.logoSub}>AI Interview Coach</div>
      </div>
      <div className={styles.nav}>
        <div className={styles.navLabel}>Menu</div>
        {navItem('/', '⊞', 'Dashboard')}
        {navItem('/new', '＋', 'New Interview')}
      </div>
      <div className={styles.footer}>
        <div className={styles.avatar}>VS</div>
        <div>
          <div className={styles.userName}>Vipin Sharma</div>
          <div className={styles.userRole}>Software Engineer</div>
        </div>
      </div>
    </nav>
  )
}
