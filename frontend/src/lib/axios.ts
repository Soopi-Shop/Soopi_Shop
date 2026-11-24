import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor — đính kèm Clerk token nếu có
api.interceptors.request.use(async (config) => {
  // Token sẽ được inject thông qua hook useAuth của Clerk
  return config
})

// Response interceptor — xử lý lỗi chung
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login hoặc refresh token
      console.error('Unauthorized — redirect to login')
    }
    return Promise.reject(error)
  }
)

export default api
