import styles from './ChatMessage.module.css'

export default function ChatMessage({ role, content }) {
  const isUser = role === 'user'
  return (
    <div className={[styles.msg, isUser ? styles.user : styles.ai].join(' ')}>
      <div className={styles.avatar}>{isUser ? 'VS' : '🤖'}</div>
      <div className={styles.bubble}>{content}</div>
    </div>
  )
}
