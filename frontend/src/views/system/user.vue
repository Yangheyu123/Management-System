<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">用户管理</h2><div class="page-sub">物业员工/业主账号、角色分配</div></div></div>
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="账号" clearable @keyup.enter="load" />
      <el-input v-model="query.realName" placeholder="姓名" clearable @keyup.enter="load" />
      <el-input v-model="query.phone" placeholder="手机号" clearable @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable><el-option v-for="o in dictOptions('commonStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="primary" plain v-permission="'system:user:add'" @click="open()">新增</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="userTypeName" label="类型" width="80" />
        <el-table-column prop="communityName" label="所属小区" width="130" />
        <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="tagType('commonStatus', row.status)">{{ dictName('commonStatus', row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="角色" min-width="120"><template #default="{ row }"><el-tag v-for="r in (row.roleNames||[])" :key="r" size="small" style="margin-right:4px">{{ r }}</el-tag></template></el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="'system:user:edit'" @click="open(row)">编辑</el-button>
            <el-button link type="primary" v-permission="'system:user:assign'" @click="openRoles(row)">角色</el-button>
            <el-button link type="warning" v-permission="'system:user:reset'" @click="resetPwd(row)">重置密码</el-button>
            <el-button link type="danger" v-permission="'system:user:delete'" @click="remove(row)" :disabled="row.id===1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" @current-change="load" @size-change="load" />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑用户' : '新增用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="账号" prop="username"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item></el-col>
          <el-col :span="12" v-if="!form.id"><el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别"><el-select v-model="form.gender" style="width:100%"><el-option v-for="o in dictOptions('gender')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用户类型"><el-select v-model="form.userType" style="width:100%"><el-option v-for="o in dictOptions('userType')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所属小区"><el-select v-model="form.communityId" clearable style="width:100%"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="o in dictOptions('commonStatus')" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" :loading="saving" @click="submit">保存</el-button></template>
    </el-dialog>

    <!-- 分配角色 -->
    <el-dialog v-model="roleDialog" title="分配角色" width="420px">
      <el-checkbox-group v-model="roleForm.roleIds">
        <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id">{{ r.roleName }}</el-checkbox>
      </el-checkbox-group>
      <template #footer><el-button @click="roleDialog = false">取消</el-button><el-button type="primary" @click="submitRoles">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userPage, userCreate, userUpdate, userDelete, userResetPwd, userRoles, userAssignRoles, roleList } from '@/api/system'
import { communityAll } from '@/api/basedata'
import { dictName, dictOptions, tagType } from '@/utils/dict'

const loading = ref(false); const list = ref([]); const total = ref(0)
const query = reactive({ page: 1, size: 10, username: '', realName: '', phone: '', status: undefined })
const communities = ref([]); const allRoles = ref([])
const dialog = ref(false); const saving = ref(false); const formRef = ref()
const form = reactive({})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}
const roleDialog = ref(false); const roleForm = reactive({})

async function load() {
  loading.value = true
  try { const d = await userPage(query); list.value = d.list; total.value = d.total } finally { loading.value = false }
}
function reset() { Object.assign(query, { page: 1, username: '', realName: '', phone: '', status: undefined }); load() }
function open(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row || { userType: 1, status: 1, gender: 0 })
  dialog.value = true
}
async function submit() {
  await formRef.value.validate(async (v) => {
    if (!v) return; saving.value = true
    try { if (form.id) await userUpdate(form.id, form); else await userCreate(form); ElMessage.success('保存成功'); dialog.value = false; load() } catch(e){} finally { saving.value = false }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await userDelete(row.id); ElMessage.success('删除成功'); load()
}
async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt('请输入新密码（6-20位）', '重置密码', { inputPattern: /^.{6,20}$/, inputErrorMessage: '长度需 6-20 位' })
  await userResetPwd(row.id, { newPassword: value }); ElMessage.success('密码已重置')
}
async function openRoles(row) {
  const d = await userRoles(row.id)
  roleForm.userId = row.id; roleForm.roleIds = d.roleIds || []
  roleDialog.value = true
}
async function submitRoles() {
  await userAssignRoles(roleForm.userId, { roleIds: roleForm.roleIds }); ElMessage.success('分配成功'); roleDialog.value = false; load()
}

onMounted(async () => {
  communities.value = await communityAll()
  allRoles.value = await roleList({})
  load()
})
</script>
