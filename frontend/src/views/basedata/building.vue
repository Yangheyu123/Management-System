<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">楼栋管理</h2><div class="page-sub">维护楼栋信息</div></div></div>
    <div class="toolbar">
      <el-select v-model="query.communityId" placeholder="所属小区" clearable @change="load" style="width:180px">
        <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-input v-model="query.name" placeholder="楼栋名称" clearable @keyup.enter="load" />
      <el-input v-model="query.buildingNo" placeholder="楼栋编号" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'basedata:building:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="communityName" label="所属小区" width="140" />
        <el-table-column prop="name" label="楼栋名称" width="100" />
        <el-table-column prop="buildingNo" label="编号" width="90" />
        <el-table-column prop="floors" label="层数" width="70" />
        <el-table-column prop="units" label="单元" width="70" />
        <el-table-column prop="elevators" label="电梯" width="70" />
        <el-table-column prop="structureType" label="结构" width="90" />
        <el-table-column prop="houseCount" label="房屋数" width="80" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'basedata:building:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="danger" v-permission="'basedata:building:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑楼栋' : '新增楼栋'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属小区" prop="communityId">
          <el-select v-model="form.communityId" style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
        </el-form-item>
        <el-form-item label="楼栋名称" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="楼栋编号" prop="buildingNo"><el-input v-model="form.buildingNo" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="层数"><el-input-number v-model="form.floors" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单元"><el-input-number v-model="form.units" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="电梯"><el-input-number v-model="form.elevators" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="结构类型"><el-input v-model="form.structureType" placeholder="如 框架/砖混" /></el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { buildingPage, buildingCreate, buildingUpdate, buildingDelete, communityAll } from '@/api/basedata'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, communityId: undefined, name: '', buildingNo: '' })
const communities = ref([])
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  name: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  buildingNo: [{ required: true, message: '请输入楼栋编号', trigger: 'blur' }]
}
async function load() {
  loading.value = true
  try { const d = await buildingPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, communityId: undefined, name: '', buildingNo: '' }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { floors: 18, units: 2, elevators: 2 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await buildingUpdate(form.id, form); else await buildingCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除楼栋「${row.name}」？`, '提示', { type: 'warning' })
  await buildingDelete(row.id); ElMessage.success('删除成功'); load()
}
onMounted(async () => { communities.value = await communityAll(); load() })
</script>
