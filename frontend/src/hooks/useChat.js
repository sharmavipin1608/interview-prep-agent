import { useMutation } from '@tanstack/react-query'
import { chat } from '../api/client'

export function useChat(sessionId) {
  return useMutation({
    mutationFn: (message) => chat(sessionId, message),
  })
}
