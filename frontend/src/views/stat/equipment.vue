<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">设备报表</h2><div class="page-sub">设备状态与在线率</div></div></div>
    <div class="toolbar">
      <el-select v-model="communityId" placeholder="小区" clearable style="width:160px" @change="load"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
    </div>
    <div class="chart-box">
      <div class="cb-title">各类设备状态</div>
      <EChart :option="barOption" height="340px" />
    </div>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8" v-for="e in list" :key="e.category">
        <div class="stat-card">
          <div class="label">{{ e.categoryName }} · 在线率 {{ e.onlineRate }}%</div>
          <div class="value">{{ e.total }}</div>
          <div class="delta">
            <el-tag type="success" size="small" effect="plain">正常 {{ e.normal }}</el-tag>
            <el-tag type="danger" size="small" effect="plain" style="margin-left:6px">故障 {{ e.fault }}</el-tag>
            <el-tag type="warning" size="small" effect="plain" style="margin-left:6px">维修 {{ e.repairing }}</el-tag>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import EChart from '@/components/EChart.vue'
import { equipmentStatus } from '@/api/stat'
import { communityAll } from '@/api/basedata'

const communities = ref([]); const communityId = ref(undefined); const list = ref([])
const barOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['正常', '故障', '维修中', '报废'], top: 0 },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: list.value.map(e => e.categoryName) },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [
    { name: '正常', type: 'bar', stack: 's', data: list.value.map(e => e.normal), itemStyle: { color: '#10b981' } },
    { name: '故障', type: 'bar', stack: 's', data: list.value.map(e => e.fault), itemStyle: { color: '#ef4444' } },
    { name: '维修中', type: 'bar', stack: 's', data: list.value.map(e => e.repairing), itemStyle: { color: '#f59e0b' } },
    { name: '报废', type: 'bar', stack: 's', data: list.value.map(e => e.scrapped), itemStyle: { color: '#94a3b8' } }
  ]
}))
async function load() { list.value = await equipmentStatus({ communityId: communityId.value }) }
onMounted(async () => { communities.value = await communityAll(); load() })
</script>
