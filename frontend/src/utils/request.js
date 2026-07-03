import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 20000
})

// 请求拦截：自动带 token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) config.headers['Authorization'] = 'Bearer ' + token
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：统一拆 Result
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 二进制文件（Excel 导出）直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    if (res.code === 200) {
      return res.data
    }
    // 业务错误
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已失效')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
