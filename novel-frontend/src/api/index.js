import axios from 'axios'

// 统一的前端 HTTP 客户端，后端接口地址见 novel-backend 的 Controller
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000
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
