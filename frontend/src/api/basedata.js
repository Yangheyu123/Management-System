import request from '@/utils/request'

// 小区
export const communityPage = (params) => request.get('/basedata/communities', { params })
export const communityAll = () => request.get('/basedata/communities/all')
export const communityDetail = (id) => request.get(`/basedata/communities/${id}`)
export const communityCreate = (data) => request.post('/basedata/communities', data)
export const communityUpdate = (id, data) => request.put(`/basedata/communities/${id}`, data)
export const communityDelete = (id) => request.delete(`/basedata/communities/${id}`)

// 楼栋
export const buildingPage = (params) => request.get('/basedata/buildings', { params })
export const buildingDetail = (id) => request.get(`/basedata/buildings/${id}`)
export const buildingCreate = (data) => request.post('/basedata/buildings', data)
export const buildingUpdate = (id, data) => request.put(`/basedata/buildings/${id}`, data)
export const buildingDelete = (id) => request.delete(`/basedata/buildings/${id}`)

// 房屋
export const housePage = (params) => request.get('/basedata/houses', { params })
export const houseDetail = (id) => request.get(`/basedata/houses/${id}`)
export const houseCreate = (data) => request.post('/basedata/houses', data)
export const houseUpdate = (id, data) => request.put(`/basedata/houses/${id}`, data)
export const houseDelete = (id) => request.delete(`/basedata/houses/${id}`)
export const houseOwners = (id) => request.get(`/basedata/houses/${id}/owners`)

// 业主
export const ownerPage = (params) => request.get('/basedata/owners', { params })
export const ownerDetail = (id) => request.get(`/basedata/owners/${id}`)
export const ownerCreate = (data) => request.post('/basedata/owners', data)
export const ownerUpdate = (id, data) => request.put(`/basedata/owners/${id}`, data)
export const ownerDelete = (id) => request.delete(`/basedata/owners/${id}`)
export const ownerHouses = (id) => request.get(`/basedata/owners/${id}/houses`)

// 通用（收费项目下拉 / 楼栋下拉 / 维修员下拉）
export const feeItemList = (params) => request.get('/business/fee-items', { params })
export const userSimpleList = (params) => request.get('/system/users', { params })
