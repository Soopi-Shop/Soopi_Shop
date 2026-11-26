import { ClerkProvider } from '@clerk/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
    },
  },
})

const PUBLISHABLE_KEY = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY

const isClerkConfigured = Boolean(
  PUBLISHABLE_KEY &&
    PUBLISHABLE_KEY !== 'pk_test_xxx' &&
    !PUBLISHABLE_KEY.includes('your_clerk_publishable_key'),
)

if (!isClerkConfigured) {
  console.warn(
    '[Clerk] Missing or placeholder VITE_CLERK_PUBLISHABLE_KEY. Running in fallback mode without ClerkProvider.',
  )
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    {isClerkConfigured && PUBLISHABLE_KEY ? (
      <ClerkProvider publishableKey={PUBLISHABLE_KEY}>
        <QueryClientProvider client={queryClient}>
          <App />
        </QueryClientProvider>
      </ClerkProvider>
    ) : (
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    )}
  </StrictMode>,
)
