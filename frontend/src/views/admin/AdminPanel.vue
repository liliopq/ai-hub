<template>
  <div class="min-h-screen">
    <div class="max-w-6xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">管理后台</h1>

        <div class="flex space-x-4 mb-6">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            @click="switchTab(tab.key)"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition-colors',
              activeTab === tab.key
                ? 'bg-primary-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- ==================== 用户管理 ==================== -->
        <div v-if="activeTab === 'users'" class="space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <input
                v-model="userFilters.username"
                type="text"
                placeholder="搜索用户名"
                class="border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 outline-none"
              />
              <select
                v-model="userFilters.role"
                class="border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 outline-none"
              >
                <option value="">全部角色</option>
                <option value="USER">普通用户</option>
                <option value="ADMIN">管理员</option>
              </select>
              <select
                v-model="userFilters.status"
                class="border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 outline-none"
              >
                <option value="">全部状态</option>
                <option value="1">正常</option>
                <option value="0">封禁</option>
              </select>
              <button
                @click="searchUsers"
                class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                搜索
              </button>
            </div>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="bg-gray-50">
                  <th class="px-4 py-3 text-center text-sm font-medium text-gray-500 w-16">ID</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">用户名</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">邮箱</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">角色</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">状态</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">注册时间</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in users" :key="user.id" class="border-b border-gray-100">
                  <td class="px-4 py-3 text-center text-xs text-gray-400 font-mono w-16">{{ user.id }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center space-x-2">
                      <img :src="user.avatar || defaultAvatar" class="w-8 h-8 rounded-full" />
                      <span class="text-sm text-gray-900">{{ user.username }}</span>
                    </div>
                  </td>
                  <td class="px-4 py-3 text-sm text-gray-500">{{ user.email || '-' }}</td>
                  <td class="px-4 py-3">
                    <span :class="[
                      'px-2 py-1 rounded text-xs',
                      user.role === 'ADMIN' ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'
                    ]">
                      {{ user.role === 'ADMIN' ? '管理员' : '普通用户' }}
                    </span>
                  </td>
                  <td class="px-4 py-3">
                    <span :class="[
                      'px-2 py-1 rounded text-xs',
                      user.status === 1 ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'
                    ]">
                      {{ user.status === 1 ? '正常' : '封禁' }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-sm text-gray-500 whitespace-nowrap">{{ formatDate(user.createTime) }}</td>
                  <td class="px-4 py-3">
                    <button
                      @click="toggleUserStatus(user)"
                      :class="[
                        'px-3 py-1 rounded text-sm',
                        user.status === 1
                          ? 'bg-red-50 text-red-600 hover:bg-red-100'
                          : 'bg-green-50 text-green-600 hover:bg-green-100'
                      ]"
                    >
                      {{ user.status === 1 ? '封禁' : '解封' }}
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <!-- 用户分页 -->
          <div v-if="users.length > 0" class="flex items-center justify-between pt-4">
            <span class="text-sm text-gray-500">共 {{ userPagination.total }} 条，第 {{ userPagination.current }}/{{ userPagination.pages || 1 }} 页</span>
            <div class="flex items-center space-x-1">
              <button
                @click="goUserPage(userPagination.current - 1)"
                :disabled="userPagination.current <= 1"
                class="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
              >上一页</button>
              <button
                v-for="p in pageNumbers(userPagination.pages, userPagination.current)"
                :key="p"
                @click="typeof p === 'number' && goUserPage(p)"
                :class="[
                  'w-8 h-8 rounded text-sm',
                  p === userPagination.current ? 'bg-primary-600 text-white' : 'hover:bg-gray-100 text-gray-600',
                  typeof p !== 'number' ? 'cursor-default' : ''
                ]"
              >{{ p }}</button>
              <button
                @click="goUserPage(userPagination.current + 1)"
                :disabled="userPagination.current >= userPagination.pages"
                class="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
              >下一页</button>
            </div>
          </div>
          <div v-if="users.length === 0" class="text-center py-12">
            <Users class="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p class="text-gray-500">暂无用户</p>
          </div>
        </div>

        <!-- ==================== 帖子管理 ==================== -->
        <div v-if="activeTab === 'posts'" class="space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <select
                v-model="postFilters.status"
                class="border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 outline-none"
              >
                <option value="">全部状态</option>
                <option value="1">已发布</option>
                <option value="0">待审核</option>
              </select>
              <button
                @click="searchPosts"
                class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                搜索
              </button>
            </div>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="bg-gray-50">
                  <th class="px-4 py-3 text-center text-sm font-medium text-gray-500 w-16">ID</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">标题</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">作者</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">状态</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">创建时间</th>
                  <th class="px-4 py-3 text-left text-sm font-medium text-gray-500">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="post in posts" :key="post.id" class="border-b border-gray-100">
                  <td class="px-4 py-3 text-center text-xs text-gray-400 font-mono w-16">{{ post.id }}</td>
                  <td class="px-4 py-3 text-sm text-primary-600 hover:text-primary-700 underline cursor-pointer">
                    <router-link :to="`/post/${post.id}`">{{ post.title }}</router-link>
                  </td>
                  <td class="px-4 py-3 text-sm text-gray-500">{{ post.user?.username }}</td>
                  <td class="px-4 py-3">
                    <span :class="[
                      'px-2 py-1 rounded text-xs',
                      post.status === 1 ? 'bg-green-100 text-green-600' : 'bg-yellow-100 text-yellow-600'
                    ]">
                      {{ post.status === 1 ? '已发布' : '待审核' }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-sm text-gray-500 whitespace-nowrap">{{ formatDate(post.createTime) }}</td>
                  <td class="px-4 py-3">
                    <div class="flex items-center space-x-2">
                      <button
                        @click="handleAudit(post.id, 1)"
                        class="px-2 py-1 bg-green-50 text-green-600 rounded text-xs hover:bg-green-100"
                      >通过</button>
                      <button
                        @click="handleAudit(post.id, 0)"
                        class="px-2 py-1 bg-yellow-50 text-yellow-600 rounded text-xs hover:bg-yellow-100"
                      >驳回</button>
                      <button
                        @click="toggleSticky(post)"
                        class="px-2 py-1 bg-blue-50 text-blue-600 rounded text-xs hover:bg-blue-100"
                      >{{ post.isSticky ? '取消置顶' : '置顶' }}</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <!-- 帖子分页 -->
          <div v-if="posts.length > 0" class="flex items-center justify-between pt-4">
            <span class="text-sm text-gray-500">共 {{ postPagination.total }} 条，第 {{ postPagination.current }}/{{ postPagination.pages || 1 }} 页</span>
            <div class="flex items-center space-x-1">
              <button
                @click="goPostPage(postPagination.current - 1)"
                :disabled="postPagination.current <= 1"
                class="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
              >上一页</button>
              <button
                v-for="p in pageNumbers(postPagination.pages, postPagination.current)"
                :key="p"
                @click="typeof p === 'number' && goPostPage(p)"
                :class="[
                  'w-8 h-8 rounded text-sm',
                  p === postPagination.current ? 'bg-primary-600 text-white' : 'hover:bg-gray-100 text-gray-600',
                  typeof p !== 'number' ? 'cursor-default' : ''
                ]"
              >{{ p }}</button>
              <button
                @click="goPostPage(postPagination.current + 1)"
                :disabled="postPagination.current >= postPagination.pages"
                class="px-3 py-1.5 border rounded-lg text-sm hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
              >下一页</button>
            </div>
          </div>
          <div v-if="posts.length === 0" class="text-center py-12">
            <FileText class="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p class="text-gray-500">暂无帖子</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Users, FileText } from 'lucide-vue-next'
