import axios from 'axios'

// 统一的前端 HTTP 客户端，后端接口地址见 novel-backend 的 Controller。
// 默认使用相对路径 /api：开发环境由 vite dev server 代理（见 vite.config.js），
// 生产环境由 nginx 代理到后端容器（见 nginx.conf），
// 这样远程访问页面时请求会落到当前访问的域名，而不是访客本机的 localhost:8080。
// 如需直连其他后端，可通过 VITE_API_URL 环境变量覆盖。
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000
})

// ---------- 作者身份 ----------
// 无登录体系，使用 localStorage 持久化的随机 ID 标识当前浏览器对应的作者，
// 作者后台接口统一携带 X-Author-Id 请求头，后端据此隔离不同作者的小说与草稿。
const AUTHOR_ID_KEY = 'novel_author_id'

const generateAuthorId = () => {
  if (window.crypto && typeof window.crypto.randomUUID === 'function') {
    return window.crypto.randomUUID()
  }
  // 非安全上下文（如 http://局域网IP 访问）下 randomUUID 不可用，退化为随机串
  return `author-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

export const getAuthorId = () => {
  let authorId = localStorage.getItem(AUTHOR_ID_KEY)
  if (!authorId) {
    authorId = generateAuthorId()
    localStorage.setItem(AUTHOR_ID_KEY, authorId)
  }
  return authorId
}

// 仅作者后台接口携带身份头，读者侧接口保持匿名
api.interceptors.request.use((config) => {
  if (config.url && config.url.startsWith('/author')) {
    config.headers['X-Author-Id'] = getAuthorId()
  }
  return config
})

// ---------- 读者侧公开接口 ----------

export const fetchNovels = (params) => api.get('/novels', { params })

export const fetchNovelDetail = (id) => api.get(`/novels/${id}`)

export const fetchChapterContent = (id) => api.get(`/chapters/${id}`)

// ---------- 作者后台接口（含草稿） ----------

export const createNovel = (data) => api.post('/author/novels', data)

export const fetchAuthorNovels = () => api.get('/author/novels')

export const fetchAuthorChapters = (novelId) => api.get(`/author/novels/${novelId}/chapters`)

export const createChapterDraft = (novelId, data) => api.post(`/author/novels/${novelId}/chapters`, data)

export const fetchAuthorChapter = (id) => api.get(`/author/chapters/${id}`)

export const saveChapterDraft = (id, data) => api.put(`/author/chapters/${id}`, data)

export const publishChapter = (id) => api.post(`/author/chapters/${id}/publish`)

export default api
