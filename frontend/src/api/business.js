import request from '@/utils/request'

// 工单
export const workorderPage = (params) => request.get('/business/workorders', { params })
export const workorderDetail = (id) => request.get(`/business/workorders/${id}`)
export const workorderCreate = (data) => request.post('/business/workorders', data)
export const workorderUpdate = (id, data) => request.put(`/business/workorders/${id}`, data)
export const workorderAction = (id, action, data) => request.post(`/business/workorders/${id}/${action}`, data)

// 收费项目
export const feeItemPage = (params) => request.get('/business/fee-items', { params })
export const feeItemCreate = (data) => request.post('/business/fee-items', data)
export const feeItemUpdate = (id, data) => request.put(`/business/fee-items/${id}`, data)
export const feeItemDelete = (id) => request.delete(`/business/fee-items/${id}`)

// 账单
export const billPage = (params) => request.get('/business/bills', { params })
export const billDetail = (id) => request.get(`/business/bills/${id}`)
export const billGenerate = (data) => request.post('/business/bills/generate', data)
export const billPay = (id, data) => request.post(`/business/bills/${id}/pay`, data)
export const billVoid = (id, data) => request.post(`/business/bills/${id}/void`, data)
export const billExportUrl = '/api/business/bills/export'

// 车位
export const parkingPage = (params) => request.get('/business/parking/spaces', { params })
export const parkingDetail = (id) => request.get(`/business/parking/spaces/${id}`)
export const parkingCreate = (data) => request.post('/business/parking/spaces', data)
export const parkingUpdate = (id, data) => request.put(`/business/parking/spaces/${id}`, data)
export const parkingDelete = (id) => request.delete(`/business/parking/spaces/${id}`)
export const parkingAction = (id, action, data) => request.post(`/business/parking/spaces/${id}/${action}`, data)
export const parkingRecords = (params) => request.get('/business/parking/records', { params })

// 设备
export const equipmentPage = (params) => request.get('/business/equipments', { params })
export const equipmentDetail = (id) => request.get(`/business/equipments/${id}`)
export const equipmentCreate = (data) => request.post('/business/equipments', data)
export const equipmentUpdate = (id, data) => request.put(`/business/equipments/${id}`, data)
export const equipmentDelete = (id) => request.delete(`/business/equipments/${id}`)
export const equipmentCheck = (id, data) => request.post(`/business/equipments/${id}/check`, data)
export const equipmentChecks = (id, params) => request.get(`/business/equipments/${id}/checks`, { params })
export const equipmentExpiring = (params) => request.get('/business/equipments/expiring', { params })
