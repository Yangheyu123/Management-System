<template>
  <div class="page">
    <div class="page-header"><div><h2 class="page-title">收费报表</h2><div class="page-sub">应收 / 实收 / 欠费统计与趋势</div></div></div>
    <div class="toolbar">
      <el-select v-model="filter.communityId" placeholder="小区" clearable style="width:160px"><el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" /></el-select>
      <el-select v-model="filter.feeItemType" placeholder="费用类型" clearable><el-option v-for="o in dictOptions('feeType')" :key="o.value" :label="o.label" :value="o.value" /></el-select>
      <el-date-picker v-model="filter.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始" />
      <el-date-picker v-model="filter.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束" />
      <el-button type="primary" @click="loadAll">查询</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <div class="stat-card"><div class="label">{{ c.label }}</div><div class="value">{{ c.value }}</div><div class="delta">{{ c.sub }}</div></div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="15">
        <div class="chart-box"><div class="cb-title">收费趋势（近 6 月）</div><EChart :option="trendOption" height="320px" /></div>
      </el-col>
      <el-col :span="9">
        <div class="chart-box"><div class="cb-title">实收类型占比</div><EChart :option="pieOption" height="320px" /></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import EChart from '@/components/EChart.vue'
import { chargeSummary, chargeTrend, chargeByType } from '@/api/stat'
import { communityAll } from '@/api/basedata'
import { dictOptions } from '@/utils/dict'

const communities = ref([])
const filter = reactive({ communityId: undefined, feeItemType: undefined, startDate: '', endDate: '' })
const summary = ref({})

const cards = computed(() => [
  { label: '应收合计', value: money(summary.value.receivable), sub: `共 ${summary.value.billCount || 0} 条账单` },
  { label: '实收合计', value: money(summary.value.received), sub: `收缴率 ${summary.value.collectionRate || 0}%` },
  { label: '欠费合计', value: money(summary.value.unpaid), sub: `已缴清 ${summary.value.paidCount || 0} 条` },
  { label: '逾期账单', value: summary.value.overdueCount || 0, sub: '已过缴费截止日' }
])

const trend = ref({ categories: [], receivable: [], received: [] })
const pie = ref([])
const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' }, legend: { data: ['应收', '实收'], right: 0 },
  grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
  xAxis: { type: 'category', data: trend.value.categories, boundaryGap: false },
  yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f1f5f9' } } },
  series: [
    { name: '应收', type: 'line', smooth: true, data: trend.value.receivable, itemStyle: { color: '#2f6fed' }, areaStyle: { color: 'rgba(47,111,237,.12)' } },
    { name: '实收', type: 'line', smooth: true, data: trend.value.received, itemStyle: { color: '#10b981' }, areaStyle: { color: 'rgba(16,185,129,.12)' } }
  ]
}))
const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' }, legend: { bottom: 0 },
  color: ['#2f6fed', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
  series: [{ type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'], itemStyle: { borderColor: '#fff', borderWidth: 2 }, label: { show: false },
    data: pie.value.map(p => ({ name: p.name, value: Number(p.value) })) }]
}))

function money(v) { return v == null ? '-' : '¥' + Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) }

async function loadAll() {
  const [s, tr, pt] = await Promise.all([
    chargeSummary({ ...filter }),
    chargeTrend({ communityId: filter.communityId, months: 6, feeItemType: filter.feeItemType }),
    chargeByType({ communityId: filter.communityId, startDate: filter.startDate, endDate: filter.endDate })
  ])
  summary.value = s; trend.value = tr; pie.value = pt
}
onMounted(async () => { communities.value = await communityAll(); loadAll() })
</script>
