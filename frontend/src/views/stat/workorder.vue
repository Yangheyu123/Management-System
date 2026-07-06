<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">工单报表</h2><div class="page-sub">工单分布与维修员绩效</div></div></div>
    <div class="toolbar">
      <el-select v-model="filter.communityId" placeholder="小区" clearable style="width:160px"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
      <el-button type="primary" @click="loadAll">查询</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card"><div class="label">{{ c.label }}</div><div class="value">{{ c.value }}</div><div class="delta">{{ c.sub }}</div></div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="11">
        <div class="chart-box"><div class="cb-title">工单状态分布</div><EChart :option="statusOption" height="320px" /></div>
      </el-col>
      <el-col :span="13">
        <div class="chart-box"><div class="cb-title">维修员绩效排名</div><EChart :option="handlerOption" height="320px" /></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import EChart from '@/components/EChart.vue'
import { workorderSummary, workorderByStatus, workorderByHandler } from '@/api/stat'
import { communityAll } from '@/api/basedata'

const communities = ref([])
const filter = reactive({ communityId: undefined })
const summary = ref({})
const statusData = ref([])
const handlerData = ref([])

const cards = computed(() => [
  { label: '工单总数', value: summary.value.total || 0, sub: `已完成 ${summary.value.finished || 0}` },
  { label: '处理中', value: summary.value.handling || 0, sub: `待派单 ${summary.value.pending || 0}` },
  { label: '平均处理时长', value: (summary.value.avgHandleHours ?? '-') + '', sub: '小时' },
  { label: '平均评分', value: (summary.value.avgRating ?? '-') + '', sub: '满分 5 星' }
])

const statusOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0 },
  color: ['#94a3b8', '#f59e0b', '#f59e0b', '#2f6fed', '#10b981', '#cbd5e1'],
  series: [{ type: 'pie', radius: ['40%', '66%'], center: ['50%', '44%'], itemStyle: { borderColor: '#fff', borderWidth: 2 }, label: { show: false },
    data: statusData.value.map(s => ({ name: s.name, value: s.count })) }]
}))
const handlerOption = computed(() => ({
  tooltip: { trigger: 'axis' }, grid: { left: 8, right: 24, top: 30, bottom: 8, containLabel: true },
  legend: { data: ['完成数', '平均评分'], right: 0 },
  xAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  yAxis: { type: 'category', data: handlerData.value.map(h => h.handlerName).reverse() },
  series: [
    { name: '完成数', type: 'bar', data: handlerData.value.map(h => h.finishedCount).reverse(), itemStyle: { color: '#2f6fed', borderRadius: [0, 6, 6, 0] } },
    { name: '平均评分', type: 'bar', data: handlerData.value.map(h => Number(h.avgRating) || 0).reverse(), itemStyle: { color: '#10b981', borderRadius: [0, 6, 6, 0] } }
  ]
}))

async function loadAll() {
  const [s, st, hd] = await Promise.all([
    workorderSummary(filter), workorderByStatus(filter), workorderByHandler(filter)
  ])
  summary.value = s; statusData.value = st; handlerData.value = hd
}
onMounted(async () => { communities.value = await communityAll(); loadAll() })
</script>
