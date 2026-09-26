import axios from 'axios'

// 开发环境走 vite 代理 (/api -> http://localhost:8080)，
// 生产环境由 nginx 把 /api 反代到 novel-backend 容器。
// 接口实现全部在 novel-backend 工程，前端只做 HTTP 调用。
const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000
})

export default http
