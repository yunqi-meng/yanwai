<template>
  <div class="not-found page-container">
    <div class="content">
      <div class="code">404</div>
      <div class="text">页面走丢了...</div>
      <div class="subtext">这个页面不存在或已被删除</div>
      <button class="btn-gold" @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  text-align: center;
}

.content {
  padding: 40px;
}

.code {
  font-size: 120px;
  font-weight: 700;
  background: linear-gradient(135deg, #B8860B, #D4AF37);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 20px;
}

.text {
  font-size: 24px;
  color: #FFFFFF;
  margin-bottom: 8px;
}

.subtext {
  font-size: 14px;
  color: #8B8B8B;
  margin-bottom: 32px;
}
</style>
