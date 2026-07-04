<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">业主管理</h2><div class="page-sub">维护业主信息（身份证已脱敏）</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="姓名" clearable @keyup.enter="load" />
      <el-input v-model="query.phone" placeholder="手机号" clearable @keyup.enter="load" />
      <el-input v-model="query.plateNo" placeholder="车牌" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option v-for="o in dictOptions('ownerStatus')" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'basedata:owner:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idCard" label="身份证号" min-width="170" />
        <el-table-column prop="plateNo" label="车牌" width="100" />
        <el-table-column prop="moveInDate" label="入住日期" width="110" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status===1?'success':'info'">{{ dictName('ownerStatus', row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="houseCount" label="房屋数" width="80" />
        <el-table-column prop="houseNames" label="名下房屋" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'basedata:owner:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="danger" v-permission="'basedata:owner:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑业主' : '新增业主'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="form.idCard" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="车牌号"><el-input v-model="form.plateNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.gender" style="width:100%"><el-option v-for="o in dictOptions('gender')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入住日期"><el-date-picker v-model="form.moveInDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('ownerStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
          </el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ownerPage, ownerCreate, ownerUpdate, ownerDelete } from '@/api/basedata'
import { dictName, dictOptions } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, name: '', phone: '', plateNo: '', status: undefined })
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}
async function load() {
  loading.value = true
  try { const d = await ownerPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, name: '', phone: '', plateNo: '', status: undefined }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { status: 1, gender: 0 })
  // 编辑时回填原始身份证（详情接口返回 idCardRaw）
  if (row && row.idCardRaw) form.idCard = row.idCardRaw
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await ownerUpdate(form.id, form); else await ownerCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除业主「${row.name}」？`, '提示', { type: 'warning' })
  await ownerDelete(row.id); ElMessage.success('删除成功'); load()
}
load()
</script>
