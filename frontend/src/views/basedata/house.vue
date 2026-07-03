<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">房屋管理</h2><div class="page-sub">维护房屋及入住信息</div></div></div>
    <div class="toolbar">
      <el-select v-model="query.communityId" placeholder="小区" clearable @change="onCommunity" style="width:150px">
        <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="query.houseNo" placeholder="房号" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option v-for="o in dictOptions('houseStatus')" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.hasOwner" placeholder="已绑业主" clearable>
        <el-option label="是" :value="1" /><el-option label="否" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'basedata:house:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="communityName" label="小区" width="130" />
        <el-table-column prop="buildingName" label="楼栋" width="90" />
        <el-table-column prop="houseNo" label="房号" min-width="130" />
        <el-table-column prop="floorNo" label="楼层" width="70" />
        <el-table-column prop="area" label="面积(㎡)" width="90" />
        <el-table-column prop="layout" label="户型" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="tagType('commonStatus', row.status===3?1:0)" effect="plain">{{ row.statusName || dictName('houseStatus', row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ownerNames" label="业主" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'basedata:house:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="danger" v-permission="'basedata:house:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑房屋' : '新增房屋'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="小区" prop="communityId">
          <el-select v-model="form.communityId" @change="onFormCommunity" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" style="width:100%"><el-option v-for="b in formBuildings" :key="b.id" :label="b.name" :value="b.id" /></el-select>
        </el-form-item>
        <el-form-item label="房号" prop="houseNo"><el-input v-model="form.houseNo" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="单元"><el-input v-model="form.unitNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="楼层"><el-input-number v-model="form.floorNo" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="面积"><el-input-number v-model="form.area" :min="0" :precision="2" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="户型"><el-input v-model="form.layout" placeholder="如 三室两厅" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('houseStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { housePage, houseCreate, houseUpdate, houseDelete, communityAll } from '@/api/basedata'
import { buildingPage } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, communityId: undefined, houseNo: '', status: undefined, hasOwner: undefined })
const communities = ref([]); const formBuildings = ref([])
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  houseNo: [{ required: true, message: '请输入房号', trigger: 'blur' }]
}
async function load() {
  loading.value = true
  try { const d = await housePage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function onCommunity() { query.page = 1; load() }
function reset() { Object.assign(query, { page: 1, communityId: undefined, houseNo: '', status: undefined, hasOwner: undefined }); load() }
async function onFormCommunity(cid) {
  form.buildingId = undefined
  formBuildings.value = cid ? (await buildingPage({ communityId: cid, size: 100 })).list : []
}
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { status: 1 })
  if (form.communityId) onFormCommunity(form.communityId)
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await houseUpdate(form.id, form); else await houseCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除房屋「${row.houseNo}」？`, '提示', { type: 'warning' })
  await houseDelete(row.id); ElMessage.success('删除成功'); load()
}
onMounted(async () => { communities.value = await communityAll(); load() })
</script>
