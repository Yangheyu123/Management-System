<template>
  <div class="page-portal">
    <div class="pp-header">
      <h2 class="pp-title">我的账单</h2>
      <div class="summary" v-if="unpaidCount > 0">
        <span class="sum-label">待缴</span>
        <span class="sum-count">{{ unpaidCount }} 笔</span>
        <span class="sum-amount">¥ {{ unpaidAmount }}</span>
      </div>
    </div>

    <div class="pp-filter">
      <el-select v-model="filter.status" placeholder="状态筛选" clearable @change="load" style="width:140px">
        <el-option v-for="o in statusOpts" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
    </div>

    <div class="bill-list" v-loading="loading">
      <div class="bill-card" v-for="b in list" :key="b.id" :class="{ overdue: b.overdue }">
        <div class="bill-main">
          <div class="bill-head">
            <span class="bill-no">{{ b.billNo }}</span>
            <el-tag size="small" :type="tagType('billStatus', b.status)" effect="light">{{ dictName('billStatus', b.status) }}</el-tag>
            <el-tag size="small" type="danger" effect="plain" v-if="b.overdue">逾期</el-tag>
          </div>
          <div class="bill-title">{{ b.feeItemName }} · {{ b.period }}</div>
          <div class="bill-meta">
            <span>{{ b.houseNo }}</span>
            <span v-if="b.dueDate"><el-icon><Calendar /></el-icon> 截止 {{ b.dueDate }}</span>
          </div>
        </div>
        <div class="bill-amount">
          <div class="amt-row"><span class="amt-label">应收</span><span class="amt-val">¥{{ fmt(b.amount) }}</span></div>
          <div class="amt-row"><span class="amt-label">已缴</span><span class="amt-val paid">¥{{ fmt(b.paidAmount) }}</span></div>
          <div class="amt-row" v-if="unpaidOf(b) > 0"><span class="amt-label">待缴</span><span class="amt-val unpaid">¥{{ fmt(unpaidOf(b)) }}</span></div>
          <el-button v-if="b.status === 1 || b.status === 2" size="small" type="primary" @click="openPay(b)" style="margin-top:8px">立即缴费</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && !list.length" description="暂无账单" />
    </div>

    <el-pagination v-if="total > 0" :current-page="filter.page" :page-size="filter.size" :total="total"
      layout="prev, pager, next" @current-change="load" class="pp-page" />

    <!-- 缴费弹窗 -->
    <el-dialog v-model="payVisible" title="账单缴费" width="440px">
      <div class="pay-info" v-if="payTarget.id">
        <div class="pay-row"><span>账单</span><span>{{ payTarget.feeItemName }} · {{ payTarget.period }}</span></div>
        <div class="pay-row"><span>待缴金额</span><span class="pay-due">¥{{ fmt(unpaidOf(payTarget)) }}</span></div>
      </div>
      <el-form label-width="80px" style="margin-top:16px">
        <el-form-item label="缴费金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :max="unpaidOf(payTarget)" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="payForm.payMethod">
            <el-radio-button v-for="o in payMethodOpts.filter(p => [2,3,4].includes(p.value))" :key="o.value" :value="o.value">{{ o.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="payForm.remark" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { DICT, dictName, dictOptions, tagType } from '@/utils/dict'
import { portalBillPage, portalBillPay } from '@/api/portal'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const filter = reactive({ page: 1, size: 10, status: undefined })
const statusOpts = Object.entries(DICT.billStatus).map(([k, v]) => ({ value: Number(k), label: v }))
const payMethodOpts = dictOptions('payMethod')

const unpaidCount = computed(() => list.value.filter(b => b.status === 1 || b.status === 2).length)
const unpaidAmount = computed(() => fmt(list.value.filter(b => b.status === 1 || b.status === 2).reduce((s, b) => s + unpaidOf(b), 0)))

function unpaidOf(b) { return b ? Number(b.amount) - Number(b.paidAmount) : 0 }
function fmt(v) { return Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }

async function load() {
  loading.value = true
  try {
    const r = await portalBillPage({ page: filter.page, size: filter.size, status: filter.status })
    list.value = r.list; total.value = r.total
  } finally { loading.value = false }
}

// 缴费
const payVisible = ref(false)
const payLoading = ref(false)
const payTarget = ref({})
const payForm = reactive({ amount: 0, payMethod: 2, remark: '' })
function openPay(b) {
  payTarget.value = b
  payForm.amount = unpaidOf(b)
  payForm.payMethod = 2
  payForm.remark = ''
  payVisible.value = true
}
async function submitPay() {
  if (!payForm.payMethod) { ElMessage.warning('请选择支付方式'); return }
  payLoading.value = true
  try {
    await portalBillPay(payTarget.value.id, { ...payForm })
    ElMessage.success('缴费成功')
    payVisible.value = false; load()
  } catch (e) {} finally { payLoading.value = false }
}

onMounted(load)
</script>

<style scoped>
.page-portal { animation: pageIn .35s var(--ease-out, ease-out); }
.pp-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.pp-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--ink-900); }
.summary { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.sum-label { color: var(--ink-500); }
.sum-count { color: var(--ink-700); font-weight: 600; }
.sum-amount { color: #c45e3b; font-size: 18px; font-weight: 700; }
.pp-filter { margin-bottom: 14px; }
.pp-page { justify-content: center; margin-top: 18px; }

.bill-list { display: flex; flex-direction: column; gap: 12px; }
.bill-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200); border-radius: var(--radius-lg);
  padding: 18px 22px; box-shadow: var(--shadow-sm); display: flex; gap: 20px;
  transition: transform .2s, box-shadow .2s;
}
.bill-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.bill-card.overdue { border-left: 3px solid #c45e3b; }
.bill-main { flex: 1; }
.bill-head { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.bill-no { font-size: 13px; color: var(--ink-400); font-family: monospace; }
.bill-title { font-size: 16px; font-weight: 600; color: var(--ink-900); }
.bill-meta { display: flex; gap: 16px; font-size: 13px; color: var(--ink-500); margin-top: 6px; }
.bill-meta span { display: flex; align-items: center; gap: 4px; }
.bill-amount { width: 160px; border-left: 1px dashed var(--ink-200); padding-left: 20px; display: flex; flex-direction: column; }
.amt-row { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; }
.amt-label { color: var(--ink-400); }
.amt-val { color: var(--ink-700); font-weight: 600; }
.amt-val.paid { color: #6b8e23; }
.amt-val.unpaid { color: #c45e3b; font-size: 15px; }

.pay-info { background: var(--ink-50); border-radius: 8px; padding: 12px 16px; }
.pay-row { display: flex; justify-content: space-between; font-size: 14px; margin: 4px 0; }
.pay-row .pay-due { color: #c45e3b; font-weight: 700; font-size: 16px; }
</style>
