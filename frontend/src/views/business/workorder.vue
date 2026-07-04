<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">工单管理</h2><div class="page-sub">报修、派单、处理、评价全流程</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="工单号" clearable @keyup.enter="load" style="width:160px" />
      <el-input v-model="query.title" placeholder="标题" clearable @keyup.enter="load" style="width:160px" />
      <el-select v-model="query.type" placeholder="类型" clearable><el-option v-for="o in dictOptions('woType')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-select v-model="query.status" placeholder="状态" clearable><el-option v-for="o in dictOptions('woStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-select v-model="query.priority" placeholder="优先级" clearable><el-option v-for="o in dictOptions('woPriority')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'business:workorder:add'" @click="openCreate">新增报修</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="工单号" width="140" />
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="communityName" label="小区" width="120" />
        <el-table-column prop="houseNo" label="房号" width="120" />
        <el-table-column prop="ownerName" label="报修人" width="90" />
        <el-table-column label="类型" width="80"><template #default="{ row }">{{ row.typeName || dictName('woType', row.type) }}</template></el-table-column>
        <el-table-column label="优先级" width="80"><template #default="{ row }"><el-tag :type="prioTag(row.priority)" size="small">{{ row.priorityName || dictName('woPriority', row.priority) }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType('woStatus', row.status)" size="small">{{ row.statusName || dictName('woStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="handlerName" label="维修员" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" v-if="row.status===1" v-permission="'business:workorder:assign'" @click="openAssign(row)">派单</el-button>
            <el-button link type="primary" v-if="row.status===2" v-permission="'business:workorder:accept'" @click="act(row, 'accept', { remark: '' })">接单</el-button>
            <el-button link type="success" v-if="row.status===3" v-permission="'business:workorder:finish'" @click="openFinish(row)">完成</el-button>
            <el-button link type="warning" v-if="row.status===4" v-permission="'business:workorder:close'" @click="act(row, 'close', { remark: '' })">关闭</el-button>
            <el-button link type="danger" v-if="row.status<4" @click="openCancel(row)">撤销</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <!-- 新增报修 -->
    <el-dialog v-model="createDialog" title="新增报修工单" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="小区" prop="communityId">
          <el-select v-model="form.communityId" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="类型" prop="type"><el-select v-model="form.type" style="width:100%"><el-option v-for="o in dictOptions('woType')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级"><el-select v-model="form.priority" style="width:100%"><el-option v-for="o in dictOptions('woPriority')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-form-item label="故障描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 派单 -->
    <el-dialog v-model="assignDialog" title="派单" width="440px">
      <el-form label-width="80px">
        <el-form-item label="维修员">
          <el-select v-model="assignForm.handlerId" style="width:100%" filterable>
            <el-option v-for="r in repairmen" :key="r.id" :label="r.realName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="assignForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定派单</el-button>
      </template>
    </el-dialog>

    <!-- 完成处理 -->
    <el-dialog v-model="finishDialog" title="处理完成" width="480px">
      <el-form label-width="90px">
        <el-form-item label="处理结果" required><el-input v-model="finishForm.handleResult" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialog = false">取消</el-button>
        <el-button type="success" @click="submitFinish">提交</el-button>
      </template>
    </el-dialog>

    <!-- 撤销 -->
    <el-dialog v-model="cancelDialog" title="撤销工单" width="440px">
      <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入撤销原因" />
      <template #footer>
        <el-button @click="cancelDialog = false">取消</el-button>
        <el-button type="danger" @click="submitCancel">确定撤销</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-drawer v-model="detailDialog" title="工单详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="状态"><el-tag :type="tagType('woStatus', detail.status)" size="small">{{ detail.statusName }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.typeName }}</el-descriptions-item>
          <el-descriptions-item label="优先级">{{ detail.priorityName }}</el-descriptions-item>
          <el-descriptions-item label="报修人">{{ detail.ownerName }} {{ detail.ownerPhone }}</el-descriptions-item>
          <el-descriptions-item label="房号">{{ detail.houseNo || '公共区域' }}</el-descriptions-item>
          <el-descriptions-item label="故障描述">{{ detail.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维修员">{{ detail.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理结果">{{ detail.handleResult || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detail.rating ? detail.rating + ' 星' : '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin:18px 0 10px">操作日志</h4>
        <el-timeline>
          <el-timeline-item v-for="l in (detail.logs || [])" :key="l.id" :timestamp="l.createTime" placement="top">
            <b>{{ l.action }}</b> · {{ l.operatorName }} <span v-if="l.toStatusName">→ {{ l.toStatusName }}</span>
            <div v-if="l.remark" style="color:#64748b;font-size:13px">{{ l.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { workorderPage, workorderDetail, workorderCreate, workorderAction } from '@/api/business'
import { communityAll, userSimpleList } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, orderNo: '', title: '', type: undefined, status: undefined, priority: undefined })
const communities = ref([]); const repairmen = ref([])

const createDialog = ref(false); const formRef = ref(); const saving = ref(false)
const form = reactive({ priority: 2 })
const rules = { communityId: [{ required: true, message: '请选择小区', trigger: 'change' }], title: [{ required: true, message: '请输入标题', trigger: 'blur' }], type: [{ required: true, message: '请选择类型', trigger: 'change' }] }

const assignDialog = ref(false); const assignForm = reactive({})
const finishDialog = ref(false); const finishForm = reactive({})
const cancelDialog = ref(false); const cancelReason = ref(''); const cancelRow = ref(null)
const detailDialog = ref(false); const detail = ref(null)

function prioTag(p) { return { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }[p] || '' }

async function load() {
  loading.value = true
  try { const d = await workorderPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, orderNo: '', title: '', type: undefined, status: undefined, priority: undefined }); load() }

function openCreate() {
  Object.keys(form).forEach(k => { if (k !== 'priority') delete form[k] })
  form.priority = 2; createDialog.value = true
}
async function submitCreate() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { await workorderCreate(form); ElMessage.success('报修成功'); createDialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
function openAssign(row) { assignForm.id = row.id; assignForm.handlerId = undefined; assignForm.remark = ''; assignDialog.value = true }
async function submitAssign() {
  if (!assignForm.handlerId) return ElMessage.warning('请选择维修员')
  await workorderAction(assignForm.id, 'assign', { handlerId: assignForm.handlerId, remark: assignForm.remark })
  ElMessage.success('派单成功'); assignDialog.value = false; load()
}
function openFinish(row) { finishForm.id = row.id; finishForm.handleResult = ''; finishDialog.value = true }
async function submitFinish() {
  if (!finishForm.handleResult) return ElMessage.warning('请填写处理结果')
  await workorderAction(finishForm.id, 'finish', { handleResult: finishForm.handleResult })
  ElMessage.success('已完成'); finishDialog.value = false; load()
}
function openCancel(row) { cancelRow.value = row; cancelReason.value = ''; cancelDialog.value = true }
async function submitCancel() {
  if (!cancelReason.value) return ElMessage.warning('请填写撤销原因')
  await workorderAction(cancelRow.value.id, 'cancel', { reason: cancelReason.value })
  ElMessage.success('已撤销'); cancelDialog.value = false; load()
}
async function act(row, action, data) {
  await ElMessageBox.confirm(`确定执行「${ { accept: '接单', close: '关闭' }[action] }」操作？`, '提示')
  await workorderAction(row.id, action, data); ElMessage.success('操作成功'); load()
}
async function openDetail(row) { detail.value = await workorderDetail(row.id); detailDialog.value = true }

onMounted(async () => {
  communities.value = await communityAll()
  repairmen.value = (await userSimpleList({ size: 100 })).list || []
  load()
})
</script>
