import http from './http'

// 公开读者接口：只包含已发布章节
export const fetchNovels = (params) => http.get('/novels', { params }).then((res) => res.data)

export const fetchNovelDetail = (id) => http.get(`/novels/${id}`).then((res) => res.data)

export const fetchChapter = (id) => http.get(`/chapters/${id}`).then((res) => res.data)
