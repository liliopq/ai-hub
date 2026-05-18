<template>
  <div class="min-h-screen">
    <div class="max-w-4xl mx-auto">
      <div v-if="loading" class="text-center py-12">
        <Loader2 class="w-8 h-8 text-primary-600 animate-spin mx-auto" />
      </div>
      <div v-else class="bg-white rounded-xl shadow-sm p-8 mb-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">个人中心</h1>
        <div class="flex items-start space-x-6 mb-8">
          <div class="relative">
            <img 
              :src="user.avatar || defaultAvatar" 
              :alt="user.username"
              class="w-24 h-24 rounded-full object-cover border-4 border-gray-100"
            />
            <label class="absolute bottom-0 right-0 w-8 h-8 bg-primary-600 rounded-full cursor-pointer hover:bg-primary-700 transition-colors flex items-center justify-center">
              <Upload class="w-4 h-4 text-white" />
              <input 
                type="file" 
                accept="image/*" 
                class="hidden"
                @change="handleAvatarUpload"
              />
            </label>
          </div>
          <div class="flex-1">
            <h2 class="text-xl font-semibold text-gray-900 mb-1">{{ user.username }}</h2>
            <p class="text-gray-500 mb-4">{{ user.email || '未设置邮箱' }}</p>
            <div class="flex items-center space-x-4 text-sm text-gray-500">
              <span>注册时间：{{ formatDate(user.createTime) }}</span>
              <span v-if="user.phoneNumber">手机号：{{ user.phoneNumber }}</span>
            </div>
          </div>
        </div>
        
        <!-- 我的帖子标签页 -->
        <div class="mb-8">
          <div class="border-b border-gray-200">
            <nav class="flex space-x-8">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                @click="activeTab = tab.key"
                :class="[
                  'py-4 px-1 border-b-2 font-medium text-sm',
                  activeTab === tab.key
                    ? 'border-primary-600 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                ]"
              >
                {{ tab.label }}
              </button>
            </nav>
          </div>
          
          <div class="mt-6">
            <div v-if="postsLoading" class="text-center py-8">
              <Loader2 class="w-6 h-6 text-primary-600 animate-spin mx-auto" />
            </div>
            <div v-else-if="currentPosts.length === 0" class="text-center py-8">
              <p class="text-gray-500">暂无{{ getTabLabel() }}</p>
            </div>
            <div v-else class="space-y-4">
              <div 
                v-for="post in currentPosts" 
                :key="post.id"
                @click="goToPost(post.id)"
                class="p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:shadow-sm cursor-pointer transition-all"
              >
                <h3 class="font-semibold text-gray-900 mb-2">{{ post.title }}</h3>
                <div class="flex items-center space-x-4 text-sm text-gray-500">
                  <span>{{ post.category }}</span>
                  <span>{{ formatDate(post.createTime) }}</span>
                  <span class="flex items-center space-x-1">
                    <Heart class="w-4 h-4" />
                    <span>{{ post.likeCount }}</span>
                  </span>
                  <span class="flex items-center space-x-1">
                    <MessageSquare class="w-4 h-4" />
                    <span>{{ post.commentCount }}</span>
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 分页 -->
            <div v-if="currentPosts.length > 0" class="flex justify-center mt-6 space-x-2">
              <button
                @click="changePage(currentPage - 1)"
                :disabled="currentPage === 1"
                class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                上一页
              </button>
              <span class="px-4 py-2 text-gray-700">第 {{ currentPage }} 页</span>
              <button
                @click="changePage(currentPage + 1)"
                :disabled="!hasMore"
                class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
        
        <div class="space-y-6">
          <div>
            <h3 class="font-semibold text-gray-900 mb-3">基本信息</h3>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-gray-500 mb-1">用户名</label>
                <input 
                  v-model="editForm.username"
                  type="text"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-500 mb-1">邮箱</label>
                <input 
                  v-model="editForm.email"
                  type="email"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4 mt-4">
              <div>
                <label class="block text-sm text-gray-500 mb-1">手机号</label>
                <input 
                  v-model="editForm.phoneNumber"
                  type="tel"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
            </div>
          </div>
          <div>
            <h3 class="font-semibold text-gray-900 mb-3">修改密码</h3>
            <div class="space-y-3">
              <div>
                <label class="block text-sm text-gray-500 mb-1">原密码</label>
                <input 
                  v-model="passwordForm.oldPassword"
                  type="password"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-500 mb-1">新密码</label>
                <input 
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-500 mb-1">确认新密码</label>
                <input 
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                />
              </div>
            </div>
          </div>
          <div class="flex justify-end space-x-3 pt-4">
            <button 
              @click="resetForm"
              class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              重置
            </button>
            <button 
              @click="handleUpdate"
              :disabled="loading"
              class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50"
            >
              保存修改
            </button>
          </div>
        </div>
        <div v-if="message" :class="['mt-4 p-3 rounded-lg text-sm', messageType === 'success' ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600']">
          {{ message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Loader2, Upload, Heart, MessageSquare } from 'lucide-vue-next'
import { getUserInfo, updateUserInfo, changePassword } from '../../api/user'
import { uploadAvatar } from '../../api/file'
import { setUser, getUserId } from '../../utils/auth'
import { getUserPosts, getUserLikedPosts, getUserCollectedPosts } from '../../api/post'

const user = ref({})
const loading = ref(false)
const message = ref('')
const messageType = ref('success')
const defaultAvatar = 'https://via.placeholder.com/150'

// 帖子相关
const activeTab = ref('my')
const tabs = [
  { key: 'my', label: '我的发布' },
  { key: 'liked', label: '我点赞的' },
  { key: 'collected', label: '我收藏的' }
]
const currentPosts = ref([])
const postsLoading = ref(false)
const currentPage = ref(1)
const hasMore = ref(true)
const pageSize = 10

const editForm = reactive({
  username: '',
  phoneNumber: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formatDate = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const loadUser = async () => {
  loading.value = true
  try {
    user.value = await getUserInfo()
    editForm.username = user.value.username
    editForm.phoneNumber = user.value.phoneNumber || ''
    editForm.email = user.value.email || ''
  } catch (e) {
    console.error('获取用户信息失败:', e)
  } finally {
    loading.value = false
  }
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  loading.value = true
  try {
    const data = await uploadAvatar(file)
    user.value.avatar = data.url
    await updateUserInfo({ avatar: data.url })
    setUser(user.value)
    showMessage('头像更新成功', 'success')
  } catch (e) {
    showMessage('头像上传失败', 'error')
    console.error('上传头像失败:', e)
  } finally {
    loading.value = false
  }
}

const handleUpdate = async () => {
  loading.value = true
  message.value = ''
  
  try {
    if (editForm.username !== user.value.username || 
        editForm.email !== (user.value.email || '') || 
        editForm.phoneNumber !== (user.value.phoneNumber || '')) {
      const updateData = {
        username: editForm.username,
        phoneNumber: editForm.phoneNumber
      }
      // 只有当邮箱不为空时才添加
      if (editForm.email) {
        updateData.email = editForm.email
      }
      const data = await updateUserInfo(updateData)
      user.value = data
      setUser(user.value)
      showMessage('基本信息更新成功', 'success')
    }
    
    if (passwordForm.oldPassword || passwordForm.newPassword || passwordForm.confirmPassword) {
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        showMessage('两次输入的密码不一致', 'error')
        return
      }
      await changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      showMessage('密码修改成功，请重新登录', 'success')
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    }
  } catch (e) {
    showMessage(e.message || '更新失败', 'error')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editForm.username = user.value.username
  editForm.phoneNumber = user.value.phoneNumber || ''
  editForm.email = user.value.email || ''
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  message.value = ''
}

const showMessage = (msg, type) => {
  message.value = msg
  messageType.value = type
}

// 帖子相关方法
const getTabLabel = () => {
  const tab = tabs.find(t => t.key === activeTab.value)
  return tab ? tab.label : ''
}

const goToPost = (postId) => {
  window.location.href = `/post/${postId}`
}

const loadPosts = async (page = 1) => {
  postsLoading.value = true
  try {
    const userId = getUserId()
    let data
    
    if (activeTab.value === 'my') {
      data = await getUserPosts(userId, { page, size: pageSize })
    } else if (activeTab.value === 'liked') {
      data = await getUserLikedPosts(userId, { page, size: pageSize })
    } else if (activeTab.value === 'collected') {
      data = await getUserCollectedPosts(userId, { page, size: pageSize })
    }
    
    currentPosts.value = data.records || []
    currentPage.value = page
    hasMore.value = currentPosts.value.length === pageSize
  } catch (e) {
    console.error('获取帖子失败:', e)
  } finally {
    postsLoading.value = false
  }
}

const changePage = (page) => {
  if (page < 1) return
  loadPosts(page)
}

// 监听标签切换
watch(activeTab, () => {
  currentPage.value = 1
  loadPosts(1)
})

onMounted(() => {
  loadUser()
  loadPosts(1)
})
</script>
