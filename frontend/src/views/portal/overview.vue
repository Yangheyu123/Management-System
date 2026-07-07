<template>
  <div class="overview">
    <div class="greeting">
      <h2>您好，{{ data.ownerName || '业主' }} 👋</h2>
      <p>欢迎回到悦居物业业主中心，这是您的最新动态</p>
    </div>

    <!-- KPI 卡片 -->
    <div class="kpi-grid">
      <div class="kpi-card" v-for="(c, i) in kpis" :key="c.label" :style="{ animationDelay: (i * 0.06) + 's' }">
        <div class="kpi-icon" :style="{ background: c.bg, color: c.color }">
          <el-icon><component :is="c.icon" /></el-icon>
        </div>
        <div class="kpi-body">
          <div class="kpi-label">{{ c.label }}</div>
          <div class="kpi-value">{{ c.value }}<span class="kpi-unit">{{ c.unit }}</span></div>
        </div>
      </div>
    </div>

    <!-- 我的房屋快捷 -->
    <div class="section">
      <div class="section-title">我的房屋</div>
      <div class="house-cards" v-if="data.houses && data.houses.length">
        <div class="house-card" v-for="h in data.houses" :key="h.houseId">
          <div class="house-no">{{ h.houseNo }}</div>
          <div class="house-meta">
            <span>{{ h.communityName }}</span> · <span>{{ h.buildingName }}</span>
          </div>
          <div class="house-tags">
            <el-tag size="small" effect="light">{{ h.area }}㎡</el-tag>
            <el-tag size="small" type="warning" effect="light" v-if="h.isPrimary">户主</el-tag>
            <el-tag size="small" effect="light" v-else>{{ h.relation }}</el-tag>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无关联房屋" :image-size="80" />
    </div>

    <!-- 快捷入口 -->
    <div class="section">
      <div class="section-title">快捷操作</div>
      <div class="quick-grid">
        <div class="quick-card" @click="$router.push('/portal/workorders')">
          <el-icon class="q-icon"><Tickets /></el-icon>
          <div class="q-text">
            <div class="q-title">提交报修</div>
            <div class="q-sub">在线报修，进度可查</div>
          </div>
        </div>
        <div class="quick-card" @click="$router.push('/portal/bills')">
          <el-icon class="q-icon"><Wallet /></el-icon>
          <div class="q-text">
            <div class="q-title">缴费</div>
            <div class="q-sub">待缴 {{ data.unpaidBillCount || 0 }} 笔</div>
          </div>
        </div>
        <div class="quick-card" @click="$router.push('/portal/houses')">
          <el-icon class="q-icon"><Document /></el-icon>
          <div class="q-text">
            <div class="q-title">房屋档案</div>
            <div class="q-sub">查看房屋详情</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { HomeFilled, Tickets, Wallet, Document } from '@element-plus/icons-vue'
import { portalOverview } from '@/api/portal'

const data = ref({})

const kpis = computed(() => [
  { label: '我的房屋', value: data.value.houseCount ?? 0, unit: '套', icon: HomeFilled, bg: '#fdf3d7', color: '#b8860b' },
  { label: '待缴账单', value: data.value.unpaidBillCount ?? 0, unit: '笔', icon: Wallet, bg: '#fde8e0', color: '#c45e3b' },
  { label: '欠费金额', value: money(data.value.unpaidAmount), unit: '元', icon: Wallet, bg: '#fde8e0', color: '#c45e3b' },
  { label: '报修记录', value: data.value.workorderTotal ?? 0, unit: '单', icon: Tickets, bg: '#e6f0e0', color: '#6b8e23' },
])

function money(v) { return v == null ? 0 : Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) }

onMounted(async () => { data.value = await portalOverview() })
</script>

<style scoped>
.greeting { margin-bottom: 22px; animation: pageIn .35s var(--ease-out, ease-out); }
.greeting h2 { margin: 0 0 4px; font-size: 24px; font-weight: 700; color: var(--ink-900); }
.greeting p { margin: 0; font-size: 14px; color: var(--ink-500); }

.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 26px; }
.kpi-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200);
  border-radius: var(--radius-lg); padding: 18px 20px; box-shadow: var(--shadow-sm);
  display: flex; align-items: center; gap: 14px;
  transition: transform .25s var(--ease, ease), box-shadow .25s var(--ease, ease);
  animation: cardIn .4s var(--ease-out, ease-out) backwards;
}
.kpi-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.kpi-icon {
  width: 48px; height: 48px; border-radius: 12px; display: grid; place-items: center; flex-shrink: 0;
}
.kpi-icon .el-icon { font-size: 24px; }
.kpi-label { font-size: 13px; color: var(--ink-500); }
.kpi-value { font-size: 24px; font-weight: 700; color: var(--ink-900); margin-top: 2px; line-height: 1.1; }
.kpi-unit { font-size: 12px; font-weight: 500; color: var(--ink-400); margin-left: 3px; }

.section { margin-bottom: 24px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ink-900); margin-bottom: 12px; }

.house-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
.house-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200); border-radius: var(--radius-lg);
  padding: 16px 18px; box-shadow: var(--shadow-sm); transition: transform .2s, box-shadow .2s;
  animation: cardIn .4s var(--ease-out, ease-out) backwards;
}
.house-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.house-no { font-size: 18px; font-weight: 700; color: var(--ink-900); }
.house-meta { font-size: 13px; color: var(--ink-500); margin: 6px 0 10px; }
.house-tags { display: flex; gap: 6px; }

.quick-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.quick-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200); border-radius: var(--radius-lg);
  padding: 18px 20px; box-shadow: var(--shadow-sm); cursor: pointer;
  display: flex; align-items: center; gap: 14px;
  transition: all .2s var(--ease, ease);
}
.quick-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); border-color: var(--brand-500); }
.q-icon { font-size: 26px; color: var(--brand-500); }
.q-title { font-size: 15px; font-weight: 600; color: var(--ink-900); }
.q-sub { font-size: 12px; color: var(--ink-500); margin-top: 2px; }
</style>
