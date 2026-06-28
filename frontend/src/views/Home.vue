<template>
  <div class="min-h-screen">
    <div class="flex gap-6">
      <div class="flex-1">
        <div class="flex items-center justify-between mb-6">
          <h1 class="text-2xl font-bold text-gray-900">社区帖子</h1>
          <div class="flex items-center space-x-2">
            <select 
              v-model="filters.sortBy" 
              @change="handleFilterChange"
              class="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:ring-2 focus:ring-primary-500 outline-none"
            >
              <option value="time">最新</option>
              <option value="hot">最热</option>
            </select>
          </div>
        </div>
        
        <!-- 搜索框 -->
        <div class="mb-6">
          <div class="relative">
            <input 
              v-model="filters.keyword"
              @keyup.enter="handleFilterChange"
              type="text"
              placeholder="搜索帖子标题或内容..."
              class="w-full border border-gray-300 rounded-lg px-4 py-2 pr-10 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
            />
            <Search class="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
          </div>
          <div v-if="filters.keyword" class="mt-2 flex items-center justify-between">
            <span class="text-sm text-gray-500">搜索关键词: {{ filters.keyword }}</span>
            <button 
              @click="clearKeyword"
              class="text-sm text-primary-600 hover:text-primary-700"
            >
              清除
            </button>
          </div>
        </div>
        
        <!-- 分类筛选 -->
        <div class="mb-4">
          <h3 class="text-sm font-medium text-gray-700 mb-2">分类</h3>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="cat in categories" 
              :key="cat"
              @click="handleCategoryClick(cat)"
              :class="[
                'px-4 py-1.5 rounded-full text-sm transition-colors',
                filters.category === cat 
                  ? 'bg-primary-600 text-white' 
                  : 'bg-white border border-gray-300 text-gray-600 hover:bg-gray-50'
              ]"
            >
              {{ cat }}
            </button>
          </div>
        </div>
        
        <!-- 标签筛选（多选） -->
        <div class="mb-6">
          <div class="flex items-center justify-between mb-2">
            <h3 class="text-sm font-medium text-gray-700">标签（可多选）</h3>
            <button
              v-if="filters.tags.length > 0"
              @click="clearTags"
              class="text-xs text-primary-600 hover:text-primary-700"
            >
              清除全部
            </button>
          </div>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in allTags"
              :key="tag"
              @click="handleTagClick(tag)"
              :class="[
                'px-3 py-1 rounded-full text-sm transition-colors',
                filters.tags.includes(tag)
                  ? 'bg-blue-600 text-white'
                  : 'bg-blue-50 text-blue-600 hover:bg-blue-100 border border-blue-200'
              ]"
            >
              #{{ tag }}
            </button>
            <span v-if="allTags.length === 0" class="text-sm text-gray-400">暂无标签</span>
          </div>
        </div>
        <div class="space-y-4">
          <div 
            v-for="post in posts" 
            :key="post.id" 
            class="bg-white rounded-xl shadow-sm p-6 card-hover"
            :class="isLoggedIn ? 'cursor-pointer' : ''"
            @click="handlePostClick(post.id)"
          >
            <div class="flex items-start space-x-4">
              <div 
                :class="isLoggedIn ? 'cursor-pointer' : ''"
                @click.stop="handleUserClick(post.user?.id)"
              >
                <img 
                  :src="post.user?.avatar || defaultAvatar" 
                  :alt="post.user?.username"
                  class="w-10 h-10 rounded-full object-cover hover:ring-2 hover:ring-primary-500 transition-all"
                />
              </div>
              <div class="flex-1">
                <div class="flex items-center space-x-2 mb-1">
                  <span class="font-medium text-gray-900">{{ post.user?.username }}</span>
                  <span class="text-sm text-gray-400">{{ formatTime(post.createTime) }}</span>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-2 hover:text-primary-600 transition-colors">
                  {{ post.title }}
                </h3>
                <div class="flex items-center space-x-4 text-sm text-gray-500">
                  <span class="flex items-center">
                    <Eye class="w-4 h-4 mr-1" />
                    {{ post.viewCount }}
                  </span>
                  <span class="flex items-center">
                    <Heart class="w-4 h-4 mr-1" />
                    {{ post.likeCount }}
                  </span>
                  <span class="flex items-center">
                    <MessageSquare class="w-4 h-4 mr-1" />
                    {{ post.commentCount }}
                  </span>
                </div>
                <div class="flex items-center space-x-2 mt-3">
                  <span class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs">{{ post.category }}</span>
                  <span 
                    v-for="tag in post.tags" 
                    :key="tag"
                    class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-xs"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="loading" class="text-center py-8">
          <Loader2 class="w-8 h-8 text-primary-600 animate-spin mx-auto" />
        </div>
        <div v-if="!loading && posts.length === 0" class="text-center py-12">
          <FileText class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p class="text-gray-500">暂无帖子</p>
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
      <div class="w-64 hidden lg:block">
        <div class="bg-white rounded-xl shadow-sm p-6 sticky top-24">
          <h3 class="font-semibold text-gray-900 mb-4">热门话题</h3>
          <div class="space-y-3">
            <div 
              v-for="topic in categories" 
              :key="topic"
              :class="isLoggedIn ? 'cursor-pointer hover:bg-gray-50' : ''"
              class="flex items-center space-x-2 p-2 rounded-lg transition-colors"
              @click="handleTopicClick(topic)"
            >
              <Hash class="w-4 h-4 text-gray-400" />
              <span class="text-sm text-gray-600">{{ topic }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Eye, Heart, MessageSquare, Loader2, FileText, Hash, Search, X } from 'lucide-vue-next'
