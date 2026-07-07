<template>
  <div class="portal-layout">
    <!-- 顶部导航 -->
    <header class="portal-header">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/portal/overview')">
          <div class="brand-mark">悦</div>
          <span class="brand-name">悦居物业 · 业主中心</span>
        </div>
        <nav class="nav-tabs">
          <router-link v-for="t in tabs" :key="t.path" :to="t.path" class="nav-tab"
                       :class="{ active: $route.path === t.path }">
            <el-icon><component :is="t.icon" /></el-icon>
            <span>{{ t.title }}</span>
          </router-link>
        </nav>
        <div class="header-right">
          <el-dropdown @command="onCommand">
            <span class="user">
              <el-avatar :size="32" class="avatar">{{ (store.userInfo.realName || 'U').charAt(0) }}</el-avatar>
              <span class="uname">{{ store.userInfo.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="portal-main">
      <router-view v-slot="{ Component }">
        <transition name="route-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 修改密码（复用） -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, HomeFilled, Tickets, Wallet, Document } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'

const router = useRouter()
const store = useUserStore()

const tabs = [
  { path: '/portal/overview', title: '首页', icon: HomeFilled },
  { path: '/portal/workorders', title: '我的报修', icon: Tickets },
  { path: '/portal/bills', title: '我的账单', icon: Wallet },
  { path: '/portal/houses', title: '我的房屋', icon: Document },
]

const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdRef = ref()
const pwd = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '长度 6-20 位', trigger: 'blur' }]
}
function submitPwd() {
  pwdRef.value.validate(async (valid) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      await changePassword({ ...pwd })
      ElMessage.success('密码修改成功，请重新登录')
      pwdVisible.value = false
      await store.logout()
      router.push('/login')
    } catch (e) {} finally { pwdLoading.value = false }
  })
}
async function onCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await store.logout()
    router.push('/login')
  } else if (cmd === 'password') {
    pwd.oldPassword = ''; pwd.newPassword = ''
    pwdVisible.value = true
  }
}
</script>

<style scoped>
.portal-layout { min-height: 100vh; background: var(--ink-50); display: flex; flex-direction: column; }

/* 顶部导航 */
.portal-header {
  background: var(--paper, #fffdf8);
  border-bottom: 1px solid var(--ink-200);
  box-shadow: var(--shadow-sm);
  position: sticky; top: 0; z-index: 10;
}
.header-inner {
  max-width: 1180px; margin: 0 auto; height: 62px; padding: 0 24px;
  display: flex; align-items: center; justify-content: space-between; gap: 24px;
}
.brand { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.brand-mark {
  width: 34px; height: 34px; border-radius: 10px;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  color: #fffdf8; font-weight: 800; font-size: 16px;
  display: grid; place-items: center;
  box-shadow: 0 2px 8px rgba(184, 134, 11, .35);
}
.brand-name { font-size: 17px; font-weight: 700; color: var(--ink-900); letter-spacing: .3px; white-space: nowrap; }

/* Tab 导航 */
.nav-tabs { display: flex; gap: 4px; flex: 1; justify-content: center; }
.nav-tab {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 18px; border-radius: 20px;
  color: var(--ink-500); font-size: 14px; font-weight: 500;
  text-decoration: none; transition: all .2s var(--ease, cubic-bezier(.4,0,.2,1));
}
.nav-tab .el-icon { font-size: 16px; }
.nav-tab:hover { color: var(--brand-500); background: var(--brand-50); }
.nav-tab.active {
  color: #fffdf8;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  box-shadow: 0 2px 8px rgba(184, 134, 11, .3);
}

.header-right .user {
  display: flex; align-items: center; gap: 8px; cursor: pointer; color: var(--ink-700);
  padding: 6px 10px; border-radius: 20px; transition: background .2s;
}
.header-right .user:hover { background: var(--brand-50); }
.avatar {
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600)) !important;
  color: #fffdf8 !important; font-weight: 600;
}
.uname { font-size: 14px; }

/* 内容区 */
.portal-main { flex: 1; max-width: 1180px; width: 100%; margin: 0 auto; padding: 26px 24px; }

.route-slide-enter-active, .route-slide-leave-active {
  transition: opacity .28s var(--ease, cubic-bezier(.4,0,.2,1)), transform .28s var(--ease, cubic-bezier(.4,0,.2,1));
}
.route-slide-enter-from { opacity: 0; transform: translateY(10px); }
.route-slide-leave-to { opacity: 0; transform: translateY(-6px); }
</style>
