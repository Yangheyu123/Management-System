<template>
  <div ref="el" :style="{ width: '100%', height: height }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' }
})

const el = ref()
let chart = null

function render() {
  if (!el.value) return
  if (!chart) chart = echarts.init(el.value)
  chart.setOption(props.option, true)
}

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', debouncedResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', debouncedResize)
  clearTimeout(timer)
  chart && chart.dispose()
})
watch(() => props.option, render, { deep: true })

/* resize 防抖：避免窗口拖动时频繁重绘卡顿 */
let timer = null
function debouncedResize() {
  clearTimeout(timer)
  timer = setTimeout(() => chart && chart.resize(), 150)
}
</script>
