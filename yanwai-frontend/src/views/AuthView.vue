<template>
  <div class="auth-page">
    <div class="auth-bg"></div>
    <div class="auth-container">
      <div class="auth-card">
        <div class="logo-section">
          <div class="logo-icon">🔮</div>
          <div class="logo-name">言外</div>
          <div class="logo-subtitle">听懂TA的潜台词</div>
        </div>

        <div class="tab-switch">
          <div
            class="tab"
            :class="{ active: isLogin }"
            @click="isLogin = true"
          >
            登录
          </div>
          <div
            class="tab"
            :class="{ active: !isLogin }"
            @click="isLogin = false"
          >
            注册
          </div>
        </div>

        <form class="auth-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <div class="input-wrapper">
              <span class="input-icon">📧</span>
              <input
                v-model="form.email"
                type="email"
                placeholder="请输入邮箱地址"
                class="auth-input"
                :class="{ error: emailError }"
              />
            </div>
            <div class="error-message" v-if="emailError">{{ emailError }}</div>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <span class="input-icon">🔐</span>
              <input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                class="auth-input"
                :class="{ error: passwordError }"
              />
              <span
                class="toggle-password"
                @click="showPassword = !showPassword"
              >
                {{ showPassword ? '🙈' : '👁️' }}
              </span>
            </div>
            <div class="error-message" v-if="passwordError">{{ passwordError }}</div>
            <div class="password-hint" v-if="!isLogin">
              密码需包含字母和数字，至少6位
            </div>
          </div>

          <div class="form-group" v-if="!isLogin">
            <label class="form-label">确认密码</label>
            <div class="input-wrapper">
              <span class="input-icon">🔑</span>
              <input
                v-model="form.confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请再次输入密码"
                class="auth-input"
                :class="{ error: confirmPasswordError }"
              />
            </div>
            <div class="error-message" v-if="confirmPasswordError">{{ confirmPasswordError }}</div>
          </div>

          <button
            type="submit"
            class="auth-btn"
            :disabled="loading"
          >
            {{ loading ? '处理中...' : (isLogin ? '登 录' : '注 册') }}
          </button>
        </form>

        <div class="divider">
          <span class="divider-line"></span>
          <span class="divider-text">或</span>
          <span class="divider-line"></span>
        </div>

        <button class="guest-btn" @click="guestLogin">
          访客模式体验
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  email: '',
  password: '',
  confirmPassword: ''
})

const emailError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')

function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

function validatePassword(password) {
  if (password.length < 6) {
    return '密码长度至少6位'
  }
  if (!/[a-zA-Z]/.test(password)) {
    return '密码需包含字母'
  }
  if (!/[0-9]/.test(password)) {
    return '密码需包含数字'
  }
  return ''
}

function validateForm() {
  emailError.value = ''
  passwordError.value = ''
  confirmPasswordError.value = ''

  if (!form.email.trim()) {
    emailError.value = '请输入邮箱'
    return false
  }
  if (!validateEmail(form.email)) {
    emailError.value = '请输入有效的邮箱地址'
    return false
  }

  if (!form.password.trim()) {
    passwordError.value = '请输入密码'
    return false
  }
  const pwdError = validatePassword(form.password)
  if (pwdError) {
    passwordError.value = pwdError
    return false
  }

  if (!isLogin.value) {
    if (!form.confirmPassword.trim()) {
      confirmPasswordError.value = '请确认密码'
      return false
    }
    if (form.password !== form.confirmPassword) {
      confirmPasswordError.value = '两次输入的密码不一致'
      return false
    }
  }

  return true
}

async function handleSubmit() {
  if (!validateForm()) return

  loading.value = true
  try {
    if (isLogin.value) {
      await userStore.login(form.email, form.password)
    } else {
      await userStore.register(form.email, form.password)
    }
    ElMessage.success(isLogin.value ? '登录成功' : '注册成功')
    router.push('/home')
  } catch (e) {
    ElMessage.error(e.message || '操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function guestLogin() {
  try {
    await userStore.guestLogin()
    router.push('/home')
  } catch (e) {
    ElMessage.error(e.message || '访客登录失败')
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0B0E14 0%, #1A1F2E 50%, #0B0E14 100%);
}

.auth-bg::before {
  content: '';
  position: absolute;
  top: -20%;
  left: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(212, 175, 55, 0.1) 0%, transparent 70%);
  animation: bgPulse 8s ease-in-out infinite;
}

.auth-bg::after {
  content: '';
  position: absolute;
  bottom: -20%;
  right: -20%;
  width: 60%;
  height: 60%;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.1) 0%, transparent 70%);
  animation: bgPulse 8s ease-in-out infinite reverse;
}

@keyframes bgPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 0.8; }
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
}

.auth-card {
  background: rgba(18, 24, 38, 0.95);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 24px;
  padding: 40px 32px;
  backdrop-filter: blur(20px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 64px;
  margin-bottom: 12px;
  animation: floatIcon 3s ease-in-out infinite;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.logo-name {
  font-size: 32px;
  font-weight: 800;
  color: #D4AF37;
  margin-bottom: 4px;
  letter-spacing: 2px;
}

.logo-subtitle {
  font-size: 14px;
  color: #64748B;
}

.tab-switch {
  display: flex;
  background: rgba(30, 41, 59, 0.5);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 32px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 12px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #64748B;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab.active {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.1));
  color: #D4AF37;
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.2);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #94A3B8;
  letter-spacing: 0.5px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  z-index: 1;
}

.auth-input {
  width: 100%;
  padding: 14px 14px 14px 44px;
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  color: #F8FAFC;
  font-size: 15px;
  outline: none;
  transition: all 0.3s ease;
}

.auth-input:focus {
  border-color: #D4AF37;
  box-shadow: 0 0 20px rgba(212, 175, 55, 0.2);
}

.auth-input.error {
  border-color: #ef4444;
}

.auth-input::placeholder {
  color: #64748B;
}

.toggle-password {
  position: absolute;
  right: 14px;
  cursor: pointer;
  font-size: 16px;
  transition: transform 0.3s ease;
}

.toggle-password:hover {
  transform: scale(1.1);
}

.error-message {
  font-size: 12px;
  color: #ef4444;
  margin-top: 4px;
}

.password-hint {
  font-size: 11px;
  color: #64748B;
  margin-top: 4px;
}

.auth-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #B8860B, #D4AF37);
  border: none;
  border-radius: 14px;
  color: #0B0E14;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 12px;
}

.auth-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #D4AF37, #F5D061);
  box-shadow: 0 0 30px rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 24px 0;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: rgba(212, 175, 55, 0.2);
}

.divider-text {
  font-size: 13px;
  color: #64748B;
}

.guest-btn {
  width: 100%;
  padding: 14px;
  background: transparent;
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 14px;
  color: #D4AF37;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.guest-btn:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: #D4AF37;
}
</style>