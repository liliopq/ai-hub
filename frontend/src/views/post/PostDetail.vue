<template>
  <div class="min-h-screen">
    <div v-if="loading" class="text-center py-12">
      <Loader2 class="w-8 h-8 text-primary-600 animate-spin mx-auto" />
    </div>
    <div v-else-if="post" class="flex gap-6">
      <div class="flex-1">
        <div class="bg-white rounded-xl shadow-sm p-8 mb-6">
          <div class="flex items-start justify-between mb-6">
            <div class="flex items-start space-x-4">
              <router-link :to="`/user/${post.user?.id}`" class="cursor-pointer">
                <img 
                  :src="post.user?.avatar || defaultAvatar" 
                  :alt="post.user?.username"
                  class="w-12 h-12 rounded-full object-cover hover:ring-2 hover:ring-primary-500 transition-all"
                />
              </router-link>
              <div>
                <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ post.title }}</h2>
                <div class="flex items-center space-x-4 text-sm text-gray-500">
                  <router-link :to="`/user/${post.user?.id}`" class="text-gray-500 hover:text-primary-600">
                    <span>{{ post.user?.username }}</span>
                  </router-link>
                  <span>{{ formatTime(post.createTime) }}</span>
                  <span class="px-2 py-0.5 bg-gray-100 text-gray-600 rounded">{{ post.category }}</span>
                </div>
              </div>
            </div>
            <div v-if="canEdit" class="flex items-center space-x-2">
              <router-link 
                :to="`/post/edit/${post.id}`"
                class="px-3 py-1.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm"
              >
                <Pencil class="w-4 h-4 inline mr-1" />
                编辑
              </router-link>
              <button 
                @click="handleDelete"
                class="px-3 py-1.5 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors text-sm"
              >
                <Trash2 class="w-4 h-4 inline mr-1" />
                删除
              </button>
            </div>
          </div>
          <div class="prose prose-lg max-w-none mb-6">
            <p class="text-gray-700 whitespace-pre-wrap">{{ post.content }}</p>
          </div>
          <div class="flex items-center space-x-4">
            <span 
              v-for="tag in post.tags" 
              :key="tag"
              class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-sm"
            >
              {{ tag }}
            </span>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
          <div class="flex items-center space-x-8">
            <button 
              @click="handleLike"
              :class="[
                'flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors',
                post.isLiked ? 'text-red-500 bg-red-50' : 'text-gray-600 hover:bg-gray-100'
              ]"
            >
              <Heart :class="['w-5 h-5', post.isLiked ? 'fill-current' : '']" />
              <span>{{ post.likeCount }}</span>
            </button>
            <button 
              @click="handleCollect"
              :class="[
                'flex items-center space-x-2 px-4 py-2 rounded-lg transition-colors',
                post.isCollected ? 'text-primary-500 bg-primary-50' : 'text-gray-600 hover:bg-gray-100'
              ]"
            >
              <Bookmark :class="['w-5 h-5', post.isCollected ? 'fill-current' : '']" />
              <span>{{ post.isCollected ? '已收藏' : '收藏' }} {{ post.collectCount || '' }}</span>
            </button>
            <div class="flex items-center space-x-2 text-gray-600">
              <Eye class="w-5 h-5" />
              <span>{{ post.viewCount }}</span>
            </div>
            <div class="flex items-center space-x-2 text-gray-600">
              <MessageSquare class="w-5 h-5" />
              <span>{{ post.commentCount }}</span>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm p-6">
          <h3 class="font-semibold text-gray-900 mb-4">评论 ({{ commentsTotal }})</h3>
          <div v-if="isLoggedIn" class="mb-6">
            <textarea 
              v-model="newComment"
              placeholder="写下你的评论..."
              class="w-full border border-gray-300 rounded-lg p-4 resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              rows="3"
            ></textarea>
            <div class="flex justify-end mt-3">
              <button 
                @click="submitComment"
                :disabled="!newComment.trim()"
                class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                发表评论
              </button>
            </div>
          </div>
          <div class="space-y-4">
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              class="border-b border-gray-100 pb-4"
            >
              <div class="flex items-start space-x-3">
                <div 
                  :class="isLoggedIn ? 'cursor-pointer' : ''"
                  @click.stop="handleUserClick(comment.user?.id)"
                >
                  <img 
                    :src="comment.user?.avatar || defaultAvatar" 
                    :alt="comment.user?.username"
                    class="w-8 h-8 rounded-full object-cover hover:ring-2 hover:ring-primary-500 transition-all"
                  />
                </div>
                <div class="flex-1">
                  <div class="flex items-center space-x-2 mb-1">
                    <span 
                      :class="isLoggedIn ? 'cursor-pointer hover:text-primary-600' : ''"
                      @click="handleUserClick(comment.user?.id)"
                      class="font-medium text-gray-900"
                    >
                      {{ comment.user?.username }}
                    </span>
                    <span class="text-sm text-gray-400">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <p class="text-gray-700 mb-2">{{ comment.content }}</p>
                  <div class="flex items-center space-x-4">
                    <button 
                      @click="handleCommentLike(comment)"
                      :disabled="!isLoggedIn"
                      :class="!isLoggedIn ? 'opacity-50 cursor-not-allowed' : ''"
                      class="flex items-center space-x-1 text-sm text-gray-500 hover:text-red-500 transition-colors disabled:cursor-not-allowed"
                    >
                      <Heart class="w-4 h-4" />
                      <span>{{ comment.likeCount }}</span>
                    </button>
                    <button 
                      v-if="isLoggedIn"
                      @click="replyToComment(comment)"
                      class="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                    >
                      回复
                    </button>
                    <button 
                      v-if="canDeleteComment(comment)"
                      @click="deleteCommentItem(comment.id)"
                      class="text-sm text-red-500 hover:text-red-600 transition-colors"
                    >
                      删除
                    </button>
                  </div>
                  <div 
                    v-if="replyingId === comment.id"
                    class="mt-3 pl-4 border-l-2 border-primary-200"
                  >
                    <textarea 
                      v-model="replyContent"
                      placeholder="回复 {{ comment.user?.username }}..."
                      class="w-full border border-gray-300 rounded-lg p-3 resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none text-sm"
                      rows="2"
                    ></textarea>
                    <div class="flex justify-end space-x-2 mt-2">
                      <button 
                        @click="replyingId = null"
                        class="px-3 py-1 text-sm text-gray-600 hover:bg-gray-100 rounded"
                      >
                        取消
                      </button>
                      <button 
                        @click="submitReply(comment.id)"
                        :disabled="!replyContent.trim()"
                        class="px-3 py-1 bg-primary-600 text-white text-sm rounded hover:bg-primary-700 disabled:opacity-50"
                      >
                        回复
                      </button>
                    </div>
                  </div>
                  <div v-if="comment.replies && comment.replies.length > 0" class="mt-3 pl-4 border-l-2 border-gray-100">
                    <div 
                        v-for="reply in comment.replies" 
                        :key="reply.id"
                        class="flex items-start space-x-3 mt-3"
                      >
                        <div 
                          :class="isLoggedIn ? 'cursor-pointer' : ''"
                          @click.stop="handleUserClick(reply.user?.id)"
                        >
                          <img 
                            :src="reply.user?.avatar || defaultAvatar" 
                            :alt="reply.user?.username"
                            class="w-6 h-6 rounded-full object-cover hover:ring-2 hover:ring-primary-500 transition-all"
                          />
                        </div>
                        <div class="flex-1">
                          <div class="flex items-center space-x-2 mb-1">
                            <span 
                              :class="isLoggedIn ? 'cursor-pointer hover:text-primary-600' : ''"
                              @click="handleUserClick(reply.user?.id)"
                              class="font-medium text-gray-900 text-sm"
                            >
                              {{ reply.user?.username }}
                            </span>
                          <span class="text-xs text-gray-400">{{ formatTime(reply.createTime) }}</span>
                        </div>
                        <p class="text-gray-700 text-sm">{{ reply.content }}</p>
                        <div class="flex items-center space-x-4 mt-1">
                          <button 
                            @click="handleCommentLike(reply)"
                            :disabled="!isLoggedIn"
                            :class="!isLoggedIn ? 'opacity-50 cursor-not-allowed' : ''"
                            class="flex items-center space-x-1 text-xs text-gray-500 hover:text-red-500 disabled:cursor-not-allowed"
                          >
                            <Heart class="w-3 h-3" />
                            <span>{{ reply.likeCount }}</span>
                          </button>
                          <button 
                            v-if="canDeleteComment(reply)"
                            @click="deleteCommentItem(reply.id)"
                            class="text-xs text-red-500 hover:text-red-600"
                          >
                            删除
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="comments.length === 0" class="text-center py-8">
            <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-2" />
            <p class="text-gray-500">暂无评论</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Loader2, Heart, Bookmark, Eye, MessageSquare, 
  Pencil, Trash2 
} from 'lucide-vue-next'
import { getPostDetail, likePost, collectPost, deletePost } from '../../api/post'
import { createComment, getCommentList, deleteComment, likeComment } from '../../api/comment'
import { getUser, isLoggedIn as checkIsLoggedIn, isAdmin } from '../../utils/auth'

