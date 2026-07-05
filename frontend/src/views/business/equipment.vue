<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">设备管理</h2><div class="page-sub">通闸 / 消防 / 安防 设备统一管理</div></div></div>
    <div class="toolbar">
      <el-select v-model="query.communityId" placeholder="小区" clearable @change="load" style="width:150px"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
      <el-select v-model="query.category" placeholder="类别" clearable><el-option v-for="o in dictOptions('equipCategory')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-input v-model="query.name" placeholder="名称" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable><el-option v-for="o in dictOptions('equipStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'business:equipment:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="code" label="编号" width="90" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column label="类别" width="100"><template #default="{ row }">{{ row.categoryName || dictName('equipCategory', row.category) }}</template></el-table-column>
        <el-table-column prop="location" label="位置" width="120" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="100" />
        <el-table-column label="在线" width="80"><template #default="{ row }"><el-tag :type="tagType('online', row.onlineStatus)" size="small">{{ dictName('online', row.onlineStatus) }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="tagType('equipStatus', row.status)" size="small">{{ row.statusName || dictName('equipStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="nextCheckDate" label="下次巡检" width="110" />
        <el-table-column prop="warrantyDate" label="保修截止" width="110" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'business:equipment:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="success" v-permission="'business:equipment:check'" @click="openCheck(row)">巡检</el-button>
            <el-button link @click="openChecks(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑设备' : '新增设备'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="小区" prop="communityId"><el-select v-model="form.communityId" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类别" prop="category"><el-select v-model="form.category" style="width:100%"><el-option v-for="o in dictOptions('equipCategory')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="编号" prop="code"><el-input v-model="form.code" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="位置"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号"><el-input v-model="form.model" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="厂商"><el-input v-model="form.manufacturer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="安装日期"><el-date-picker v-model="form.installDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="保修截止"><el-date-picker v-model="form.warrantyDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="在线状态"><el-select v-model="form.onlineStatus" style="width:100%"><el-option label="在线" :value="1" /><el-option label="离线" :value="0" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="运行状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('equipStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存</el-button></template>
    </el-dialog>

    <!-- 巡检 -->
    <el-dialog v-model="checkDialog" title="提交巡检" width="460px">
      <el-form label-width="90px">
        <el-form-item label="结果" required><el-radio-group v-model="checkForm.result"><el-radio :label="1">正常</el-radio><el-radio :label="2">异常</el-radio></el-radio-group></el-form-item>
        <el-form-item label="异常描述" v-if="checkForm.result===2" required><el-input v-model="checkForm.issueDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="下次巡检"><el-date-picker v-model="checkForm.nextCheckDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="checkDialog = false">取消</el-button><el-button type="success" @click="submitCheck">提交</el-button></template>
    </el-dialog>

    <!-- 巡检记录 -->
    <el-drawer v-model="checksDrawer" title="巡检记录" size="520px">
      <el-table :data="checks" border>
        <el-table-column prop="checkerName" label="巡检人" width="90" />
        <el-table-column prop="checkTime" label="时间" width="160" />
        <el-table-column label="结果" width="80"><template #default="{ row }"><el-tag :type="row.result===1?'success':'danger'" size="small">{{ row.resultName }}</el-tag></template></el-table-column>
        <el-table-column prop="issueDesc" label="说明" show-overflow-tooltip />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { equipmentPage, equipmentCreate, equipmentUpdate, equipmentCheck, equipmentChecks } from '@/api/business'
import { communityAll } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, communityId: undefined, category: undefined, name: '', status: undefined })
const communities = ref([])
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编号', trigger: 'blur' }]
}
const checkDialog = ref(false); const checkForm = reactive({})
const checksDrawer = ref(false); const checks = ref([])

async function load() {
  loading.value = true
  try { const d = await equipmentPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, communityId: undefined, category: undefined, name: '', status: undefined }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { category: 1, onlineStatus: 1, status: 1 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await equipmentUpdate(form.id, form); else await equipmentCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
function openCheck(row) { checkForm.id = row.id; checkForm.result = 1; checkForm.issueDesc = ''; checkForm.nextCheckDate = ''; checkDialog.value = true }
async function submitCheck() {
  if (checkForm.result === 2 && !checkForm.issueDesc) return ElMessage.warning('请填写异常描述')
  await equipmentCheck(checkForm.id, { ...checkForm }); ElMessage.success('巡检已提交'); checkDialog.value = false; load()
}
async function openChecks(row) { checks.value = (await equipmentChecks(row.id, { size: 50 })).list || []; checksDrawer.value = true }

onMounted(async () => { communities.value = await communityAll(); load() })
</script>
