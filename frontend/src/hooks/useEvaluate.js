import { useMutation, useQueryClient } from '@tanstack/react-query'
import { evaluate } from '../api/client'

export function useEvaluate(sessionId) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: () => evaluate(sessionId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  })
}
