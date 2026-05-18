<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
    <div class="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-primary-600 rounded-lg flex items-center justify-center mx-auto mb-4">
          <Sparkles class="w-6 h-6 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-gray-900">创建账户</h1>
        <p class="text-gray-500 mt-2">加入我们的AI社区</p>
      </div>
      <form @submit.prevent="handleRegister">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input 
                v-model="form.username"
                type="text" 
                placeholder="请输入用户名（2-20个字符）"
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                required
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input 
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码（至少6个字符）"
                class="w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                required
              />
              <button 
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <Eye v-if="!showPassword" class="w-5 h-5" />
                <EyeOff v-else class="w-5 h-5" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">确认密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input 
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
                required
              />
            </div>
          </div>
        </div>
        <button 
          type="submit"
          :disabled="loading"
          class="w-full bg-primary-600 text-white py-2 rounded-lg hover:bg-primary-700 transition-colors mt-6 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="loading" class="flex items-center justify-center">
            <Loader2 class="w-5 h-5 animate-spin mr-2" />
            注册中...
          </span>
          <span v-else>注册</span>
        </button>
      </form>
      <p class="text-center text-gray-500 text-sm mt-6">
        已有账户？
        <router-link to="/login" class="text-primary-600 hover:text-primary-700">立即登录</router-link>
      </p>
      <div v-if="error" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Sparkles, User, Lock, Eye, EyeOff, Loader2 } from 'lucide-vue-next'
import { register } from '../../api/auth'
import { getUserInfo } from '../../api/user'
import { setToken, setUser } from '../../utils/auth'

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const handleRegister = async () => {
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const registerData = {
      username: form.username,
      password: form.password
    }
    // 只有当邮箱不为空时才添加
    if (form.email) {
      registerData.email = form.email
    }
    const data = await register(registerData)
    setToken(data.token)
    
    // 获取用户信息
    const userInfo = await getUserInfo()
    setUser(userInfo)
    
    window.location.href = '/'
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
