<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">角色管理</h2><div class="page-sub">角色与权限分配（RBAC）</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.roleName" placeholder="角色名" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'system:role:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="roleName" label="角色名" width="140" />
        <el-table-column prop="roleCode" label="编码" width="170" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="userCount" label="用户数" width="80" />
        <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="tagType('commonStatus', row.status)">{{ dictName('commonStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'system:role:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="primary" v-permission="'system:role:assign'" @click="openPerms(row)">权限</el-button>
            <el-button link type="danger" v-permission="'system:role:delete'" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="form.id ? '编辑角色' : '新增角色'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名" prop="roleName"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="编码" prop="roleCode"><el-input v-model="form.roleCode" :disabled="!!form.id" placeholder="大写+下划线" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('commonStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="permDialog" title="分配权限" width="480px">
      <el-tree ref="treeRef" :data="permTree" node-key="id" :props="{ label: 'permName', children: 'children' }" show-checkbox default-expand-all />
      <template #footer><el-button @click="permDialog = false">取消</el-button><el-button type="primary" @click="submitPerms">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleList, roleCreate, roleUpdate, roleDelete, rolePermissions, roleAssignPermissions, permTree } from '@/api/system'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([])
const query = reactive({ roleName: '' })
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入编码', trigger: 'blur' }]
}
const permDialog = ref(false); const permTreeData = ref([]); const treeRef = ref(); const currentRoleId = ref(null)

async function load() {
  loading.value = true
  try { list.value = await roleList(query) } finally { loading.value = false }
}
function reset() { query.roleName = ''; load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { status: 1 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await roleUpdate(form.id, form); else await roleCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await roleDelete(row.id); ElMessage.success('删除成功'); load()
}
async function openPerms(row) {
  currentRoleId.value = row.id
  if (!permTreeData.value.length) permTreeData.value = await permTree()
  const d = await rolePermissions(row.id)
  permDialog.value = true
  await nextTick()
  treeRef.value.setCheckedKeys(d.permissionIds || [])
}
async function submitPerms() {
  const ids = treeRef.value.getCheckedKeys()
  await roleAssignPermissions(currentRoleId.value, { permissionIds: ids })
  ElMessage.success('分配成功'); permDialog.value = false
}

onMounted(load)
</script>
