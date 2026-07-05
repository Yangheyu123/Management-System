<template>
  <div class="login-wrap">
    <div class="login-left">
      <div class="brand">
        <div class="brand-mark">物</div>
        <div class="brand-name">悦居物业 · 管理后台</div>
      </div>
      <h1 class="hero-title">把小区的人、房、车、钱、事，<br />搬进一个安静的驾驶舱。</h1>
      <p class="hero-sub">报修工单、收费账单、车位与设备，全链路线上化、可统计、可追溯。</p>
      <ul class="hero-points">
        <li><el-icon><CircleCheckFilled /></el-icon> 工单状态机，从派单到评价全程留痕</li>
        <li><el-icon><CircleCheckFilled /></el-icon> 账单批量生成、部分缴费、欠费一目了然</li>
        <li><el-icon><CircleCheckFilled /></el-icon> 设备巡检到期提醒，安防在线率实时可见</li>
      </ul>
    </div>

    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-sub">请使用账号密码登录</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="onSubmit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码"
                      :prefix-icon="Lock" @keyup.enter="onSubmit" />
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="onSubmit">登 录</el-button>
        </el-form>
        <div class="demo-tip">
          <el-icon><InfoFilled /></el-icon>
          演示账号：admin / admin123（经理 manager01 / 123456）
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, InfoFilled, CircleCheckFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await store.login({ ...form })
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (e) {
      /* 拦截器已提示 */
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh; display: grid; grid-template-columns: 1.1fr 1fr;
}
.login-left {
  background: linear-gradient(150deg, #0f172a 0%, #1e293b 55%, #2459d6 130%);
  color: #fff; padding: 56px 64px; display: flex; flex-direction: column; justify-content: center;
}
.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 56px; }
.brand-mark {
  width: 40px; height: 40px; border-radius: 10px; background: #fff; color: #2459d6;
  font-weight: 800; font-size: 20px; display: grid; place-items: center;
}
.brand-name { font-size: 16px; font-weight: 600; letter-spacing: .5px; }
.hero-title { font-size: 34px; line-height: 1.25; font-weight: 700; margin: 0 0 18px; letter-spacing: -.5px; }
.hero-sub { font-size: 15px; line-height: 1.7; color: #cbd5e1; max-width: 460px; margin: 0 0 28px; }
.hero-points { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 12px; }
.hero-points li { display: flex; align-items: center; gap: 10px; color: #e2e8f0; font-size: 14px; }
.hero-points .el-icon { color: #6ee7b7; font-size: 17px; }

.login-right { display: grid; place-items: center; background: var(--ink-50); padding: 24px; }
.login-card { width: 100%; max-width: 380px; }
.login-title { font-size: 24px; font-weight: 700; margin: 0; color: var(--ink-900); }
.login-sub { font-size: 14px; color: var(--ink-500); margin: 6px 0 28px; }
.login-btn { width: 100%; height: 44px; font-size: 15px; font-weight: 600; margin-top: 6px; }
.demo-tip {
  margin-top: 20px; display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: var(--ink-500); background: var(--ink-100);
  padding: 10px 12px; border-radius: var(--radius);
}

@media (max-width: 900px) {
  .login-wrap { grid-template-columns: 1fr; }
  .login-left { display: none; }
}
</style>
