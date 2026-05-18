<template>
  <footer class="bg-white border-t border-gray-200 mt-auto">
    <div class="max-w-6xl mx-auto px-4 py-8">
      <div class="flex flex-col md:flex-row items-center justify-between">
        <div class="flex items-center space-x-2 mb-4 md:mb-0">
          <div class="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
            <Sparkles class="w-5 h-5 text-white" />
          </div>
          <span class="text-xl font-bold text-gray-900">AI Hub</span>
        </div>
        <p class="text-sm text-gray-500">© 2026 AI Hub Community. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { Sparkles } from 'lucide-vue-next'
</script>
