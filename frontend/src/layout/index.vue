<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <div class="logo-mark">物</div>
        <span v-show="!collapsed" class="logo-text">悦居物业</span>
      </div>
      <el-menu :default-active="$route.path" :collapse="collapsed" :collapse-transition="false"
               router unique-opened background-color="#2b2420" text-color="#c9bca9" active-text-color="#fdf3d7">
        <template v-for="r in menuRoutes" :key="r.path">
          <!-- 一级菜单（无子） -->
          <el-menu-item v-if="!r.children || r.children.length === 0" :index="r.path">
            <el-icon><component :is="r.meta.icon" /></el-icon>
            <template #title>{{ r.meta.title }}</template>
          </el-menu-item>
          <!-- 分组 -->
          <el-sub-menu v-else :index="r.path">
            <template #title>
              <el-icon><component :is="r.meta.icon" /></el-icon>
              <span>{{ r.meta.title }}</span>
            </template>
            <el-menu-item v-for="c in visibleChildren(r)" :key="r.path + '/' + c.path"
                          :index="r.path + '/' + c.path">
              {{ c.meta.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="onCommand">
            <span class="user">
              <el-avatar :size="30" class="avatar">{{ (store.userInfo.realName || 'U').charAt(0) }}</el-avatar>
              <span class="uname">{{ store.userInfo.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="route-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码 -->
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
  </el-container>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { asyncRoutes } from '@/router'
import { changePassword } from '@/api/auth'

const router = useRouter()
const store = useUserStore()
const collapsed = ref(false)

const menuRoutes = computed(() => asyncRoutes.filter(r => !r.meta?.hideInMenu))
function visibleChildren(r) {
  return (r.children || []).filter(c => !c.meta?.hideInMenu && (!c.meta?.perm || store.hasPermission(c.meta.perm)))
}

// 修改密码
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdRef = ref()
const pwd = reactive({ oldPassword: '', newPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' },
                { min: 6, max: 20, message: '长度 6-20 位', trigger: 'blur' }]
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
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }

/* 侧边栏：暖色深咖啡棕 */
.aside {
  background: #2b2420;
  transition: width .28s var(--ease, cubic-bezier(.4,0,.2,1));
  overflow-x: hidden;
  box-shadow: 2px 0 12px rgba(43, 36, 32, .15);
}
.logo {
  height: 56px; display: flex; align-items: center; gap: 11px; padding: 0 18px;
  color: #fffdf8; border-bottom: 1px solid rgba(255, 253, 248, .06);
}
.logo-mark {
  width: 32px; height: 32px; border-radius: 9px;
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600));
  color: #fffdf8; font-weight: 800; font-size: 15px;
  display: grid; place-items: center; flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(184, 134, 11, .35);
}
.logo-text { font-size: 16px; font-weight: 600; white-space: nowrap; letter-spacing: .3px; }
.aside :deep(.el-menu) { border-right: none; background: transparent !important; }

/* 菜单项：hover 微亮 + 激活态左侧琥珀金指示条 */
.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  color: #c9bca9 !important;
  transition: all .2s var(--ease, cubic-bezier(.4,0,.2,1));
  position: relative;
}
.aside :deep(.el-menu-item:hover),
.aside :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 253, 248, .04) !important;
  color: #fffdf8 !important;
}
.aside :deep(.el-menu-item.is-active) {
  color: #fdf3d7 !important;
  background: rgba(184, 134, 11, .14) !important;
}
/* 激活态左侧指示条 */
.aside :deep(.el-menu-item.is-active::before) {
  content: ''; position: absolute; left: 0; top: 8px; bottom: 8px; width: 3px;
  background: var(--brand-500); border-radius: 0 3px 3px 0;
  animation: indicatorIn .25s var(--ease, cubic-bezier(.4,0,.2,1));
}
@keyframes indicatorIn {
  from { transform: scaleY(0); }
  to   { transform: scaleY(1); }
}
.aside :deep(.el-sub-menu .el-menu-item) { background-color: rgba(0,0,0,.15) !important; }

/* 顶部 header */
.header {
  background: var(--paper, #fffdf8); border-bottom: 1px solid var(--ink-200);
  display: flex; align-items: center; justify-content: space-between; height: 56px;
  padding: 0 22px;
}
.header-left { display: flex; align-items: center; gap: 14px; }
.collapse-btn {
  font-size: 19px; cursor: pointer; color: var(--ink-500);
  transition: color .2s, transform .2s;
}
.collapse-btn:hover { color: var(--brand-500); transform: scale(1.1); }
.header-right .user {
  display: flex; align-items: center; gap: 8px; cursor: pointer; color: var(--ink-700);
  padding: 6px 10px; border-radius: 20px;
  transition: background .2s;
}
.header-right .user:hover { background: var(--brand-50); }
.avatar {
  background: linear-gradient(135deg, var(--brand-500), var(--brand-600)) !important;
  color: #fffdf8 !important; font-weight: 600;
  box-shadow: 0 2px 6px rgba(184, 134, 11, .3);
}
.uname { font-size: 14px; }

/* 内容区 */
.main {
  background: var(--ink-50); padding: 0; overflow-y: auto;
}

/* 路由切换：淡入 + 上移 */
.route-slide-enter-active,
.route-slide-leave-active {
  transition: opacity .28s var(--ease, cubic-bezier(.4,0,.2,1)),
              transform .28s var(--ease, cubic-bezier(.4,0,.2,1));
}
.route-slide-enter-from { opacity: 0; transform: translateY(10px); }
.route-slide-leave-to   { opacity: 0; transform: translateY(-6px); }
</style>
