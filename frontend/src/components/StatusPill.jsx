const map = {
  ACTIVE:    { label: 'In Progress', cls: 'pill-active' },
  COMPLETED: { label: 'Scored',      cls: 'pill-scored' },
}

export default function StatusPill({ status }) {
  const { label, cls } = map[status] ?? { label: status, cls: '' }
  return <span className={`pill ${cls}`}>{label}</span>
}
