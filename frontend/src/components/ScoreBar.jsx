import styles from './ScoreBar.module.css'

const color = (s) => s >= 4 ? '#4f46e5' : s >= 3 ? '#f59e0b' : '#ef4444'

export default function ScoreBar({ score }) {
  if (score == null) return <span className={styles.dash}>—</span>
  return (
    <div className={styles.wrap}>
      <span className={styles.num}>{score}/5</span>
      <div className={styles.track}>
        <div className={styles.fill} style={{ width: `${score * 20}%`, background: color(score) }} />
      </div>
    </div>
  )
}
