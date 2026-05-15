import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useResearch } from '../hooks/useResearch'
import { useStartSession } from '../hooks/useStartSession'
import styles from './NewInterview.module.css'

export default function NewInterview() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ companyName: '', jobTitle: '', jobDescription: '' })
  const [brief, setBrief] = useState(null)
  const [researchSessionId, setResearchSessionId] = useState(null)

  const researchMutation = useResearch()
  const startMutation = useStartSession()

  const handleResearch = () => {
    researchMutation.mutate(form, {
      onSuccess: ({ sessionId, companyBrief }) => {
        setResearchSessionId(sessionId)
        setBrief(companyBrief)
        localStorage.setItem(`brief:${sessionId}`, JSON.stringify(companyBrief))
      },
    })
  }

  const handleStart = () => {
    startMutation.mutate(
      { companyBriefSessionId: researchSessionId, jobDescription: form.jobDescription },
      {
        onSuccess: (interviewSessionId) => {
          localStorage.setItem(`brief:${interviewSessionId}`, JSON.stringify(brief))
          navigate(`/sessions/${interviewSessionId}`)
        },
      }
    )
  }

  const set = (field) => (e) => setForm(f => ({ ...f, [field]: e.target.value }))

  if (brief) {
    return (
      <div className={styles.card}>
        <h2>Company Brief</h2>
        <p className={styles.sub}>Review before your interview begins.</p>
        <div className={styles.briefHeader}>
          <div className={styles.briefCompany}>{brief.companyName}</div>
          <div className={styles.briefRole}>{form.jobTitle}</div>
        </div>
        <div className={styles.briefGrid}>
          <div className={styles.briefBox}>
            <div className={styles.briefLabel}>Summary</div>
            <div className={styles.briefText}>{brief.companySummary}</div>
          </div>
          <div className={styles.briefBox}>
            <div className={styles.briefLabel}>Culture</div>
            <div className={styles.briefText}>{brief.cultureSignals}</div>
          </div>
          <div className={styles.briefBox}>
            <div className={styles.briefLabel}>Tech Stack</div>
            <div className="tag-list">{brief.techStack?.map(t => <span key={t} className="tag">{t}</span>)}</div>
          </div>
          <div className={styles.briefBox}>
            <div className={styles.briefLabel}>Recent News</div>
            <div className={styles.briefText}>{brief.recentNews}</div>
          </div>
          <div className={[styles.briefBox, styles.full].join(' ')}>
            <div className={styles.briefLabel}>Likely Interview Topics</div>
            <div className="tag-list">{brief.likelyInterviewTopics?.map(t => <span key={t} className="tag">{t}</span>)}</div>
          </div>
        </div>
        <div className={styles.actions}>
          <button className="btn btn-secondary" onClick={() => setBrief(null)}>← Edit Details</button>
          <button className="btn btn-primary btn-lg" onClick={handleStart} disabled={startMutation.isPending}>
            {startMutation.isPending ? 'Starting…' : 'Start Interview →'}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className={styles.card}>
      <h2>New Interview</h2>
      <p className={styles.sub}>Tell us the company and role — we'll research it before the interview begins.</p>
      <div className={styles.formRow}>
        <div>
          <label className="form-label">Company Name</label>
          <input className="form-input" placeholder="Company name (e.g. Stripe)" value={form.companyName} onChange={set('companyName')} />
        </div>
        <div>
          <label className="form-label">Job Title</label>
          <input className="form-input" placeholder="e.g. Job title" value={form.jobTitle} onChange={set('jobTitle')} />
        </div>
      </div>
      <div style={{ marginBottom: '18px' }}>
        <label className="form-label">Job Description</label>
        <textarea className="form-input" style={{ height: '110px', resize: 'vertical' }} placeholder="Paste job description here…" value={form.jobDescription} onChange={set('jobDescription')} />
      </div>
      {researchMutation.isError && (
        <div className={styles.error}>Research failed: {researchMutation.error?.message}</div>
      )}
      <div className={styles.actions}>
        <button
          className="btn btn-primary btn-lg"
          onClick={handleResearch}
          disabled={!form.companyName || !form.jobTitle || !form.jobDescription || researchMutation.isPending}
        >
          {researchMutation.isPending ? 'Researching…' : '🔍 Research & Preview'}
        </button>
      </div>
      <div className={styles.hint}>Research takes ~15 seconds. We'll look up tech stack, culture signals, and recent news.</div>
    </div>
  )
}
