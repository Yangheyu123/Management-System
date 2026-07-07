import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'

// 静态路由：meta.perm 为访问所需权限码（菜单/按钮码的 list）
export const asyncRoutes = [
  { path: '/dashboard', component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '仪表盘', icon: 'Odometer', perm: 'stat:dashboard:view' } },

  { path: '/basedata', redirect: '/basedata/community', meta: { title: '基础数据', icon: 'OfficeBuilding' },
    children: [
      { path: 'community', component: () => import('@/views/basedata/community.vue'), meta: { title: '小区管理', perm: 'basedata:community:list' } },
      { path: 'building', component: () => import('@/views/basedata/building.vue'), meta: { title: '楼栋管理', perm: 'basedata:building:list' } },
      { path: 'house', component: () => import('@/views/basedata/house.vue'), meta: { title: '房屋管理', perm: 'basedata:house:list' } },
      { path: 'owner', component: () => import('@/views/basedata/owner.vue'), meta: { title: '业主管理', perm: 'basedata:owner:list' } }
    ]
  },

  { path: '/business', redirect: '/business/workorder', meta: { title: '业务管理', icon: 'Briefcase' },
    children: [
      { path: 'workorder', component: () => import('@/views/business/workorder.vue'), meta: { title: '工单管理', perm: 'business:workorder:list' } },
      { path: 'bill', component: () => import('@/views/business/bill.vue'), meta: { title: '费用管理', perm: 'business:bill:list' } },
      { path: 'parking', component: () => import('@/views/business/parking.vue'), meta: { title: '车位管理', perm: 'business:parking:list' } },
      { path: 'equipment', component: () => import('@/views/business/equipment.vue'), meta: { title: '设备管理', perm: 'business:equipment:list' } }
    ]
  },

  { path: '/stat', redirect: '/stat/charge', meta: { title: '数据统计', icon: 'TrendCharts' },
    children: [
      { path: 'charge', component: () => import('@/views/stat/charge.vue'), meta: { title: '收费报表', perm: 'stat:charge:view' } },
      { path: 'workorder', component: () => import('@/views/stat/workorder.vue'), meta: { title: '工单报表', perm: 'stat:workorder:view' } },
      { path: 'parking', component: () => import('@/views/stat/parking.vue'), meta: { title: '车位报表', perm: 'stat:parking:view' } },
      { path: 'equipment', component: () => import('@/views/stat/equipment.vue'), meta: { title: '设备报表', perm: 'stat:equipment:view' } }
    ]
  },

  { path: '/system', redirect: '/system/user', meta: { title: '系统管理', icon: 'Setting' },
    children: [
      { path: 'user', component: () => import('@/views/system/user.vue'), meta: { title: '用户管理', perm: 'system:user:list' } },
      { path: 'role', component: () => import('@/views/system/role.vue'), meta: { title: '角色管理', perm: 'system:role:list' } },
      { path: 'permission', component: () => import('@/views/system/permission.vue'), meta: { title: '权限管理', perm: 'system:perm:list' } }
    ]
  },

  { path: '/profile', component: () => import('@/views/profile/index.vue'), meta: { title: '个人中心', hideInMenu: true } }
]

const routes = [
  { path: '/login', component: () => import('@/views/login/index.vue'), meta: { title: '登录', public: true } },
  // 员工端（管理后台）
  { path: '/', component: () => import('@/layout/index.vue'),
    children: asyncRoutes },
  // 业主端（独立布局）
  { path: '/portal', component: () => import('@/views/portal/layout.vue'),
    children: [
      { path: 'overview', component: () => import('@/views/portal/overview.vue'), meta: { title: '业主首页' } },
      { path: 'workorders', component: () => import('@/views/portal/workorders.vue'), meta: { title: '我的报修' } },
      { path: 'bills', component: () => import('@/views/portal/bills.vue'), meta: { title: '我的账单' } },
      { path: 'houses', component: () => import('@/views/portal/houses.vue'), meta: { title: '我的房屋' } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const WHITE_LIST = ['/login']
router.beforeEach(async (to, from, next) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '小区物业管理系统'
  const hasToken = getToken()
  if (!hasToken) {
    if (WHITE_LIST.includes(to.path)) return next()
    return next('/login')
  }
  if (to.path === '/login') return next('/dashboard')

  const store = useUserStore()
  if (!store.userInfo.id) {
    try {
      await store.fetchUserInfo()
    } catch (e) {
      store.reset()
      return next('/login')
    }
  }
  // 业主身份判断：userType===2 或 roles 含 OWNER
  const isOwner = store.userInfo.userType === 2 || (store.userInfo.roles || []).includes('OWNER')
  // 业主只能进 /portal，员工只能进员工端
  if (isOwner && !to.path.startsWith('/portal')) {
    return next('/portal/overview')
  }
  if (!isOwner && to.path.startsWith('/portal')) {
    return next('/dashboard')
  }
  // 员工端权限校验
  if (!isOwner && to.meta.perm && !store.hasPermission(to.meta.perm)) {
    return next('/dashboard')
  }
  next()
})

export default router
