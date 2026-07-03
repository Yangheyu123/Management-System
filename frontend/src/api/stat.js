import request from '@/utils/request'

export const dashboard = (params) => request.get('/stat/dashboard', { params })
export const chargeSummary = (params) => request.get('/stat/charge/summary', { params })
export const chargeTrend = (params) => request.get('/stat/charge/trend', { params })
export const chargeByType = (params) => request.get('/stat/charge/by-type', { params })
export const workorderSummary = (params) => request.get('/stat/workorder/summary', { params })
export const workorderByStatus = (params) => request.get('/stat/workorder/by-status', { params })
export const workorderByHandler = (params) => request.get('/stat/workorder/by-handler', { params })
export const parkingUsage = (params) => request.get('/stat/parking/usage', { params })
export const equipmentStatus = (params) => request.get('/stat/equipment/status', { params })
