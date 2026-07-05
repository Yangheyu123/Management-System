<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">费用管理</h2><div class="page-sub">账单生成、缴费、欠费统计</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.billNo" placeholder="账单号" clearable @keyup.enter="load" style="width:150px" />
      <el-select v-model="query.status" placeholder="状态" clearable><el-option v-for="o in dictOptions('billStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-select v-model="query.feeItemType" placeholder="费用类型" clearable><el-option v-for="o in dictOptions('feeType')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-input v-model="query.period" placeholder="账期 如 2026-07" clearable @keyup.enter="load" style="width:140px" />
      <el-select v-model="query.overdue" placeholder="只看欠费" clearable><el-option label="是" :value="1" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'business:bill:generate'" @click="genDialog = true">生成账单</el-button>
      <el-button plain @click="exportBills">导出 Excel</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="billNo" label="账单号" width="150" />
        <el-table-column prop="communityName" label="小区" width="120" />
        <el-table-column prop="houseNo" label="房号" width="120" />
        <el-table-column prop="ownerName" label="业主" width="90" />
        <el-table-column prop="feeItemName" label="收费项目" width="100" />
        <el-table-column prop="period" label="账期" width="90" />
        <el-table-column prop="amount" label="应收" width="90" />
        <el-table-column prop="paidAmount" label="已收" width="90" />
        <el-table-column prop="unpaidAmount" label="未收" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="tagType('billStatus', row.status)" size="small">{{ row.statusName }}</el-tag><el-tag v-if="row.overdue" type="danger" size="small" effect="plain" style="margin-left:4px">逾期</el-tag></template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日" width="110" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" v-if="row.status!==3 && row.status!==4" v-permission="'business:bill:pay'" @click="openPay(row)">缴费</el-button>
            <el-button link type="danger" v-if="row.status===1" v-permission="'business:bill:void'" @click="voidBill(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <!-- 生成账单 -->
    <el-dialog v-model="genDialog" title="批量生成账单" width="480px">
      <el-form label-width="90px">
        <el-form-item label="小区" required>
          <el-select v-model="gen.communityId" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="收费项目" required>
          <el-select v-model="gen.feeItemId" style="width:100%"><el-option v-for="f in feeItems" :key="f.id" :label="f.name" :value="f.id" /></el-select>
        </el-form-item>
        <el-form-item label="账期" required><el-date-picker v-model="gen.period" type="month" value-format="YYYY-MM" style="width:100%" /></el-form-item>
        <el-form-item label="截止日"><el-date-picker v-model="gen.dueDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="submitGen">生成</el-button>
      </template>
    </el-dialog>

    <!-- 缴费 -->
    <el-dialog v-model="payDialog" title="缴费" width="440px">
      <el-form label-width="90px">
        <el-form-item label="应收">{{ payRow.amount }} 元（未收 {{ payRow.unpaidAmount }} 元）</el-form-item>
        <el-form-item label="实缴金额" required><el-input-number v-model="payForm.amount" :min="0.01" :precision="2" controls-position="right" style="width:100%" /></el-form-item>
        <el-form-item label="缴费方式" required>
          <el-select v-model="payForm.payMethod" style="width:100%"><el-option v-for="o in dictOptions('payMethod')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="payForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { billPage, billGenerate, billPay, billVoid } from '@/api/business'
import { communityAll, feeItemList } from '@/api/basedata'
import { dictOptions, tagType } from '@/utils/dict'
import { getToken } from '@/utils/auth'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, billNo: '', status: undefined, feeItemType: undefined, period: '', overdue: undefined })
const communities = ref([]); const feeItems = ref([])

const genDialog = ref(false); const genLoading = ref(false)
const gen = reactive({ communityId: undefined, feeItemId: undefined, period: '', dueDate: '' })
const payDialog = ref(false); const payRow = reactive({}); const payForm = reactive({})

async function load() {
  loading.value = true
  try { const d = await billPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, billNo: '', status: undefined, feeItemType: undefined, period: '', overdue: undefined }); load() }

async function submitGen() {
  if (!gen.communityId || !gen.feeItemId || !gen.period) return ElMessage.warning('请补全必填项')
  genLoading.value = true
  try {
    const r = await billGenerate({ ...gen })
    ElMessage.success(`生成 ${r.generated} 条，跳过 ${r.skipped} 条`); genDialog.value = false; load()
  } catch(e){} finally { genLoading.value = false }
}
function openPay(row) { Object.keys(payRow).forEach(k => delete payRow[k]); Object.assign(payRow, row); payForm.amount = row.unpaidAmount; payForm.payMethod = 2; payForm.remark = ''; payDialog.value = true }
async function submitPay() {
  if (!payForm.amount || !payForm.payMethod) return ElMessage.warning('请补全')
  const r = await billPay(payRow.id, { ...payForm })
  ElMessage.success('缴费成功'); payDialog.value = false; load()
}
async function voidBill(row) {
  const { value } = await ElMessageBox.prompt('请输入作废原因', '作废账单', { type: 'warning', inputPattern: /.+/, inputErrorMessage: '请输入原因' })
  await billVoid(row.id, { reason: value }); ElMessage.success('已作废'); load()
}
function exportBills() {
  const params = new URLSearchParams()
  for (const k in query) if (query[k] !== '' && query[k] != null) params.append(k, query[k])
  const url = `/api/business/bills/export?${params.toString()}`
  // 带 token 下载
  const xhr = new XMLHttpRequest()
  xhr.open('GET', url); xhr.responseType = 'blob'
  xhr.setRequestHeader('Authorization', 'Bearer ' + getToken())
  xhr.onload = () => {
    if (xhr.status === 200) {
      const blob = xhr.response
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob); a.download = 'bills.xlsx'; a.click()
      URL.revokeObjectURL(a.href)
    } else ElMessage.error('导出失败')
  }
  xhr.send()
}

onMounted(async () => {
  communities.value = await communityAll()
  feeItems.value = (await feeItemList({ size: 100 })).list || []
  load()
})
</script>
