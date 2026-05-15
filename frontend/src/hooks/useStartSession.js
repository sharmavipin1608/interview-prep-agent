import { useMutation, useQueryClient } from '@tanstack/react-query'
import { startSession } from '../api/client'

export function useStartSession() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: startSession,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sessions'] }),
  })
}
