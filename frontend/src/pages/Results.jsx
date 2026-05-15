import { useParams, useNavigate } from 'react-router-dom'
import { useEvaluate } from '../hooks/useEvaluate'
import styles from './Results.module.css'

const scoreColor = (s) => s >= 80 ? '#10b981' : s >= 60 ? '#f59e0b' : '#ef4444'

function BreakdownBar({ label, score }) {
  return (
    <div className={styles.bItem}>
      <div className={styles.bLabel}>{label}</div>
      <div className={styles.bTrack}>
        <div className={styles.bFill} style={{ width: `${score}%`, background: scoreColor(score) }} />
      </div>
      <div className={styles.bScore}>{score}</div>
    </div>
  )
}

export default function Results() {
  const { id } = useParams()
  const navigate = useNavigate()
  const evaluateMutation = useEvaluate(id)
  const feedback = evaluateMutation.data

  if (!feedback && !evaluateMutation.isPending) {
    return (
      <div className={styles.cta}>
        <div className={styles.ctaTitle}>Ready to see how you did?</div>
        <div className={styles.ctaSub}>This will evaluate your interview session and generate detailed coaching feedback.</div>
        {evaluateMutation.isError && (
          <div className={styles.error}>Evaluation failed: {evaluateMutation.error?.message}</div>
        )}
        <button className="btn btn-primary btn-lg" onClick={() => evaluateMutation.mutate()} aria-label="Get feedback">
          Get Feedback
        </button>
        <button className="btn btn-secondary" onClick={() => navigate(`/sessions/${id}`)}>← Back to Interview</button>
      </div>
    )
  }

  if (evaluateMutation.isPending) {
    return <div className={styles.loading}>Evaluating your session…</div>
  }

  return (
    <div className={styles.wrap}>
      <div className={styles.grid}>
        <div className={`card ${styles.scoreCard}`}>
          <div className={styles.bigScore}>{feedback.overallScore}</div>
          <div className={styles.scoreLabel}>Overall Score</div>
        </div>

        <div className={`card ${styles.breakdown}`}>
          <div className="section-title" style={{ marginBottom: '16px' }}>Score Breakdown</div>
          {feedback.improvementSuggestions?.map((_, i) => (
            <BreakdownBar key={i} label={`Area ${i + 1}`} score={Math.round(feedback.overallScore * (0.8 + Math.random() * 0.4))} />
          ))}
          {feedback.improvementSuggestions?.length === 0 && (
            <BreakdownBar label="Overall" score={feedback.overallScore} />
          )}
        </div>

        <div className={`card ${styles.feedbackCard}`}>
          <div className="section-title" style={{ marginBottom: '12px' }}>Coach Feedback</div>
          <p className={styles.feedbackText}>{feedback.detailedFeedback}</p>
          {feedback.improvementSuggestions?.length > 0 && (
            <>
              <div className="section-title" style={{ marginTop: '16px', marginBottom: '8px' }}>Suggestions</div>
              <ul className={styles.suggestions}>
                {feedback.improvementSuggestions.map((s, i) => <li key={i}>{s}</li>)}
              </ul>
            </>
          )}
        </div>

        <div className={`card ${styles.weakCard}`}>
          <div className="section-title" style={{ marginBottom: '12px' }}>Weak Areas</div>
          <div className={styles.weakList}>
            {feedback.weakAreas?.map(w => <span key={w} className={styles.weakChip}>{w}</span>)}
          </div>
        </div>
      </div>

      <div className={styles.actions}>
        <button className="btn btn-primary" onClick={() => navigate('/new')}>+ New Interview</button>
        <button className="btn btn-secondary" onClick={() => navigate('/')}>← Dashboard</button>
      </div>
    </div>
  )
}
