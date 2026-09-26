import axios from 'axios'

/**
 * HTTP client for the novel-backend REST API.
 * All backend communication of the frontend goes through this module.
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    timeout: 10000
})

/* ---------- Public reader API ---------- */

export const fetchNovels = (params) => api.get('/novels', { params })

export const fetchNovelDetail = (id) => api.get(`/novels/${id}`)

export const fetchPublishedChapter = (id) => api.get(`/chapters/${id}`)

/* ---------- Author studio API (draft management) ---------- */

export const createNovel = (data) => api.post('/author/novels', data)

// Includes draft chapters — author only
export const fetchAuthorChapters = (novelId) => api.get(`/author/novels/${novelId}/chapters`)

export const fetchAuthorChapter = (id) => api.get(`/author/chapters/${id}`)

export const createChapter = (novelId, data) => api.post(`/author/novels/${novelId}/chapters`, data)

export const updateChapter = (id, data) => api.put(`/author/chapters/${id}`, data)

export const updateChapterStatus = (id, status) =>
    api.put(`/author/chapters/${id}/status`, null, { params: { status } })

export const deleteChapter = (id) => api.delete(`/author/chapters/${id}`)

/** Extracts a readable message from a backend error response. */
export const errorMessage = (err) =>
    (err.response && err.response.data && err.response.data.message) || '操作失败，请稍后重试'

export default api