import { getPostList, getAllTags } from '../api/post'
import { isLoggedIn as checkLoggedIn } from '../utils/auth'

const posts = ref([])
const loading = ref(false)
const hasMore = ref(true)
const defaultAvatar = 'https://via.placeholder.com/150'
const allTags = ref([])
const isLoggedIn = computed(() => checkLoggedIn())

const filters = reactive({
  page: 1,
  size: 10,
  category: '',
  tags: [],
  keyword: '',
  sortBy: 'time'
})

const categories = ['技术分享', 'AI讨论', '资源推荐', '经验交流', '其他']

const formatTime = (time) => {
  // 后端返回 Asia/Shanghai 时间，手动加时区偏移避免 JS 按 UTC 解析
  const date = new Date(time.indexOf('+') === -1 && time.indexOf('Z') === -1 ? time + '+08:00' : time)
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

const loadPosts = async (reset = false) => {
  if (loading.value) return
  
  loading.value = true
  
  if (reset) {
    filters.page = 1
    posts.value = []
    hasMore.value = true
  }
  
  try {
    const params = {
      page: filters.page,
      size: filters.size
    }
    if (filters.category) params.category = filters.category
    if (filters.tags.length > 0) params.tag = filters.tags.join(',')
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.sortBy) params.sortBy = filters.sortBy
    
    const data = await getPostList(params)
    
    posts.value = [...posts.value, ...data.records]
    hasMore.value = data.current < data.pages
    filters.page++
  } catch (e) {
    console.error('获取帖子列表失败:', e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  loadPosts()
}

const handleFilterChange = () => {
  loadPosts(true)
}

const handleCategoryClick = (category) => {
  filters.category = filters.category === category ? '' : category
  loadPosts(true)
}

const handleTagClick = (tag) => {
  const idx = filters.tags.indexOf(tag)
  if (idx >= 0) {
    filters.tags.splice(idx, 1)
  } else {
    filters.tags.push(tag)
  }
  loadPosts(true)
}

const clearTags = () => {
  filters.tags = []
  loadPosts(true)
}

const clearKeyword = () => {
  filters.keyword = ''
  loadPosts(true)
}

const loadTags = async () => {
  try {
    allTags.value = await getAllTags()
  } catch (e) {
    console.error('获取标签失败:', e)
  }
}

// 处理帖子点击
const handlePostClick = (postId) => {
  if (!isLoggedIn.value) {
    alert('请先登录后再查看帖子详情')
    return
  }
  window.location.href = `/post/${postId}`
}

// 处理用户头像点击
const handleUserClick = (userId) => {
  if (!isLoggedIn.value) {
    alert('请先登录后再查看用户主页')
    return
  }
  window.location.href = `/user/${userId}`
}

// 处理话题点击
const handleTopicClick = (topic) => {
  if (!isLoggedIn.value) {
    alert('请先登录后再进行筛选操作')
    return
  }
  filters.category = filters.category === topic ? '' : topic
  loadPosts(true)
}

onMounted(() => {
  loadPosts()
  loadTags()
})
</script>
