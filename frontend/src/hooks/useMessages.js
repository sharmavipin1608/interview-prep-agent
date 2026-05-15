import { useQuery } from '@tanstack/react-query'
import { getMessages } from '../api/client'

export function useMessages(sessionId) {
  return useQuery({
    queryKey: ['messages', sessionId],
    queryFn: () => getMessages(sessionId),
    enabled: !!sessionId,
  })
}
