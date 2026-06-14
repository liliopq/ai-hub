<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="max-w-6xl mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        <div class="flex items-center space-x-8">
          <router-link to="/" class="flex items-center space-x-2">
            <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <Sparkles class="w-5 h-5 text-white" />
            </div>
            <span class="text-xl font-bold text-gray-900">AI Hub</span>
          </router-link>
          <nav class="hidden md:flex items-center space-x-6">
            <router-link to="/" class="text-gray-600 hover:text-primary-600 transition-colors">首页</router-link>
            <router-link to="/post/create" class="text-gray-600 hover:text-primary-600 transition-colors">发帖</router-link>
            <router-link to="/ai-chat" class="text-gray-600 hover:text-primary-600 transition-colors">AI聊天</router-link>
          </nav>
        </div>
        <div class="flex items-center space-x-4">
          <div v-if="isLoggedIn" class="flex items-center space-x-4">
            <!-- 消息通知链接 -->
            <router-link 
              to="/notifications" 
              class="relative text-gray-600 hover:text-primary-600 transition-colors"
            >
              消息
              <span 
                v-if="unreadCount > 0" 
                class="ml-1 px-1.5 py-0.5 bg-red-500 text-white text-xs rounded-full"
              >
                {{ unreadCount > 9 ? '9+' : unreadCount }}
              </span>
            </router-link>
            <div class="relative" ref="dropdownRef">
              <button 
                @click="showDropdown = !showDropdown"
                class="flex items-center space-x-2 hover:bg-gray-100 rounded-lg p-1 transition-colors"
              >
                <img 
                  :src="user?.avatar || defaultAvatar" 
                  :alt="user?.username"
                  class="w-8 h-8 rounded-full object-cover"
                />
                <span class="text-sm font-medium text-gray-700">{{ user?.username }}</span>
                <ChevronDown class="w-4 h-4 text-gray-500" />
              </button>
              <div 
                v-show="showDropdown"
                class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-100 py-1 z-50"
              >
                <button
                  @click="goToProfile" 
                  class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <User class="w-4 h-4 inline mr-2" />
                  个人中心
                </button>
                <router-link 
                  to="/admin" 
                  v-if="isAdmin"
                  class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                  @click="showDropdown = false"
                >
                  <Settings class="w-4 h-4 inline mr-2" />
                  管理后台
                </router-link>
                <div class="border-t border-gray-100 my-1"></div>
                <button 
                  @click="handleLogout"
                  class="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-gray-50"
                >
                  <LogOut class="w-4 h-4 inline mr-2" />
                  退出登录
                </button>
              </div>
            </div>
          </div>
          <div v-else class="flex items-center space-x-3">
            <router-link to="/login" class="text-gray-600 hover:text-primary-600 transition-colors">登录</router-link>
            <router-link 
              to="/register" 
              class="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors text-sm"
            >
              注册
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Sparkles, ChevronDown, User, Settings, LogOut } from 'lucide-vue-next'
import { logout } from '../api/auth'
import { getUnreadCount } from '../api/notification'
import { getUser, isLoggedIn as checkLoggedIn, isAdmin as checkAdmin, removeToken, removeUser } from '../utils/auth'

const router = useRouter()
const user = ref(getUser())
const showDropdown = ref(false)
const unreadCount = ref(0)
const dropdownRef = ref(null)

// 监听user-updated自定义事件，以响应头像等用户信息的更新
const handleUserUpdated = (e) => {
  user.value = e.detail
}
const isLoggedIn = computed(() => checkLoggedIn())
const isAdmin = computed(() => checkAdmin())
const defaultAvatar = 'https://via.placeholder.com/150'
const fetchUnreadCount = async () => {
  if (isLoggedIn.value) {
    try {
      const data = await getUnreadCount()
      unreadCount.value = data.count
    } catch (e) {
      console.error('获取未读通知失败:', e)
    }
  }
}
const handleLogout = async () => {
  try {
    await logout()
  } catch (e) {
    console.error('退出登录失败:', e)
  } finally {
    removeToken()
    removeUser()
    user.value = null
    showDropdown.value = false
    window.location.href = '/'
  }
}
const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    showDropdown.value = false
  }
}

const goToProfile = () => {
  showDropdown.value = false
  router.push('/profile')
}
const handleNotificationUpdated = () => {
  fetchUnreadCount()
}

onMounted(() => {
  fetchUnreadCount()
  document.addEventListener('click', handleClickOutside)
  // 添加user-updated事件监听器
  window.addEventListener('user-updated', handleUserUpdated)
  // 添加notification-updated事件监听器
  window.addEventListener('notification-updated', handleNotificationUpdated)
})
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  // 移除user-updated事件监听器
  window.removeEventListener('user-updated', handleUserUpdated)
  // 移除notification-updated事件监听器
  window.removeEventListener('notification-updated', handleNotificationUpdated)
})
defineExpose({
  fetchUnreadCount
})
</script>
