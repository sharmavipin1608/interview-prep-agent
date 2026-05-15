import { useState, useRef, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useChat } from '../hooks/useChat'
import ChatMessage from '../components/ChatMessage'
import styles from './Interview.module.css'

export default function Interview() {
  const { id } = useParams()
  const navigate = useNavigate()
  const brief = JSON.parse(localStorage.getItem(`brief:${id}`) ?? 'null')
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const msgsEndRef = useRef(null)
  const chatMutation = useChat(id)

  useEffect(() => {
    if (msgsEndRef.current && typeof msgsEndRef.current.scrollIntoView === 'function') {
      msgsEndRef.current.scrollIntoView({ behavior: 'smooth' })
    }
  }, [messages])

  const send = () => {
    const text = input.trim()
    if (!text || chatMutation.isPending) return
    setInput('')
    setMessages(m => [...m, { role: 'user', content: text }])
    chatMutation.mutate(text, {
      onSuccess: ({ reply }) => setMessages(m => [...m, { role: 'ai', content: reply }]),
      onError: () => setMessages(m => [...m, { role: 'ai', content: 'Something went wrong. Please try again.' }]),
    })
  }

  const handleKey = (e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() } }

  return (
    <div className={styles.layout}>
      <div className={styles.brief}>
        {brief ? (
          <>
            <div className={styles.briefTitle}>Company Brief</div>
            <div className={styles.briefSection}>
              <div className={styles.briefLabel}>Company</div>
              <div className={styles.briefValue} style={{ fontWeight: 600 }}>{brief.companyName}</div>
            </div>
            <div className={styles.briefSection}>
              <div className={styles.briefLabel}>Summary</div>
              <div className={styles.briefValue}>{brief.companySummary}</div>
            </div>
            {brief.techStack?.length > 0 && (
              <div className={styles.briefSection}>
                <div className={styles.briefLabel}>Stack</div>
                <div className="tag-list">{brief.techStack.map(t => <span key={t} className="tag">{t}</span>)}</div>
              </div>
            )}
            {brief.likelyInterviewTopics?.length > 0 && (
              <div className={styles.briefSection}>
                <div className={styles.briefLabel}>Likely Topics</div>
                <div className="tag-list">{brief.likelyInterviewTopics.map(t => <span key={t} className="tag">{t}</span>)}</div>
              </div>
            )}
          </>
        ) : (
          <div className={styles.briefValue} style={{ color: 'var(--text4)' }}>Brief not available.</div>
        )}
        <div className={styles.briefFooter}>
          <button className="btn btn-secondary" style={{ width: '100%', fontSize: '12px' }} aria-label="End session" onClick={() => navigate(`/sessions/${id}/results`)}>
            End &amp; Evaluate
          </button>
        </div>
      </div>

      <div className={styles.chatPanel}>
        <div className={styles.chatHeader}>
          <div className={styles.interviewerAvatar}>🤖</div>
          <div>
            <div className={styles.interviewerName}>AI Interviewer{brief ? ` — ${brief.companyName}` : ''}</div>
            <div className={styles.interviewerSub}>Technical Interview Session</div>
          </div>
        </div>
        <div className={styles.msgs}>
          {messages.length === 0 && (
            <div className={styles.emptyChat}>The interviewer will ask the first question once you send a message below.</div>
          )}
          {messages.map((m, i) => <ChatMessage key={i} role={m.role} content={m.content} />)}
          {chatMutation.isPending && (
            <div className={styles.typing}>
              <span />
              <span />
              <span />
            </div>
          )}
          <div ref={msgsEndRef} />
        </div>
        <div className={styles.inputRow}>
          <input
            className={styles.input}
            placeholder="Type your answer…"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKey}
            disabled={chatMutation.isPending}
          />
          <button className="btn btn-secondary" onClick={() => navigate(`/sessions/${id}/results`)} aria-label="Evaluate">Evaluate</button>
          <button className="btn btn-primary" onClick={send} disabled={!input.trim() || chatMutation.isPending} aria-label="Send">Send</button>
        </div>
      </div>
    </div>
  )
}
