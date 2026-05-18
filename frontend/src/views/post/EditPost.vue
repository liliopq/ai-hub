<template>
  <div class="min-h-screen">
    <div v-if="loading" class="text-center py-12">
      <Loader2 class="w-8 h-8 text-primary-600 animate-spin mx-auto" />
    </div>
    <div v-else class="max-w-3xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-6">编辑帖子</h1>
        <form @submit.prevent="handleSubmit">
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">标题</label>
            <input 
              v-model="form.title"
              type="text" 
              placeholder="请输入帖子标题"
              class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              required
            />
          </div>
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">分类</label>
            <select 
              v-model="form.category"
              class="w-full border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              required
            >
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">标签</label>
            <div class="flex flex-wrap gap-2 mb-2">
              <span 
                v-for="tag in form.tags" 
                :key="tag"
                class="flex items-center space-x-1 px-2 py-1 bg-blue-50 text-blue-600 rounded-full text-sm"
              >
                <span>{{ tag }}</span>
                <button 
                  type="button"
                  @click="removeTag(tag)"
                  class="hover:text-blue-800"
                >
                  <X class="w-4 h-4" />
                </button>
              </span>
            </div>
            <div class="flex items-center space-x-2">
              <input 
                v-model="newTag"
                type="text" 
                placeholder="输入标签后按回车添加"
                class="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                @keydown.enter.prevent="addTag"
              />
              <button 
                type="button"
                @click="addTag"
                class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
              >
                <Plus class="w-5 h-5" />
              </button>
            </div>
          </div>
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-2">内容</label>
            <textarea 
              v-model="form.content"
              placeholder="写下你的内容..."
              class="w-full border border-gray-300 rounded-lg px-4 py-3 resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
              rows="10"
              required
            ></textarea>
          </div>
          <div class="flex justify-end space-x-3">
            <button 
              type="button"
              @click="$router.back()"
              class="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button 
              type="submit"
              :disabled="loading"
              class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="flex items-center">
                <Loader2 class="w-5 h-5 animate-spin mr-2" />
                保存中...
              </span>
              <span v-else>保存修改</span>
            </button>
          </div>
        </form>
        <div v-if="error" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
          {{ error }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { X, Plus, Loader2 } from 'lucide-vue-next'
import { getPostDetail, updatePost } from '../../api/post'

const route = useRoute()
const postId = route.params.id

const categories = ['技术分享', 'AI讨论', '资源推荐', '经验交流', '其他']

const form = reactive({
  title: '',
  content: '',
  category: '',
  tags: []
})

const newTag = ref('')
const loading = ref(false)
const error = ref('')

const addTag = () => {
  const tag = newTag.value.trim()
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag)
    newTag.value = ''
  }
}

const removeTag = (tag) => {
  form.tags = form.tags.filter(t => t !== tag)
}

const loadPost = async () => {
  loading.value = true
  try {
    const data = await getPostDetail(postId)
    form.title = data.title
    form.content = data.content
    form.category = data.category
    form.tags = data.tags || []
  } catch (e) {
    console.error('获取帖子详情失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  
  try {
    await updatePost(postId, form)
    window.location.href = `/post/${postId}`
  } catch (e) {
    error.value = e.message || '更新失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPost()
})
</script>
