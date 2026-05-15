import styles from './StatCard.module.css'

export default function StatCard({ label, value, sub, subWarn }) {
  return (
    <div className={`card ${styles.card}`}>
      <div className={styles.label}>{label}</div>
      <div className={styles.value}>{value}</div>
      {sub && <div className={[styles.sub, subWarn ? styles.warn : ''].join(' ')}>{sub}</div>}
    </div>
  )
}
