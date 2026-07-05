<template>
  <div class="page">
    <div class="page-header"><h2 class="page-title">个人中心</h2></div>
    <div class="profile-card">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="账号">{{ store.userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ store.userInfo.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ store.userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ store.userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属小区">{{ store.userInfo.communityName || '全部' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ store.userInfo.userTypeName }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="r in (store.userInfo.roleNames || store.roles)" :key="r" style="margin-right:6px">{{ r }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>
<script setup>
import { useUserStore } from '@/stores/user'
const store = useUserStore()
</script>
<style scoped>
.profile-card { background:#fff; border:1px solid var(--ink-200); border-radius: var(--radius-lg); padding: 20px 24px; }
</style>
