import { useMutation } from '@tanstack/react-query'
import { research } from '../api/client'

export function useResearch() {
  return useMutation({ mutationFn: research })
}
