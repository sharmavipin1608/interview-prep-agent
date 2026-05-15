import { Routes, Route, Navigate } from 'react-router-dom'

const Placeholder = ({ name }) => <div style={{ padding: 24 }}>{name}</div>

const Layout    = () => <Placeholder name="Layout (coming soon)" />
const Dashboard = () => <Placeholder name="Dashboard" />
const NewInterview = () => <Placeholder name="New Interview" />
const Interview = () => <Placeholder name="Interview" />
const Results   = () => <Placeholder name="Results" />

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="new" element={<NewInterview />} />
        <Route path="sessions/:id" element={<Interview />} />
        <Route path="sessions/:id/results" element={<Results />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
