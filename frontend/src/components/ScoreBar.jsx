import styles from './ScoreBar.module.css'

const color = (s) => s >= 80 ? '#4f46e5' : s >= 60 ? '#f59e0b' : '#ef4444'

export default function ScoreBar({ score }) {
  if (score == null) return <span className={styles.dash}>—</span>
  return (
    <div className={styles.wrap}>
      <span className={styles.num}>{score}</span>
      <div className={styles.track}>
        <div className={styles.fill} style={{ width: `${score}%`, background: color(score) }} />
      </div>
    </div>
  )
}
