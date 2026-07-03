<template>
  <div class="page">
    <div class="page-header">
      <div><h2 class="page-title">小区管理</h2><div class="page-sub">维护小区基本信息</div></div>
    </div>

    <div class="toolbar">
      <el-input v-model="query.name" placeholder="小区名称" clearable @keyup.enter="load" />
      <el-input v-model="query.address" placeholder="地址" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option v-for="o in dictOptions('commonStatus')" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'basedata:community:add'" @click="open()">新增</el-button>
    </div>

    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="小区名称" min-width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="buildYear" label="建成年份" width="90" />
        <el-table-column prop="developer" label="开发商" width="120" />
        <el-table-column prop="totalBuildings" label="楼栋数" width="80" />
        <el-table-column prop="totalHouses" label="房屋数" width="80" />
        <el-table-column prop="contactName" label="联系人" width="90" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="tagType('commonStatus', row.status)">{{ dictName('commonStatus', row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'basedata:community:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="danger" v-permission="'basedata:community:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     @current-change="load" @size-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑小区' : '新增小区'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="小区名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="开发商"><el-input v-model="form.developer" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="建成年份"><el-input-number v-model="form.buildYear" :min="1980" :max="2030" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="占地面积"><el-input-number v-model="form.area" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="绿化率(%)"><el-input-number v-model="form.greenRate" :min="0" :max="100" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼栋总数"><el-input-number v-model="form.totalBuildings" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="房屋总数"><el-input-number v-model="form.totalHouses" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('commonStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item></el-col>
        </el-row>
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
import { communityPage, communityCreate, communityUpdate, communityDelete } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, name: '', address: '', status: undefined })
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = { name: [{ required: true, message: '请输入小区名称', trigger: 'blur' }] }

async function load() {
  loading.value = true
  try {
    const d = await communityPage(query)
    list.value = d.list; total.value = d.total
  } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, name: '', address: '', status: undefined }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  if (row) Object.assign(form, row, { status: row.status ?? 1 })
  else Object.assign(form, { status: 1 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) await communityUpdate(form.id, form); else await communityCreate(form)
      ElMessage.success('保存成功'); dialog.value = false; load()
    } catch (e) {} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除小区「${row.name}」？`, '提示', { type: 'warning' })
  await communityDelete(row.id); ElMessage.success('删除成功'); load()
}
load()
</script>
