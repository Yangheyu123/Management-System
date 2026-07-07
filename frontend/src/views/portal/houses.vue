<template>
  <div class="page-portal">
    <div class="pp-header">
      <h2 class="pp-title">我的房屋</h2>
    </div>

    <div class="house-list" v-loading="loading">
      <div class="house-card" v-for="(h, i) in list" :key="h.houseId" :style="{ animationDelay: (i * 0.06) + 's' }">
        <div class="house-banner">
          <el-icon class="banner-icon"><House /></el-icon>
          <span class="house-status" :class="{ primary: h.isPrimary }">{{ h.isPrimary ? '户主' : h.relation }}</span>
        </div>
        <div class="house-body">
          <div class="house-no">{{ h.houseNo }}</div>
          <div class="house-loc">{{ h.communityName }} · {{ h.buildingName }}</div>
          <div class="house-attrs">
            <div class="attr"><span class="attr-label">面积</span><span class="attr-val">{{ h.area }}㎡</span></div>
            <div class="attr"><span class="attr-label">关系</span><span class="attr-val">{{ h.isPrimary ? '户主' : h.relation }}</span></div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && !list.length" description="暂无关联房屋" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { House } from '@element-plus/icons-vue'
import { portalHouses } from '@/api/portal'

const loading = ref(false)
const list = ref([])

onMounted(async () => {
  loading.value = true
  try { list.value = await portalHouses() } finally { loading.value = false }
})
</script>

<style scoped>
.page-portal { animation: pageIn .35s var(--ease-out, ease-out); }
.pp-header { margin-bottom: 18px; }
.pp-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--ink-900); }

.house-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.house-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200); border-radius: var(--radius-lg);
  overflow: hidden; box-shadow: var(--shadow-sm);
  transition: transform .25s var(--ease, ease), box-shadow .25s var(--ease, ease);
  animation: cardIn .4s var(--ease-out, ease-out) backwards;
}
.house-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }

.house-banner {
  height: 90px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #fdf3d7, #f0dcab);
  position: relative;
}
.banner-icon { font-size: 40px; color: var(--brand-500); }
.house-status {
  position: absolute; top: 10px; right: 10px;
  font-size: 12px; padding: 2px 10px; border-radius: 10px;
  background: rgba(255,253,248,.85); color: var(--ink-500);
}
.house-status.primary { background: var(--brand-500); color: #fffdf8; }

.house-body { padding: 16px 20px; }
.house-no { font-size: 20px; font-weight: 700; color: var(--ink-900); }
.house-loc { font-size: 13px; color: var(--ink-500); margin: 6px 0 14px; }
.house-attrs { display: flex; gap: 24px; }
.attr { display: flex; flex-direction: column; }
.attr-label { font-size: 12px; color: var(--ink-400); }
.attr-val { font-size: 15px; font-weight: 600; color: var(--ink-700); margin-top: 2px; }
</style>
