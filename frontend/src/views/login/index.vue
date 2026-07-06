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

/* 左侧：深咖啡棕 → 琥珀金暖渐变 */
.login-left {
  background: linear-gradient(150deg, #2b2420 0%, #4a3a1f 50%, #946906 125%);
  color: #fffdf8; padding: 56px 64px;
  display: flex; flex-direction: column; justify-content: center;
  position: relative; overflow: hidden;
  animation: fadeInLeft .6s cubic-bezier(.4,0,.2,1);
}
/* 左侧装饰：柔和光晕 */
.login-left::before {
  content: ''; position: absolute; width: 420px; height: 420px;
  right: -160px; top: -120px; border-radius: 50%;
  background: radial-gradient(circle, rgba(253, 243, 215, .12), transparent 70%);
  pointer-events: none;
}
.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 56px; }
.brand-mark {
  width: 42px; height: 42px; border-radius: 11px;
  background: #fffdf8; color: #946906;
  font-weight: 800; font-size: 21px; display: grid; place-items: center;
  box-shadow: 0 4px 14px rgba(0,0,0,.18);
}
.brand-name { font-size: 16px; font-weight: 600; letter-spacing: .5px; }
.hero-title {
  font-size: 34px; line-height: 1.3; font-weight: 700; margin: 0 0 18px;
  letter-spacing: -.5px;
}
.hero-sub {
  font-size: 15px; line-height: 1.7; color: #e6d9c4; max-width: 460px; margin: 0 0 30px;
}
.hero-points { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 13px; }
.hero-points li {
  display: flex; align-items: center; gap: 10px; color: #f0e4d0; font-size: 14px;
  animation: fadeInUp .5s cubic-bezier(.4,0,.2,1) backwards;
}
.hero-points li:nth-child(1) { animation-delay: .2s; }
.hero-points li:nth-child(2) { animation-delay: .32s; }
.hero-points li:nth-child(3) { animation-delay: .44s; }
.hero-points .el-icon { color: #f0c75e; font-size: 17px; }

/* 右侧：奶油底 */
.login-right {
  display: grid; place-items: center; background: var(--ink-50); padding: 24px;
  animation: fadeInRight .6s cubic-bezier(.4,0,.2,1);
}
.login-card {
  width: 100%; max-width: 380px;
  background: var(--paper, #fffdf8);
  border: 1px solid var(--ink-200); border-radius: 18px;
  padding: 40px 36px;
  box-shadow: 0 12px 40px rgba(74, 63, 54, .08);
}
.login-title { font-size: 25px; font-weight: 700; margin: 0; color: var(--ink-900); letter-spacing: -.3px; }
.login-sub { font-size: 14px; color: var(--ink-500); margin: 6px 0 30px; }
.login-btn {
  width: 100%; height: 46px; font-size: 15px; font-weight: 600; margin-top: 6px;
  letter-spacing: 2px;
}
.demo-tip {
  margin-top: 22px; display: flex; align-items: center; gap: 6px;
  font-size: 12px; color: var(--ink-500); background: var(--brand-50);
  padding: 11px 13px; border-radius: var(--radius);
  border: 1px solid rgba(184, 134, 11, .15);
}
.demo-tip .el-icon { color: var(--brand-500); }

/* 入场动画 */
@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-24px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(24px); }
  to   { opacity: 1; transform: translateX(0); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .login-wrap { grid-template-columns: 1fr; }
  .login-left { display: none; }
}
</style>