const route = useRoute()
const postId = route.params.id

const post = ref(null)
const loading = ref(false)
const comments = ref([])
const commentsTotal = ref(0)
const newComment = ref('')
const replyingId = ref(null)
const replyContent = ref('')
const defaultAvatar = 'https://via.placeholder.com/150'

const currentUser = getUser()
const isLoggedIn = computed(() => checkIsLoggedIn())

const canEdit = computed(() => {
  if (!post.value || !currentUser) return false
  return post.value.user?.id === currentUser.id || isAdmin()
})

const canDeleteComment = (comment) => {
  if (!currentUser) return false
  return comment.user?.id === currentUser.id || isAdmin()
}

const formatTime = (time) => {
  // 后端返回的是 Asia/Shanghai 时间，但 JavaScript 默认按 UTC 解析
  // 手动追加 +08:00 时区偏移确保正确
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

const loadPost = async () => {
  loading.value = true
  try {
    post.value = await getPostDetail(postId)
    await loadComments()
  } catch (e) {
    console.error('获取帖子详情失败:', e)
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const data = await getCommentList(postId, { page: 1, size: 20 })
    comments.value = data.records
    commentsTotal.value = data.total
  } catch (e) {
    console.error('获取评论失败:', e)
  }
}

const handleLike = async () => {
  if (!isLoggedIn.value) {
    alert('请先登录后再点赞')
    return
  }
  try {
    const action = post.value.isLiked ? 'unlike' : 'like'
    const data = await likePost(postId, action)
    post.value.likeCount = data.likeCount
    post.value.isLiked = data.isLiked
  } catch (e) {
    console.error('点赞失败:', e)
    alert(e.message || '操作失败')
  }
}

const handleCollect = async () => {
  if (!isLoggedIn.value) {
    alert('请先登录后再收藏')
    return
  }
  try {
    const action = post.value.isCollected ? 'uncollect' : 'collect'
    const data = await collectPost(postId, action)
    post.value.isCollected = data.isCollected
    post.value.collectCount = data.collectCount
  } catch (e) {
    console.error('收藏失败:', e)
    alert(e.message || '操作失败')
  }
}

const handleDelete = async () => {
  if (!confirm('确定要删除这篇帖子吗？')) return
  try {
    await deletePost(postId)
    window.location.href = '/'
  } catch (e) {
    console.error('删除失败:', e)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await createComment({
      postId: parseInt(postId),
      parentId: 0,
      content: newComment.value
    })
    newComment.value = ''
    await loadComments()
  } catch (e) {
    console.error('发表评论失败:', e)
  }
}

const replyToComment = (comment) => {
  replyingId.value = comment.id
}

const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) return
  try {
    await createComment({
      postId: parseInt(postId),
      parentId,
      content: replyContent.value
    })
    replyContent.value = ''
    replyingId.value = null
    await loadComments()
  } catch (e) {
    console.error('回复失败:', e)
  }
}

const handleCommentLike = async (comment) => {
  if (!isLoggedIn.value) {
    alert('请先登录后再点赞')
    return
  }
  try {
    const data = await likeComment(comment.id)
    comment.likeCount = data.likeCount
  } catch (e) {
    console.error('评论点赞失败:', e)
  }
}

const deleteCommentItem = async (commentId) => {
  if (!confirm('确定要删除这条评论吗？')) return
  try {
    await deleteComment(commentId)
    await loadComments()
  } catch (e) {
    console.error('删除评论失败:', e)
  }
}

// 处理用户头像点击
const handleUserClick = (userId) => {
  if (!isLoggedIn.value) {
    alert('请先登录后再查看用户主页')
    return
  }
  window.location.href = `/user/${userId}`
}

onMounted(() => {
  loadPost()
})
</script>
