// v-permission="'system:user:add'" 按钮级权限控制
import { useUserStore } from '@/stores/user'

export default {
  install(app) {
    app.directive('permission', {
      mounted(el, binding) {
        const store = useUserStore()
        const code = binding.value
        if (code && !store.hasPermission(code)) {
          el.parentNode && el.parentNode.removeChild(el)
        }
      }
    })
  }
}
