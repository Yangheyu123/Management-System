import request from '@/utils/request'

// 用户
export const userPage = (params) => request.get('/system/users', { params })
export const userDetail = (id) => request.get(`/system/users/${id}`)
export const userCreate = (data) => request.post('/system/users', data)
export const userUpdate = (id, data) => request.put(`/system/users/${id}`, data)
export const userDelete = (id) => request.delete(`/system/users/${id}`)
export const userResetPwd = (id, data) => request.put(`/system/users/${id}/reset-password`, data)
export const userStatus = (id, data) => request.put(`/system/users/${id}/status`, data)
export const userRoles = (id) => request.get(`/system/users/${id}/roles`)
export const userAssignRoles = (id, data) => request.put(`/system/users/${id}/roles`, data)

// 角色
export const roleList = (params) => request.get('/system/roles', { params })
export const roleDetail = (id) => request.get(`/system/roles/${id}`)
export const roleCreate = (data) => request.post('/system/roles', data)
export const roleUpdate = (id, data) => request.put(`/system/roles/${id}`, data)
export const roleDelete = (id) => request.delete(`/system/roles/${id}`)
export const rolePermissions = (id) => request.get(`/system/roles/${id}/permissions`)
export const roleAssignPermissions = (id, data) => request.put(`/system/roles/${id}/permissions`, data)

// 权限
export const permTree = () => request.get('/system/permissions/tree')
export const permPage = (params) => request.get('/system/permissions', { params })
export const permCreate = (data) => request.post('/system/permissions', data)
export const permUpdate = (id, data) => request.put(`/system/permissions/${id}`, data)
export const permDelete = (id) => request.delete(`/system/permissions/${id}`)
