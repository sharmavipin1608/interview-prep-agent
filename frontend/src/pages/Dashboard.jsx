import { useNavigate } from 'react-router-dom'
import { useSessions } from '../hooks/useSessions'
import { useWeakAreas } from '../hooks/useWeakAreas'
import StatCard from '../components/StatCard'
import StatusPill from '../components/StatusPill'
import ScoreBar from '../components/ScoreBar'
import styles from './Dashboard.module.css'

export default function Dashboard() {
  const navigate = useNavigate()
  const { data: sessions = [], isLoading } = useSessions()
  const { data: weakAreas = [] } = useWeakAreas()

  const scoredSessions = sessions.filter(s => s.overallScore != null)
  const avgScore = scoredSessions.length
    ? Math.round(scoredSessions.reduce((sum, s) => sum + s.overallScore, 0) / scoredSessions.length)
    : null
  const uniqueCompanies = [...new Set(sessions.map(s => s.companyName))]

  return (
    <div>
      <div className={styles.stats}>
        <StatCard label="Total Sessions" value={sessions.length} sub={`${scoredSessions.length} scored`} />
        <StatCard label="Avg Score"      value={avgScore ?? '—'} />
        <StatCard label="Companies"      value={uniqueCompanies.length} sub={uniqueCompanies.slice(0, 3).join(', ')} />
        <StatCard label="Top Weak Area"  value={weakAreas[0] ?? '—'} sub={weakAreas[0] ? '⚠ Needs attention' : ''} subWarn={!!weakAreas[0]} />
      </div>

      <div className="section-header">
        <div className="section-title">Recent Sessions</div>
      </div>

      <div className={`card ${styles.table}`}>
        <div className={styles.tableHeader}>
          <span>Company / Role</span>
          <span>Date</span>
          <span>Status</span>
          <span>Score</span>
          <span />
        </div>
        {isLoading && <div className={styles.empty}>Loading…</div>}
        {!isLoading && sessions.length === 0 && (
          <div className={styles.empty}>
            No sessions yet.{' '}
            <span className={styles.emptyLink} onClick={() => navigate('/new')}>
              Start your first interview →
            </span>
          </div>
        )}
        {sessions.map(s => (
          <div
            key={s.id}
            className={styles.row}
            onClick={() => navigate(s.status === 'ACTIVE' ? `/sessions/${s.id}` : `/sessions/${s.id}/results`)}
          >
            <div>
              <div className={styles.company}>{s.companyName}</div>
              <div className={styles.role}>{s.jobTitle}</div>
            </div>
            <div className={styles.date}>
              {new Date(s.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
            </div>
            <div className={styles.cell}><StatusPill status={s.status} /></div>
            <div className={styles.scoreCell}><ScoreBar score={s.overallScore} /></div>
            <div className={styles.action}>{s.status === 'ACTIVE' ? 'Continue →' : 'Review →'}</div>
          </div>
        ))}
      </div>

      {weakAreas.length > 0 && (
        <div className={`card ${styles.weakCard}`}>
          <div className="section-header">
            <div className="section-title">Weak Areas to Focus On</div>
          </div>
          <div className={styles.weakList}>
            {weakAreas.map(w => (
              <span key={w} className={styles.weakChip}>{w}</span>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
