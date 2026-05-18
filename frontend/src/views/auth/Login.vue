<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4">
    <div class="max-w-md w-full bg-white rounded-xl shadow-lg p-8">
      <div class="text-center mb-8">
        <div class="w-12 h-12 bg-primary-600 rounded-lg flex items-center justify-center mx-auto mb-4">
          <Sparkles class="w-6 h-6 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-gray-900">欢迎回来</h1>
        <p class="text-gray-500 mt-2">登录您的账户</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">用户名或邮箱</label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input 
                v-model="form.username"
                type="text" 
                placeholder="请输入用户名或邮箱"
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
                placeholder="请输入密码"
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
        </div>
        <button 
          type="submit"
          :disabled="loading"
          class="w-full bg-primary-600 text-white py-2 rounded-lg hover:bg-primary-700 transition-colors mt-6 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="loading" class="flex items-center justify-center">
            <Loader2 class="w-5 h-5 animate-spin mr-2" />
            登录中...
          </span>
          <span v-else>登录</span>
        </button>
      </form>
      <p class="text-center text-gray-500 text-sm mt-6">
        还没有账户？
        <router-link to="/register" class="text-primary-600 hover:text-primary-700">立即注册</router-link>
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
import { login } from '../../api/auth'
import { getUserInfo } from '../../api/user'
import { setToken, setUser } from '../../utils/auth'

const form = reactive({
  username: '',
  password: ''
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const token = await login(form)
    setToken(token)
    
    // 获取用户信息
    const userInfo = await getUserInfo()
    setUser(userInfo)
    
    window.location.href = '/'
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>
