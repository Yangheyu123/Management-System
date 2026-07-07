<template>
  <div class="page-portal">
    <div class="pp-header">
      <h2 class="pp-title">我的报修</h2>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 提交报修</el-button>
    </div>

    <div class="pp-filter">
      <el-select v-model="filter.status" placeholder="状态筛选" clearable @change="load" style="width:140px">
        <el-option v-for="o in statusOpts" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
    </div>

    <div class="wo-list" v-loading="loading">
      <div class="wo-card" v-for="w in list" :key="w.id">
        <div class="wo-head">
          <span class="wo-no">{{ w.orderNo }}</span>
          <el-tag size="small" :type="tagType('woStatus', w.status)" effect="light">{{ dictName('woStatus', w.status) }}</el-tag>
        </div>
        <div class="wo-title">{{ w.title }}</div>
        <div class="wo-meta">
          <span><el-icon><Warning /></el-icon> {{ dictName('woType', w.type) }}</span>
          <span><el-icon><Star /></el-icon> {{ dictName('woPriority', w.priority) }}优先级</span>
          <span v-if="w.handlerName"><el-icon><User /></el-icon> {{ w.handlerName }}</span>
          <span><el-icon><Clock /></el-icon> {{ w.createTime }}</span>
        </div>
        <div class="wo-desc" v-if="w.description">{{ w.description }}</div>
        <div class="wo-foot" v-if="w.status === 4">
          <el-button size="small" type="primary" plain @click="openRate(w)">评价</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && !list.length" description="暂无报修记录" />
    </div>

    <el-pagination v-if="total > 0" :current-page="filter.page" :page-size="filter.size" :total="total"
      layout="prev, pager, next" @current-change="load" class="pp-page" />

    <!-- 提交报修弹窗 -->
    <el-dialog v-model="createVisible" title="提交报修" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="房屋" prop="houseId">
          <el-select v-model="createForm.houseId" placeholder="选择报修房屋" style="width:100%">
            <el-option v-for="h in houses" :key="h.houseId" :label="`${h.communityName} ${h.houseNo}`" :value="h.houseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="一句话描述问题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="createForm.type" placeholder="选择类型" style="width:100%">
            <el-option v-for="o in typeOpts" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="createForm.priority">
            <el-radio-button v-for="o in priorityOpts" :key="o.value" :value="o.value">{{ o.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="详细描述故障情况" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="rateVisible" title="评价维修服务" width="420px">
      <el-form label-width="80px">
        <el-form-item label="工单">{{ rateTarget.orderNo }} - {{ rateTarget.title }}</el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="rateForm.rating" :texts="['很差','差','一般','满意','很满意']" show-text />
        </el-form-item>
        <el-form-item label="评价">
          <el-input v-model="rateForm.ratingComment" type="textarea" :rows="3" placeholder="对本次服务的评价（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" :loading="rateLoading" @click="submitRate">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Warning, Star, User, Clock } from '@element-plus/icons-vue'
import { DICT, dictName, dictOptions, tagType } from '@/utils/dict'
import { portalWorkorderPage, portalWorkorderCreate, portalWorkorderRate, portalHouses } from '@/api/portal'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const filter = reactive({ page: 1, size: 10, status: undefined })
const statusOpts = Object.entries(DICT.woStatus).map(([k, v]) => ({ value: Number(k), label: v }))
const typeOpts = dictOptions('woType')
const priorityOpts = dictOptions('woPriority')

async function load() {
  loading.value = true
  try {
    const r = await portalWorkorderPage({ page: filter.page, size: filter.size, status: filter.status })
    list.value = r.list; total.value = r.total
  } finally { loading.value = false }
}

// 提交报修
const houses = ref([])
const createVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref()
const createForm = reactive({ houseId: undefined, title: '', type: undefined, priority: 2, description: '' })
const createRules = {
  houseId: [{ required: true, message: '请选择房屋', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
}
async function openCreate() {
  if (!houses.value.length) houses.value = await portalHouses()
  Object.assign(createForm, { houseId: undefined, title: '', type: undefined, priority: 2, description: '' })
  createVisible.value = true
}
async function submitCreate() {
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    createLoading.value = true
    try {
      await portalWorkorderCreate({ ...createForm })
      ElMessage.success('报修提交成功')
      createVisible.value = false
      filter.page = 1; load()
    } catch (e) {} finally { createLoading.value = false }
  })
}

// 评价
const rateVisible = ref(false)
const rateLoading = ref(false)
const rateTarget = ref({})
const rateForm = reactive({ rating: 5, ratingComment: '' })
function openRate(w) {
  rateTarget.value = w
  rateForm.rating = 5; rateForm.ratingComment = ''
  rateVisible.value = true
}
async function submitRate() {
  rateLoading.value = true
  try {
    await portalWorkorderRate(rateTarget.value.id, { ...rateForm })
    ElMessage.success('评价提交成功')
    rateVisible.value = false; load()
  } catch (e) {} finally { rateLoading.value = false }
}

onMounted(load)
</script>

<style scoped>
.page-portal { animation: pageIn .35s var(--ease-out, ease-out); }
.pp-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.pp-title { margin: 0; font-size: 22px; font-weight: 700; color: var(--ink-900); }
.pp-filter { margin-bottom: 14px; }
.pp-page { justify-content: center; margin-top: 18px; }

.wo-list { display: flex; flex-direction: column; gap: 12px; }
.wo-card {
  background: var(--paper, #fffdf8); border: 1px solid var(--ink-200); border-radius: var(--radius-lg);
  padding: 16px 20px; box-shadow: var(--shadow-sm); transition: transform .2s, box-shadow .2s;
}
.wo-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.wo-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.wo-no { font-size: 13px; color: var(--ink-400); font-family: monospace; }
.wo-title { font-size: 16px; font-weight: 600; color: var(--ink-900); }
.wo-meta { display: flex; flex-wrap: wrap; gap: 16px; font-size: 13px; color: var(--ink-500); margin: 8px 0; }
.wo-meta span { display: flex; align-items: center; gap: 4px; }
.wo-meta .el-icon { font-size: 14px; }
.wo-desc { font-size: 13px; color: var(--ink-700); background: var(--ink-50); padding: 8px 12px; border-radius: 8px; margin-top: 8px; }
.wo-foot { margin-top: 12px; text-align: right; }
</style>
