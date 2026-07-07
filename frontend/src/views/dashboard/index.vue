<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">仪表盘</h2>
        <div class="page-sub">小区运营关键指标与待办提醒</div>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card">
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

const cards = computed(() => [
  { label: '房屋总数', value: data.value.houseTotal ?? '-', delta: `入住率 ${data.value.checkInRate ?? 0}%` },
  { label: '本月应收(元)', value: money(data.value.monthReceivable), delta: `收缴率 ${data.value.collectionRate ?? 0}%` },
  { label: '本月实收(元)', value: money(data.value.monthReceived), delta: `未收 ${money(data.value.monthUnpaid)} 元` },
  { label: '待处理工单', value: data.value.workorderPending ?? '-', delta: `本月新增 ${data.value.workorderMonthTotal ?? 0} 单` }
])
const todoList = computed(() => data.value.todoList || [])
const tagMap = { overdue_bill: 'danger', workorder: 'warning', equipment: 'info', parking: 'primary' }

const brand = '#2f6fed'
const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['应收', '实收'], right: 0, top: 0 },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: trend.value.categories, boundaryGap: false, axisLine: { lineStyle: { color: '#cbd5e1' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [
    { name: '应收', type: 'line', smooth: true, data: trend.value.receivable, itemStyle: { color: brand }, areaStyle: { color: 'rgba(47,111,237,.12)' } },
    { name: '实收', type: 'line', smooth: true, data: trend.value.received, itemStyle: { color: '#10b981' }, areaStyle: { color: 'rgba(16,185,129,.12)' } }
  ]
}))
const pieColors = ['#2f6fed', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']
const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, left: 'center' },
  color: pieColors,
  series: [{
    type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'], avoidLabelOverlap: true,
    itemStyle: { borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    data: pie.value.map(p => ({ name: p.name, value: Number(p.value) }))
  }]
}))
const woStatusOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 8, right: 16, top: 16, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: woStatus.value.map(s => s.name), axisLine: { lineStyle: { color: '#cbd5e1' } } },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [{ type: 'bar', data: woStatus.value.map(s => s.count), barWidth: 24, itemStyle: { color: brand, borderRadius: [6, 6, 0, 0] } }]
}))
const equipOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['正常', '故障', '维修中', '报废'], top: 0 },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: equip.value.map(e => e.categoryName) },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [
    { name: '正常', type: 'bar', stack: 't', data: equip.value.map(e => e.normal), itemStyle: { color: '#10b981' } },
    { name: '故障', type: 'bar', stack: 't', data: equip.value.map(e => e.fault), itemStyle: { color: '#ef4444' } },
    { name: '维修中', type: 'bar', stack: 't', data: equip.value.map(e => e.repairing), itemStyle: { color: '#f59e0b' } },
    { name: '报废', type: 'bar', stack: 't', data: equip.value.map(e => e.scrapped), itemStyle: { color: '#94a3b8' } }
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
})
</script>

<style scoped>
.unit { font-size: 13px; font-weight: 500; color: var(--ink-400); margin-left: 4px; }
.cb-title { font-size: 15px; font-weight: 600; color: var(--ink-900); margin-bottom: 12px; }
.todo { margin-top: 16px; background: #fff; border: 1px solid var(--ink-200); border-radius: var(--radius-lg); padding: 16px 20px; }
.todo-list { display: flex; flex-direction: column; gap: 12px; }
.todo-item { display: flex; align-items: center; gap: 12px; font-size: 14px; color: var(--ink-700); }
</style>

<!-- [fix] 仪表盘图表 resize 监听改为防抖，修复窗口拖动卡顿 -->
