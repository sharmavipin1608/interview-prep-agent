import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import NewInterview from './pages/NewInterview'
import Interview from './pages/Interview'

const Placeholder = ({ name }) => <div style={{ padding: 24 }}>{name}</div>
const Results      = () => <Placeholder name="Results" />

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
