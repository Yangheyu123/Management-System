import request from '@/utils/request'

// 业主端接口（/api/portal）
export const portalOverview = () => request.get('/portal/overview')

export const portalHouses = () => request.get('/portal/houses')

export const portalWorkorderPage = (params) => request.get('/portal/workorders', { params })

export const portalWorkorderCreate = (data) => request.post('/portal/workorders', data)

export const portalWorkorderRate = (id, data) => request.post(`/portal/workorders/${id}/rate`, data)

export const portalBillPage = (params) => request.get('/portal/bills', { params })

export const portalBillPay = (id, data) => request.post(`/portal/bills/${id}/pay`, data)
