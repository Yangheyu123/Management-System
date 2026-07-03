import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getUserInfo = () => request.get('/auth/info')
export const refreshToken = () => request.post('/auth/refresh')
export const changePassword = (data) => request.put('/auth/password', data)