import { getUserList, updateUserStatus } from '../../api/admin'
import { getPostList, auditPost, setPostSticky } from '../../api/admin'

const tabs = [
  { key: 'users', label: '用户管理' },
  { key: 'posts', label: '帖子管理' }
]

const activeTab = ref('users')
const users = ref([])
const posts = ref([])
const defaultAvatar = 'https://via.placeholder.com/150'

const userFilters = reactive({
  page: 1, size: 10,
  username: '', role: '', status: ''
})
const userPagination = reactive({ current: 1, pages: 0, total: 0 })

const postFilters = reactive({
  page: 1, size: 10,
  status: ''
})
const postPagination = reactive({ current: 1, pages: 0, total: 0 })

const formatDate = (time) => {
  if (!time) return ''
  const date = new Date(time.indexOf('+') === -1 && time.indexOf('Z') === -1 ? time + '+08:00' : time)
  return date.toLocaleString('zh-CN')
}

// 生成页码数组，超多页时省略中间
const pageNumbers = (totalPages, current) => {
  if (!totalPages || totalPages <= 1) return []
  if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i + 1)
  const pages = [1]
  const start = Math.max(2, current - 1)
  const end = Math.min(totalPages - 1, current + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  if (pages[pages.length - 1] < totalPages - 1) pages.push('…')
  pages.push(totalPages)
  return pages
}

const switchTab = (key) => {
  activeTab.value = key
  if (key === 'users') { userFilters.page = 1; loadUsers() }
  else { postFilters.page = 1; loadPosts() }
}

const searchUsers = () => { userFilters.page = 1; loadUsers() }
const searchPosts = () => { postFilters.page = 1; loadPosts() }

const loadUsers = async () => {
  try {
    const params = { page: userFilters.page, size: userFilters.size }
    if (userFilters.username) params.username = userFilters.username
    if (userFilters.role) params.role = userFilters.role
    if (userFilters.status) params.status = parseInt(userFilters.status)
    const data = await getUserList(params)
    users.value = data.records
    Object.assign(userPagination, { total: data.total, current: data.current, pages: data.pages })
  } catch (e) { console.error('获取用户列表失败:', e) }
}

const goUserPage = (page) => {
  if (page < 1 || page > userPagination.pages) return
  userFilters.page = page
  loadUsers()
}

const loadPosts = async () => {
  try {
    const params = { page: postFilters.page, size: postFilters.size }
    if (postFilters.status) params.status = parseInt(postFilters.status)
    const data = await getPostList(params)
    posts.value = data.records
    Object.assign(postPagination, { total: data.total, current: data.current, pages: data.pages })
  } catch (e) { console.error('获取帖子列表失败:', e) }
}

const goPostPage = (page) => {
  if (page < 1 || page > postPagination.pages) return
  postFilters.page = page
  loadPosts()
}

const toggleUserStatus = async (user) => {
  try {
    await updateUserStatus(user.id, { status: user.status === 1 ? 0 : 1 })
    user.status = user.status === 1 ? 0 : 1
  } catch (e) { console.error('更新用户状态失败:', e) }
}

const handleAudit = async (postId, status) => {
  if (status === 2 && !confirm('确定要删除这篇帖子吗？')) return
  try { await auditPost(postId, { status }); loadPosts() }
  catch (e) { console.error('审核帖子失败:', e) }
}

const toggleSticky = async (post) => {
  try {
    await setPostSticky(post.id, { sticky: post.isSticky ? 0 : 1 })
    post.isSticky = post.isSticky ? 0 : 1
  } catch (e) { console.error('设置置顶失败:', e) }
}

onMounted(() => { loadUsers() })
</script>
