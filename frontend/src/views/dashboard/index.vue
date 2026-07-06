<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">仪表盘</h2>
        <div class="page-sub">小区运营关键指标与待办提醒</div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="(c, i) in cards" :key="c.label">
        <div class="stat-card" :style="{ animationDelay: (i * 0.06) + 's' }">
          <div class="label">{{ c.label }}</div>
          <div class="value">{{ c.value }}<span class="unit">{{ c.unit }}</span></div>
          <div class="delta">{{ c.delta }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <div class="chart-box">
          <div class="cb-title">近 6 个月收费趋势</div>
          <EChart :option="trendOption" height="320px" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-box">
          <div class="cb-title">本月实收构成</div>
          <EChart :option="pieOption" height="320px" />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <div class="chart-box">
          <div class="cb-title">工单状态分布</div>
          <EChart :option="woStatusOption" height="280px" />
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-box">
          <div class="cb-title">设备状态统计</div>
          <EChart :option="equipOption" height="280px" />
        </div>
      </el-col>
    </el-row>

    <div class="todo" v-if="todoList.length">
      <div class="cb-title">待办提醒</div>
      <div class="todo-list">
        <div class="todo-item" v-for="t in todoList" :key="t.type">
          <el-tag :type="tagMap[t.type]" effect="light" round>{{ t.count }}</el-tag>
          <span>{{ t.desc }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import EChart from '@/components/EChart.vue'
import { dashboard, chargeTrend, chargeByType, workorderByStatus, equipmentStatus } from '@/api/stat'

const data = ref({})
const trend = ref({ categories: [], receivable: [], received: [] })
const pie = ref([])
const woStatus = ref([])
const equip = ref([])

/* 数字滚动：从 0 平滑过渡到目标值 */
const anim = ref({ houseTotal: 0, monthReceivable: 0, monthReceived: 0, workorderPending: 0 })
function roll(key, to) {
  if (to == null) return
  const from = 0, dur = 900, start = performance.now()
  function step(now) {
    const t = Math.min((now - start) / dur, 1)
    const eased = 1 - Math.pow(1 - t, 3)          // easeOutCubic
    anim.value[key] = Math.round(from + (to - from) * eased)
    if (t < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

const cards = computed(() => [
  { label: '房屋总数', value: anim.value.houseTotal || '-', delta: `入住率 ${data.value.checkInRate ?? 0}%` },
  { label: '本月应收(元)', value: money(anim.value.monthReceivable || null), delta: `收缴率 ${data.value.collectionRate ?? 0}%` },
  { label: '本月实收(元)', value: money(anim.value.monthReceived || null), delta: `未收 ${money(data.value.monthUnpaid)} 元` },
  { label: '待处理工单', value: anim.value.workorderPending || '-', delta: `本月新增 ${data.value.workorderMonthTotal ?? 0} 单` }
])
const todoList = computed(() => data.value.todoList || [])
const tagMap = { overdue_bill: 'danger', workorder: 'warning', equipment: 'info', parking: 'primary' }

const brand = '#b8860b'
const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['应收', '实收'], right: 0, top: 0, textStyle: { color: '#6b5d4f' } },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: trend.value.categories, boundaryGap: false, axisLine: { lineStyle: { color: '#d6c9b4' } }, axisLabel: { color: '#a89c8c' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0e9dc' } }, axisLabel: { color: '#a89c8c' } },
  series: [
    { name: '应收', type: 'line', smooth: true, data: trend.value.receivable, itemStyle: { color: brand }, lineStyle: { width: 3 }, symbolSize: 7, areaStyle: { color: 'rgba(184,134,11,.12)' } },
    { name: '实收', type: 'line', smooth: true, data: trend.value.received, itemStyle: { color: '#946906' }, lineStyle: { width: 3 }, symbolSize: 7, areaStyle: { color: 'rgba(148,105,6,.10)' } }
  ]
}))
const pieColors = ['#b8860b', '#d4a83a', '#946906', '#e6c878', '#6b5d4f']
const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, left: 'center', textStyle: { color: '#6b5d4f' } },
  color: pieColors,
  series: [{
    type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'], avoidLabelOverlap: true,
    itemStyle: { borderColor: '#fffdf8', borderWidth: 2 },
    label: { show: false },
    data: pie.value.map(p => ({ name: p.name, value: Number(p.value) }))
  }]
}))
const woStatusOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: woStatus.value.map(s => s.name), axisLine: { lineStyle: { color: '#d6c9b4' } }, axisLabel: { color: '#a89c8c' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0e9dc' } }, axisLabel: { color: '#a89c8c' } },
  series: [{
    type: 'bar', data: woStatus.value.map(s => s.count), barWidth: 26,
    itemStyle: { color: brand, borderRadius: [6, 6, 0, 0] },
    emphasis: { itemStyle: { color: '#946906' } }
  }]
}))
const equipOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['正常', '故障', '维修中', '报废'], top: 0, textStyle: { color: '#6b5d4f' } },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: equip.value.map(e => e.categoryName), axisLine: { lineStyle: { color: '#d6c9b4' } }, axisLabel: { color: '#a89c8c' } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0e9dc' } }, axisLabel: { color: '#a89c8c' } },
  series: [
    { name: '正常', type: 'bar', stack: 't', data: equip.value.map(e => e.normal), itemStyle: { color: '#b8860b' } },
    { name: '故障', type: 'bar', stack: 't', data: equip.value.map(e => e.fault), itemStyle: { color: '#c45e3b' } },
    { name: '维修中', type: 'bar', stack: 't', data: equip.value.map(e => e.repairing), itemStyle: { color: '#e6a23c' } },
    { name: '报废', type: 'bar', stack: 't', data: equip.value.map(e => e.scrapped), itemStyle: { color: '#a89c8c' } }
  ]
}))

function money(v) { return v == null ? '-' : Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) }

onMounted(async () => {
  const [d, tr, pt, ws, eq] = await Promise.all([
    dashboard({}), chargeTrend({ months: 6 }), chargeByType({}),
    workorderByStatus({}), equipmentStatus({})
  ])
  data.value = d
  trend.value = tr
  pie.value = pt
  woStatus.value = ws
  equip.value = eq
  // 触发 KPI 数字滚动
  roll('houseTotal', d.houseTotal)
  roll('monthReceivable', d.monthReceivable)
  roll('monthReceived', d.monthReceived)
  roll('workorderPending', d.workorderPending)
})
</script>

<style scoped>
.unit { font-size: 13px; font-weight: 500; color: var(--ink-400); margin-left: 4px; }
.cb-title { font-size: 15px; font-weight: 600; color: var(--ink-900); margin-bottom: 14px; }
.todo {
  margin-top: 16px; background: var(--paper, #fffdf8); border: 1px solid var(--ink-200);
  border-radius: var(--radius-lg); padding: 18px 22px; box-shadow: var(--shadow-sm);
}
.todo-list { display: flex; flex-direction: column; gap: 12px; }
.todo-item {
  display: flex; align-items: center; gap: 12px; font-size: 14px; color: var(--ink-700);
  padding: 8px 12px; border-radius: var(--radius);
  transition: background .2s;
}
.todo-item:hover { background: var(--brand-50); }
</style>
