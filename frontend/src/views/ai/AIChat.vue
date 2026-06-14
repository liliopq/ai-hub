<template>
  <div class="min-h-screen">
    <div class="max-w-4xl mx-auto">
      <div class="bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="flex items-center justify-between p-4 border-b border-gray-100">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
              <Bot class="w-6 h-6 text-primary-600" />
            </div>
            <h1 class="text-lg font-semibold text-gray-900">AI 助手</h1>
          </div>
          <button 
            @click="newChat"
            class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors text-sm"
          >
            <Plus class="w-4 h-4 inline mr-1" />
            新会话
          </button>
        </div>
        <div class="flex h-[600px]">
          <div class="w-64 border-r border-gray-100 p-3 overflow-y-auto">
            <h3 class="text-sm font-medium text-gray-500 mb-3">历史会话</h3>
            <div class="space-y-2">
              <div 
                v-for="session in sessions" 
                :key="session.sessionId"
                @click="switchSession(session.sessionId)"
                :class="[
                  'p-3 rounded-lg cursor-pointer transition-colors',
                  currentSessionId === session.sessionId 
                    ? 'bg-primary-50 border border-primary-200' 
                    : 'hover:bg-gray-50'
                ]"
              >
                <p class="text-sm font-medium text-gray-900 truncate">{{ session.title }}</p>
                <p class="text-xs text-gray-400">{{ formatTime(session.lastUpdate) }}</p>
                <button 
                  @click.stop="deleteSessionItem(session.sessionId)"
                  class="mt-1 text-xs text-red-500 hover:text-red-600"
                >
                  删除
                </button>
              </div>
            </div>
            <div v-if="sessions.length === 0" class="text-center py-8">
              <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-2" />
              <p class="text-gray-500 text-sm">暂无会话</p>
            </div>
          </div>
          <div class="flex-1 flex flex-col">
            <div 
              ref="messagesContainer"
              class="flex-1 overflow-y-auto p-4 space-y-4"
            >
              <div 
                v-for="(msg, index) in messages" 
                :key="index"
                :class="[
                  'flex',
                  msg.role === 'user' ? 'justify-end' : 'justify-start'
                ]"
              >
                <div 
                  :class="[
                    'max-w-[70%] p-4 rounded-xl',
                    msg.role === 'user' 
                      ? 'bg-primary-600 text-white rounded-tr-none' 
                      : 'bg-gray-100 text-gray-900 rounded-tl-none'
                  ]"
                >
                  <p>{{ msg.content }}</p>
                </div>
              </div>
              <div v-if="isTyping" class="flex justify-start">
                <div class="bg-gray-100 p-4 rounded-xl rounded-tl-none">
                  <div class="flex space-x-1">
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 0ms"></span>
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 150ms"></span>
                    <span class="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style="animation-delay: 300ms"></span>
                  </div>
                </div>
              </div>
              <div v-if="messages.length === 0 && !isTyping" class="flex flex-col items-center justify-center h-full">
                <Bot class="w-16 h-16 text-gray-300 mb-4" />
                <h3 class="text-lg font-semibold text-gray-900 mb-2">AI 助手</h3>
                <p class="text-gray-500 text-center max-w-xs">
                  我可以帮助您解答问题、提供建议等。开始对话吧！
                </p>
              </div>
            </div>
            <div class="border-t border-gray-100 p-4">
              <form @submit.prevent="sendMessage">
                <div class="flex items-end space-x-3">
                  <textarea 
                    v-model="inputMessage"
                    placeholder="输入您的问题..."
                    class="flex-1 border border-gray-300 rounded-xl px-4 py-3 resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none"
                    rows="2"
                  ></textarea>
                  <button 
                    type="submit"
                    :disabled="!inputMessage.trim() || isTyping"
                    class="px-6 py-3 bg-primary-600 text-white rounded-xl hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Send class="w-5 h-5" />
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Bot, Plus, MessageSquare, Send } from 'lucide-vue-next'
import { chatWithAI, getSessions, deleteSession, getSessionMessages } from '../../api/ai'

const sessions = ref([])
const currentSessionId = ref('')
const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  return '刚刚'
}

const loadSessions = async () => {
  try {
    sessions.value = await getSessions()
    // 优先从 localStorage 恢复当前会话
    const savedSessionId = localStorage.getItem('ai_current_session_id')
    if (savedSessionId && sessions.value.some(s => s.sessionId === savedSessionId)) {
      currentSessionId.value = savedSessionId
    } else if (sessions.value.length > 0) {
      currentSessionId.value = sessions.value[0].sessionId
    }
    // 加载当前会话的历史消息
    if (currentSessionId.value) {
      await loadSessionMessages()
    }
  } catch (e) {
    console.error('获取会话列表失败:', e)
  }
}

const loadSessionMessages = async () => {
  if (!currentSessionId.value) {
    messages.value = []
    return
  }
  // 保存当前会话ID到 localStorage
  localStorage.setItem('ai_current_session_id', currentSessionId.value)
  // 从后端获取历史消息
  try {
    const historyMessages = await getSessionMessages(currentSessionId.value)
    messages.value = historyMessages.map(msg => ({
      role: msg.role === 'USER' ? 'user' : 'assistant',
      content: msg.content
    }))
  } catch (e) {
    console.error('获取历史消息失败:', e)
    messages.value = []
  }
}

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId
  await loadSessionMessages()
}

const newChat = () => {
  currentSessionId.value = ''
  messages.value = []
  localStorage.removeItem('ai_current_session_id')
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({ role: 'user', content: userMessage })
  inputMessage.value = ''
  isTyping.value = true
  
  await nextTick(() => {
    scrollToBottom()
  })
  
  try {
    const data = await chatWithAI({
      message: userMessage,
      sessionId: currentSessionId.value || undefined
    })
    
    currentSessionId.value = data.sessionId
    // 发送消息后保存会话ID到 localStorage
    localStorage.setItem('ai_current_session_id', currentSessionId.value)
    messages.value.push({ role: 'assistant', content: data.reply })
    
    await loadSessions()
  } catch (e) {
    console.error('AI聊天失败:', e)
    messages.value.push({ role: 'assistant', content: '抱歉，我暂时无法回答您的问题。' })
  } finally {
    isTyping.value = false
    await nextTick(() => {
      scrollToBottom()
    })
  }
}

const deleteSessionItem = async (sessionId) => {
  if (!confirm('确定要删除这个会话吗？')) return
  try {
    await deleteSession(sessionId)
    sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = sessions.value.length > 0 ? sessions.value[0].sessionId : ''
      messages.value = []
    }
  } catch (e) {
    console.error('删除会话失败:', e)
  }
}

const scrollToBottom = () => {
  const container = document.querySelector('.overflow-y-auto')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

onMounted(() => {
  loadSessions()
})
</script>
