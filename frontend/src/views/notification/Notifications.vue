<template>
  <div class="min-h-screen">
    <div class="max-w-3xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold text-gray-900">通知中心</h1>
          <button 
            @click="markAllRead"
            :disabled="loading"
            class="text-primary-600 hover:text-primary-700 text-sm"
          >
            全部标记为已读
          </button>
        </div>
        <div v-if="loading" class="text-center py-12">
          <Loader2 class="w-8 h-8 text-primary-600 animate-spin mx-auto" />
        </div>
        <div v-else-if="notifications.length === 0" class="text-center py-12">
          <Bell class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p class="text-gray-500">暂无通知</p>
        </div>
        <div v-else class="space-y-3">
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            :class="[
              'p-4 rounded-lg border transition-colors cursor-pointer',
              notification.isRead 
                ? 'bg-white border-gray-100 hover:bg-gray-50' 
                : 'bg-primary-50 border-primary-200'
            ]"
            @click="handleNotificationClick(notification)"
          >
            <div class="flex items-start space-x-3">
              <div :class="[
                'w-10 h-10 rounded-full flex items-center justify-center',
                getTypeColor(notification.type)
              ]">
                <component :is="getTypeIcon(notification.type)" class="w-5 h-5" />
              </div>
              <div class="flex-1">
                <p class="text-gray-900">{{ notification.content }}</p>
                <div class="flex items-center space-x-3 mt-1">
                  <span class="text-sm text-gray-500">{{ formatTime(notification.createTime) }}</span>
                  <span v-if="notification.sourceUser" class="text-sm text-gray-500">
                    {{ notification.sourceUser.username }}
                  </span>
                </div>
              </div>
              <button 
                @click.stop="markNotificationAsRead(notification.id)"
                class="text-gray-400 hover:text-gray-600"
              >
                <Check class="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
        <div v-if="hasMore" class="text-center mt-8">
          <button 
            @click="loadMore"
            :disabled="loading"
            class="px-8 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
          >
            <span v-if="loading">加载中...</span>
            <span v-else>加载更多</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, MessageSquare, Heart, Bookmark, UserPlus, Info, Check, Loader2 } from 'lucide-vue-next'
import { getNotificationList, markAsRead as apiMarkAsRead, markAllAsRead } from '../../api/notification'
import { connect as connectWebSocket, disconnect as disconnectWebSocket } from '../../utils/stomp'

const router = useRouter()

const notifications = ref([])
const loading = ref(false)
const hasMore = ref(true)
const showToast = ref(false)
const toastMessage = ref('')

const filters = ref({
  page: 1,
  size: 10
})

const getTypeIcon = (type) => {
  const icons = {
    COMMENT: MessageSquare,
    LIKE: Heart,
    COLLECT: Bookmark,
    FOLLOW: UserPlus,
    SYSTEM: Info
  }
  return icons[type] || Bell
}

const getTypeColor = (type) => {
  const colors = {
    COMMENT: 'bg-blue-100 text-blue-600',
    LIKE: 'bg-red-100 text-red-600',
    COLLECT: 'bg-yellow-100 text-yellow-600',
    FOLLOW: 'bg-green-100 text-green-600',
    SYSTEM: 'bg-gray-100 text-gray-600'
  }
  return colors[type] || 'bg-gray-100 text-gray-600'
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

const loadNotifications = async (reset = false) => {
  if (loading.value) return
  
  loading.value = true
  
  if (reset) {
    filters.value.page = 1
    notifications.value = []
    hasMore.value = true
  }
  
  try {
    console.log('开始加载通知，参数:', filters.value)
    const data = await getNotificationList(filters.value)
    console.log('通知数据返回:', data)
    console.log('通知数据类型:', typeof data)
    console.log('通知记录:', data?.records || data)
    
    // 兼容不同的返回数据结构
    const records = data?.records || data || []
    notifications.value = [...notifications.value, ...records]
    hasMore.value = data?.current < data?.pages || false
    filters.value.page++
  } catch (e) {
    console.error('获取通知失败:', e)
    console.error('错误详情:', e.response || e.message)
    console.error('完整错误对象:', e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  loadNotifications()
}

const markNotificationAsRead = async (notificationId) => {
  try {
    await apiMarkAsRead(notificationId)
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = true
    }
  } catch (e) {
    console.error('标记已读失败:', e)
  }
}

const markAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
  } catch (e) {
    console.error('全部标记已读失败:', e)
  }
}

const handleNotificationClick = (notification) => {
  // 如果未读，标记为已读
  if (!notification.isRead) {
    markNotificationAsRead(notification.id)
  }
  
  // 如果有帖子ID，跳转到帖子详情页
  if (notification.postId) {
    router.push(`/post/${notification.postId}`)
  }
}

const handleNewNotification = (notification) => {
  // 将新通知插入到列表顶部
  notifications.value = [notification, ...notifications.value]
  
  // 显示提示
  toastMessage.value = notification.content
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

onMounted(() => {
  loadNotifications()
  connectWebSocket(handleNewNotification)
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>
