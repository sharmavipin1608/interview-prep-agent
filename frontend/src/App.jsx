import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import NewInterview from './pages/NewInterview'
import Interview from './pages/Interview'
import Results from './pages/Results'

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
