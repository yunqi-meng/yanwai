<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
      <div class="bg-shape bg-shape-4"></div>
    </div>
    
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <span>🔮</span>
        </div>
        <h1>言外管理后台</h1>
        <p>听懂TA的潜台词 · AI对话分析管理系统</p>
      </div>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            class="custom-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            class="custom-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <div class="divider">
          <span>默认账号</span>
        </div>
        <div class="account-info">
          <el-tag type="info" effect="plain">admin</el-tag>
          <span class="separator">/</span>
          <el-tag type="info" effect="plain">123456</el-tag>
        </div>
      </div>
    </div>
    
    <div class="copyright">
      © 2026 言外 - 听懂TA的潜台词
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const adminStore = useAdminStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await login(form)
    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin_id', String(res.data.adminId))
    adminStore.setToken(res.data.token)
    adminStore.setAdminId(res.data.adminId)
    ElMessage.success('登录成功')
    setTimeout(() => {
      window.location.href = '/dashboard'
    }, 500)
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.bg-shape-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation: float 15s infinite ease-in-out;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation: float 12s infinite ease-in-out reverse;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  animation: float 18s infinite ease-in-out;
}

.bg-shape-4 {
  width: 150px;
  height: 150px;
  top: 20%;
  right: 20%;
  animation: float 10s infinite ease-in-out reverse;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(20px, -20px) rotate(5deg);
  }
  50% {
    transform: translate(-20px, 20px) rotate(-5deg);
  }
  75% {
    transform: translate(10px, 10px) rotate(3deg);
  }
}

.login-card {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.login-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 10px;
  letter-spacing: 2px;
}

.login-header p {
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-bottom: 30px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 25px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 8px 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.25);
}

.login-form :deep(.el-input__prefix) {
  color: #667eea;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
}

.login-footer .divider {
  position: relative;
  margin-bottom: 20px;
}

.login-footer .divider::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 1px;
  background: #e8e8e8;
}

.login-footer .divider span {
  position: relative;
  padding: 0 15px;
  background: rgba(255, 255, 255, 0.95);
  color: #999;
  font-size: 13px;
}

.account-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.account-info .separator {
  color: #ccc;
}

.copyright {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  z-index: 1;
}
</style>
