import axios from 'axios'

const http = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

export function unwrap(response) {
  const { data, error } = response.data
  if (error) throw new Error(error.message)
  return data
}

export const research = (body) =>
  http.post('/research', body).then(unwrap)

export const startSession = (body) =>
  http.post('/sessions', body).then(unwrap)

export const getSessions = () =>
  http.get('/sessions').then(unwrap)

export const getWeakAreas = () =>
  http.get('/sessions/weak-areas').then(unwrap)

export const chat = (sessionId, message) =>
  http.post(`/sessions/${sessionId}/chat`, { message }).then(unwrap)

export const evaluate = (sessionId) =>
  http.post(`/sessions/${sessionId}/evaluate`).then(unwrap)

export default http
