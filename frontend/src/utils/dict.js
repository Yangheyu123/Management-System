// 前后端共享枚举翻译，与后端 Dict.java 一致
export const DICT = {
  gender: { 0: '未知', 1: '男', 2: '女' },
  userType: { 1: '员工', 2: '业主' },
  commonStatus: { 1: '启用', 0: '禁用' },
  houseStatus: { 1: '空置', 2: '已售', 3: '已入住', 4: '装修中' },
  ownerStatus: { 1: '在住', 2: '已搬离' },
  woType: { 1: '水电', 2: '土建', 3: '门窗', 4: '公共设施', 5: '其他' },
  woPriority: { 1: '低', 2: '中', 3: '高', 4: '紧急' },
  woStatus: { 1: '待派单', 2: '已派单', 3: '处理中', 4: '已完成', 5: '已关闭', 6: '已撤销' },
  feeType: { 1: '物业费', 2: '水费', 3: '电费', 4: '车位费', 5: '其他' },
  billStatus: { 1: '未缴', 2: '部分缴纳', 3: '已缴清', 4: '已作废' },
  payMethod: { 1: '现金', 2: '微信', 3: '支付宝', 4: '银行转账', 5: 'POS' },
  billingCycle: { 1: '月', 2: '季', 3: '年', 4: '一次性' },
  areaType: { 1: '地上', 2: '地下' },
  useType: { 1: '长租', 2: '出售', 3: '临时' },
  parkingStatus: { 1: '空闲', 2: '使用中', 3: '已售', 4: '已禁用' },
  equipCategory: { 1: '通闸设备', 2: '消防设备', 3: '安防设备' },
  equipStatus: { 1: '正常', 2: '故障', 3: '维修中', 4: '报废' },
  checkResult: { 1: '正常', 2: '异常' },
  online: { 1: '在线', 0: '离线' }
}

export function dictName(key, val) {
  const m = DICT[key]
  return m && val != null ? (m[val] ?? val) : val
}

// 各字典生成 el-option 列表
export function dictOptions(key) {
  const m = DICT[key] || {}
  return Object.entries(m).map(([k, v]) => ({ value: Number(k), label: v }))
}

// 状态对应的 el-tag type，用于表格着色
export const TAG_TYPE = {
  billStatus: { 1: 'danger', 2: 'warning', 3: 'success', 4: 'info' },
  woStatus: { 1: 'info', 2: 'warning', 3: 'warning', 4: 'primary', 5: 'success', 6: 'info' },
  equipStatus: { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info' },
  parkingStatus: { 1: 'info', 2: 'primary', 3: 'success', 4: 'info' },
  commonStatus: { 1: 'success', 0: 'danger' },
  online: { 1: 'success', 0: 'danger' }
}

export function tagType(key, val) {
  const m = TAG_TYPE[key]
  return m && val != null ? (m[val] ?? '') : ''
}
