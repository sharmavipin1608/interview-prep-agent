import { useQuery } from '@tanstack/react-query'
import { getSessions } from '../api/client'

export function useSessions() {
  return useQuery({ queryKey: ['sessions'], queryFn: getSessions })
}
