<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 用户信息头部 -->
    <div class="bg-white shadow-sm">
      <div class="max-w-6xl mx-auto px-4 py-8">
        <div class="flex flex-col md:flex-row items-center">
          <img 
            :src="user?.avatar || defaultAvatar" 
            :alt="user?.username"
            class="w-24 h-24 rounded-full object-cover mb-4 md:mb-0 md:mr-8"
          />
          <div class="flex-1 text-center md:text-left">
            <h1 class="text-2xl font-bold text-gray-900">{{ user?.username }}</h1>
            <p class="text-gray-600 mt-2">
              <span class="inline-block px-2 py-1 bg-gray-100 rounded text-sm">
                {{ user?.role === 'ADMIN' ? '管理员' : user?.role === 'CREATOR' ? '创作者' : '普通用户' }}
              </span>
            </p>
          </div>
          <div class="mt-4 md:mt-0 text-center">
            <div class="flex space-x-6 mb-4">
              <div class="text-center w-16">
                <p class="text-2xl font-bold text-gray-900">{{ userPosts.total }}</p>
                <p class="text-xs text-gray-500">帖子</p>
              </div>
              <button @click="showFollowList('following')" class="text-center w-16 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer">
                <p class="text-2xl font-bold text-gray-900">{{ followStats.followingCount }}</p>
                <p class="text-xs text-gray-500">关注</p>
              </button>
              <button @click="showFollowList('followers')" class="text-center w-16 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer">
                <p class="text-2xl font-bold text-gray-900">{{ followStats.followerCount }}</p>
                <p class="text-xs text-gray-500">粉丝</p>
              </button>
            </div>
            <button
              v-if="isLoggedIn && !isOwnProfile"
              @click="toggleFollow"
              :class="[
                'px-6 py-2 rounded-lg font-medium transition-colors',
                isFollowing
                  ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  : 'bg-primary-600 text-white hover:bg-primary-700'
              ]"
              :disabled="followLoading"
            >
              {{ followLoading ? '处理中...' : (isFollowing ? '取消关注' : '+ 关注') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户帖子列表 -->
    <div class="max-w-6xl mx-auto px-4 py-8">
      <h2 class="text-xl font-semibold text-gray-900 mb-6">发布的帖子</h2>
      
      <div v-if="userPosts.records.length > 0" class="space-y-4">
        <div 
          v-for="post in userPosts.records" 
          :key="post.id"
          class="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow cursor-pointer"
          @click="goToPost(post.id)"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <h3 class="text-lg font-medium text-gray-900 hover:text-primary-600 transition-colors">
                {{ post.title }}
              </h3>
              <p class="text-gray-600 mt-2 line-clamp-2">{{ post.content }}</p>
              <div class="flex items-center space-x-4 mt-4 text-sm text-gray-500">
                <span>{{ post.category }}</span>
                <span>浏览 {{ post.viewCount }}</span>
                <span>点赞 {{ post.likeCount }}</span>
                <span>评论 {{ post.commentCount }}</span>
                <span>{{ formatDate(post.createTime) }}</span>
              </div>
              <div class="flex flex-wrap gap-2 mt-3">
                <span 
                  v-for="tag in post.tags" 
                  :key="tag"
                  class="px-2 py-1 bg-primary-50 text-primary-600 rounded text-xs"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12">
        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <FileText class="w-8 h-8 text-gray-400" />
        </div>
        <p class="text-gray-500">该用户还没有发布帖子</p>
      </div>

      <!-- 分页 -->
      <div v-if="userPosts.total > userPosts.size" class="mt-8 flex justify-center">
        <nav class="flex items-center space-x-2">
          <button 
            @click="prevPage"
            :disabled="currentPage === 1"
            class="px-4 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            上一页
          </button>
          <span class="px-4 py-2 text-gray-600">{{ currentPage }} / {{ totalPages }}</span>
          <button 
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="px-4 py-2 border rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            下一页
          </button>
        </nav>
      </div>
    </div>

    <!-- 关注/粉丝列表弹窗 -->
    <div v-if="followListModal" class="fixed inset-0 z-50 overflow-y-auto" @click.self="followListModal = false">
      <div class="flex items-center justify-center min-h-screen px-4">
        <div class="fixed inset-0 bg-black bg-opacity-40 transition-opacity"></div>
        <div class="relative bg-white rounded-xl shadow-xl max-w-md w-full p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900">
              {{ followListType === 'following' ? (isOwnProfile ? '我的关注' : 'TA的关注') : (isOwnProfile ? '我的粉丝' : 'TA的粉丝') }}
            </h3>
            <button @click="followListModal = false" class="text-gray-400 hover:text-gray-600">
              <X class="w-5 h-5" />
            </button>
          </div>
          <div v-if="followListLoading" class="text-center py-8">
            <Loader2 class="w-6 h-6 text-primary-600 animate-spin mx-auto" />
          </div>
          <div v-else-if="followList.length === 0" class="text-center py-8 text-gray-500">
            {{ followListType === 'following' ? '还没有关注任何人' : '还没有粉丝' }}
          </div>
          <div v-else class="space-y-3 max-h-96 overflow-y-auto">
            <div
              v-for="item in followList"
              :key="item.id"
              class="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-50 cursor-pointer"
              @click="goToUser(item.id)"
            >
              <img :src="item.avatar || defaultAvatar" :alt="item.username" class="w-10 h-10 rounded-full object-cover" />
              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900">{{ item.username }}</p>
                <p class="text-xs text-gray-400">
                  <span class="inline-block px-1.5 py-0.5 bg-gray-100 rounded text-xs">
                    {{ item.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { FileText, X, Loader2 } from 'lucide-vue-next'
import { getUserById } from '../../api/user'
import { getUserPosts } from '../../api/post'
import { followUser, unfollowUser, checkFollowing, getFollowCounts, getFollowingList, getFollowerList } from '../../api/follow'
import { getUser } from '../../utils/auth'

const route = useRoute()
const router = useRouter()
const userId = ref(Number(route.params.userId))
const user = ref(null)
const userPosts = ref({ records: [], total: 0, size: 10, current: 1 })
const currentPage = ref(1)
const defaultAvatar = 'https://via.placeholder.com/150'

// 关注相关
const followStats = ref({ followingCount: 0, followerCount: 0 })
const isFollowing = ref(false)
const followLoading = ref(false)
const isLoggedIn = computed(() => !!getUser())
const isOwnProfile = computed(() => {
  const currentUser = getUser()
  return currentUser && currentUser.id === userId.value
})

// 关注/粉丝列表弹窗
const followListModal = ref(false)
const followListType = ref('following')
const followList = ref([])
const followListLoading = ref(false)

const totalPages = computed(() => {
  return Math.ceil(userPosts.value.total / userPosts.value.size)
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  // 后端返回 Asia/Shanghai 时间，手动加时区偏移
  const date = new Date(dateStr.indexOf('+') === -1 && dateStr.indexOf('Z') === -1 ? dateStr + '+08:00' : dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const goToPost = (postId) => {
  router.push(`/post/${postId}`)
}

const showFollowList = async (type) => {
  followListType.value = type
  followListModal.value = true
  followListLoading.value = true
  followList.value = []
  try {
    const data = type === 'following'
      ? await getFollowingList(userId.value)
      : await getFollowerList(userId.value)
    followList.value = data
  } catch (e) {
    console.error('获取列表失败:', e)
  } finally {
    followListLoading.value = false
  }
}

const goToUser = (uid) => {
  followListModal.value = false
  router.push(`/user/${uid}`)
}

const fetchUserInfo = async () => {
  try {
    const data = await getUserById(userId.value)
    user.value = data
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

const fetchUserPostsData = async (page = 1) => {
  try {
    const data = await getUserPosts(userId.value, { page, size: 10 })
    userPosts.value = data
    currentPage.value = page
  } catch (e) {
    console.error('获取用户帖子失败:', e)
  }
}

const fetchFollowStats = async () => {
  try {
    const data = await getFollowCounts(userId.value)
    followStats.value = data
  } catch (e) {
    console.error('获取关注统计失败:', e)
  }
}

const fetchFollowingStatus = async () => {
  if (!isLoggedIn.value || isOwnProfile.value) return
  try {
    const data = await checkFollowing(userId.value)
    isFollowing.value = data.isFollowing
  } catch (e) {
    console.error('检查关注状态失败:', e)
  }
}

const toggleFollow = async () => {
  if (followLoading.value) return
  followLoading.value = true
  
  try {
    if (isFollowing.value) {
      await unfollowUser(userId.value)
      isFollowing.value = false
      followStats.value.followerCount = Math.max(0, followStats.value.followerCount - 1)
    } else {
      await followUser(userId.value)
      isFollowing.value = true
      followStats.value.followerCount++
    }
  } catch (e) {
    console.error('关注操作失败:', e)
    alert('操作失败，请重试')
  } finally {
    followLoading.value = false
  }
}

const prevPage = () => {
  if (currentPage.value > 1) {
    fetchUserPostsData(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    fetchUserPostsData(currentPage.value + 1)
  }
}

onMounted(() => {
  fetchUserInfo()
  fetchUserPostsData()
  fetchFollowStats()
  fetchFollowingStatus()
})
</script>
