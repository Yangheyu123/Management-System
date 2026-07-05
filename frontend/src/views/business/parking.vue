<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">车位管理</h2><div class="page-sub">车位登记、绑定、续费</div></div></div>
    <div class="toolbar">
      <el-select v-model="query.communityId" placeholder="小区" clearable @change="load" style="width:150px"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
      <el-input v-model="query.spaceNo" placeholder="车位号" clearable @keyup.enter="load" />
      <el-select v-model="query.areaType" placeholder="区域" clearable><el-option v-for="o in dictOptions('areaType')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-select v-model="query.status" placeholder="状态" clearable><el-option v-for="o in dictOptions('parkingStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'business:parking:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="spaceNo" label="车位号" width="100" />
        <el-table-column prop="communityName" label="小区" width="130" />
        <el-table-column label="区域" width="80"><template #default="{ row }">{{ row.areaTypeName || dictName('areaType', row.areaType) }}</template></el-table-column>
        <el-table-column label="用途" width="80"><template #default="{ row }">{{ row.useTypeName || dictName('useType', row.useType) }}</template></el-table-column>
        <el-table-column prop="ownerName" label="绑定业主" width="100" />
        <el-table-column prop="plateNo" label="车牌" width="100" />
        <el-table-column prop="monthlyFee" label="月租" width="80" />
        <el-table-column prop="endDate" label="到期日" width="110" />
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType('parkingStatus', row.status)" size="small">{{ row.statusName || dictName('parkingStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'business:parking:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="success" v-if="row.status===1||row.status===3" v-permission="'business:parking:bind'" @click="openBind(row)">绑定</el-button>
            <el-button link type="warning" v-if="row.status===2" v-permission="'business:parking:renew'" @click="openRenew(row)">续费</el-button>
            <el-button link type="danger" v-if="row.status===2" v-permission="'business:parking:unbind'" @click="unbind(row)">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑车位' : '新增车位'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="小区" prop="communityId"><el-select v-model="form.communityId" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="车位号" prop="spaceNo"><el-input v-model="form.spaceNo" /></el-form-item>
        <el-form-item label="区域" prop="areaType"><el-select v-model="form.areaType" style="width:100%"><el-option v-for="o in dictOptions('areaType')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="用途" prop="useType"><el-select v-model="form.useType" style="width:100%"><el-option v-for="o in dictOptions('useType')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="月租费"><el-input-number v-model="form.monthlyFee" :min="0" :precision="2" controls-position="right" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 绑定 -->
    <el-dialog v-model="bindDialog" title="绑定业主/车牌" width="460px">
      <el-form label-width="80px">
        <el-form-item label="业主"><el-select v-model="bindForm.ownerId" filterable style="width:100%"><el-option v-for="o in owners" :key="o.id" :label="o.name+' '+o.phone" :value="o.id" /></el-select></el-form-item>
        <el-form-item label="车牌"><el-input v-model="bindForm.plateNo" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="开始"><el-date-picker v-model="bindForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束"><el-date-picker v-model="bindForm.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="bindDialog = false">取消</el-button><el-button type="primary" @click="submitBind">确定绑定</el-button></template>
    </el-dialog>

    <!-- 续费 -->
    <el-dialog v-model="renewDialog" title="续费" width="380px">
      <el-form label-width="90px">
        <el-form-item label="续费月数"><el-input-number v-model="renewForm.months" :min="1" :max="36" controls-position="right" style="width:100%" /></el-form-item>
        <el-form-item label="金额(可选)"><el-input-number v-model="renewForm.amount" :min="0" :precision="2" controls-position="right" style="width:100%" placeholder="留空按月租计算" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="renewDialog = false">取消</el-button><el-button type="warning" @click="submitRenew">确定续费</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { parkingPage, parkingCreate, parkingUpdate, parkingDelete, parkingAction } from '@/api/business'
import { communityAll, ownerPage } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, communityId: undefined, spaceNo: '', areaType: undefined, status: undefined })
const communities = ref([]); const owners = ref([])
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  spaceNo: [{ required: true, message: '请输入车位号', trigger: 'blur' }],
  areaType: [{ required: true, message: '请选择区域', trigger: 'change' }],
  useType: [{ required: true, message: '请选择用途', trigger: 'change' }]
}
const bindDialog = ref(false); const bindForm = reactive({})
const renewDialog = ref(false); const renewForm = reactive({ months: 12 })

async function load() {
  loading.value = true
  try { const d = await parkingPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, communityId: undefined, spaceNo: '', areaType: undefined, status: undefined }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { areaType: 1, useType: 1, status: 1 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await parkingUpdate(form.id, form); else await parkingCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) { /* 删除按钮未在表格放，留接口 */ await parkingDelete(row.id); load() }
function openBind(row) { bindForm.id = row.id; bindForm.ownerId = undefined; bindForm.plateNo = ''; bindForm.startDate = ''; bindForm.endDate = ''; bindDialog.value = true }
async function submitBind() {
  if (!bindForm.ownerId) return ElMessage.warning('请选择业主')
  await parkingAction(bindForm.id, 'bind', { ...bindForm }); ElMessage.success('绑定成功'); bindDialog.value = false; load()
}
async function unbind(row) {
  await ElMessageBox.confirm('确定解绑该车位？', '提示', { type: 'warning' })
  await parkingAction(row.id, 'unbind', {}); ElMessage.success('已解绑'); load()
}
function openRenew(row) { renewForm.id = row.id; renewForm.months = 12; renewForm.amount = undefined; renewDialog.value = true }
async function submitRenew() {
  const r = await parkingAction(renewForm.id, 'renew', { months: renewForm.months, amount: renewForm.amount })
  ElMessage.success(`续费成功，新到期日 ${r.newEndDate}`); renewDialog.value = false; load()
}

onMounted(async () => {
  communities.value = await communityAll()
  owners.value = (await ownerPage({ size: 100 })).list || []
  load()
})
</script>
