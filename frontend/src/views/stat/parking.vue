<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">车位报表</h2><div class="page-sub">车位使用率统计</div></div></div>
    <div class="toolbar">
      <el-select v-model="communityId" placeholder="小区" clearable style="width:160px" @change="load"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
    </div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card"><div class="label">{{ c.label }}</div><div class="value">{{ c.value }}</div></div>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12"><div class="chart-box"><div class="cb-title">整体使用情况</div><EChart :option="pieOption" height="300px" /></div></el-col>
      <el-col :span="12"><div class="chart-box"><div class="cb-title">地上/地下对比</div><EChart :option="barOption" height="300px" /></div></el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import EChart from '@/components/EChart.vue'
import { parkingUsage } from '@/api/stat'
import { communityAll } from '@/api/basedata'

const communities = ref([]); const communityId = ref(undefined); const data = ref({})
const cards = computed(() => [
  { label: '车位总数', value: data.value.total ?? '-' },
  { label: '使用中', value: data.value.inUse ?? '-' },
  { label: '空闲', value: data.value.free ?? '-' },
  { label: '使用率', value: (data.value.usageRate ?? 0) + '%' }
])
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' }, legend: { bottom: 0 }, color: ['#2f6fed', '#10b981', '#f59e0b'],
  series: [{ type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'], itemStyle: { borderColor: '#fff', borderWidth: 2 }, label: { show: false },
    data: [
      { name: '使用中', value: data.value.inUse || 0 },
      { name: '空闲', value: data.value.free || 0 },
      { name: '已售', value: data.value.sold || 0 }
    ] }]
}))
const barOption = computed(() => {
  const byArea = data.value.byArea || []
  return {
    tooltip: { trigger: 'axis' }, legend: { data: ['总数', '使用中'], top: 0 },
    grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
    xAxis: { type: 'category', data: byArea.map(a => a.areaName) },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
    series: [
      { name: '总数', type: 'bar', data: byArea.map(a => a.total), itemStyle: { color: '#94a3b8', borderRadius: [6, 6, 0, 0] } },
      { name: '使用中', type: 'bar', data: byArea.map(a => a.inUse), itemStyle: { color: '#2f6fed', borderRadius: [6, 6, 0, 0] } }
    ]
  }
})
async function load() { data.value = await parkingUsage({ communityId: communityId.value }) }
onMounted(async () => { communities.value = await communityAll(); load() })
</script>
