<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">权限管理</h2><div class="page-sub">菜单与按钮权限维护</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.permName" placeholder="名称" clearable @keyup.enter="load" />
      <el-select v-model="query.type" placeholder="类型" clearable><el-option label="菜单" :value="1" /><el-option label="按钮" :value="2" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'system:perm:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="treeData" v-loading="loading" row-key="id" border :tree-props="{ children: 'children' }" default-expand-all>
        <el-table-column prop="permName" label="名称" min-width="180" />
        <el-table-column prop="permCode" label="权限码" min-width="180" />
        <el-table-column label="类型" width="90"><template #default="{ row }"><el-tag size="small" :type="row.type===1?'':'info'">{{ row.type===1?'菜单':'按钮' }}</el-tag></template></el-table-column>
        <el-table-column prop="path" label="路径" min-width="140" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'system:perm:add'" @click="open(null, row.id)">新增子级</el-button>
            <el-button link type="primary" v-permission="'system:perm:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="danger" v-permission="'system:perm:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑权限' : '新增权限'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="父级"><el-select v-model="form.parentId" style="width:100%"><el-option label="根节点" :value="0" /><el-option v-for="p in flatList" :key="p.id" :label="p.permName" :value="p.id" /></el-select></el-form-item>
        <el-form-item label="名称" prop="permName"><el-input v-model="form.permName" /></el-form-item>
        <el-form-item label="权限码" prop="permCode"><el-input v-model="form.permCode" placeholder="模块:子模块:动作" /></el-form-item>
        <el-form-item label="类型" prop="type"><el-radio-group v-model="form.type"><el-radio :label="1">菜单</el-radio><el-radio :label="2">按钮</el-radio></el-radio-group></el-form-item>
        <el-form-item label="路径" v-if="form.type===1"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="图标" v-if="form.type===1"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" controls-position="right" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { permTree, permCreate, permUpdate, permDelete } from '@/api/system'

const loading = ref(false); const treeData = ref([]); const flatList = ref([])
const query = reactive({ permName: '', type: undefined })
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  permName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  permCode: [{ required: true, message: '请输入权限码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function flatten(nodes, out) { for (const n of nodes) { out.push(n); if (n.children) flatten(n.children, out) } }
async function load() {
  loading.value = true
  try {
    const data = await permTree()
    treeData.value = data
    const flat = []; flatten(data, flat)
    flatList.value = flat.filter(f => f.type === 1)
  } finally { loading.value = false }
}
function reset() { query.permName = ''; query.type = undefined; load() }
function open(row, parentId) {
  Object.keys(form).forEach(k => delete form[k])
  if (row) Object.assign(form, row)
  else Object.assign(form, { type: 1, parentId: parentId || 0, sort: 0 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await permUpdate(form.id, form); else await permCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除权限「${row.permName}」？`, '提示', { type: 'warning' })
  await permDelete(row.id); ElMessage.success('删除成功'); load()
}
load()
</script>
