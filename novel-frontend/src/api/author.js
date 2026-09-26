import http from './http'

// 作者后台接口：可以访问包含草稿在内的全部数据
export const fetchMyNovels = () => http.get('/author/novels').then((res) => res.data)

export const createNovel = (payload) => http.post('/author/novels', payload).then((res) => res.data)

export const fetchAuthorNovel = (id) => http.get(`/author/novels/${id}`).then((res) => res.data)

export const fetchAuthorChapters = (id) =>
  http.get(`/author/novels/${id}/chapters`).then((res) => res.data)

export const createChapterDraft = (novelId, payload) =>
  http.post(`/author/novels/${novelId}/chapters`, payload).then((res) => res.data)

export const fetchAuthorChapter = (id) => http.get(`/author/chapters/${id}`).then((res) => res.data)

export const updateChapter = (id, payload) =>
  http.put(`/author/chapters/${id}`, payload).then((res) => res.data)

export const publishChapter = (id) =>
  http.post(`/author/chapters/${id}/publish`).then((res) => res.data)
