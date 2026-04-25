<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authStore } from '../stores/auth.js'

const router = useRouter()
const loginForm = ref({ username: '', password: '' })
const errorMsg = ref('')
const submitting = ref(false)

const handleLogin = async () => {
  errorMsg.value = ''
  submitting.value = true

  try {
    await authStore.login(loginForm.value.username, loginForm.value.password)
    router.push('/dashboard/members')
  } catch (error) {
    errorMsg.value = error?.message || '账号或密码错误'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-screen">
    <div class="hero-section">
      <div class="hero-overlay">
        <div class="brand-logo">
          醉<text class="spade">♠</text>岛 Bar
        </div>
        <div class="hero-title">BOSS</div>
        <div class="hero-subtitle">CONSOLE</div>
        <div class="console-subtitle">超级管理 / 店东操作台</div>
        
        <div class="hero-elements">
          <div class="poker-chip chip-red"></div>
          <div class="poker-chip chip-black"></div>
          <div class="poker-card">
            <div class="card-suit">♠</div>
            <div class="card-val">K</div>
          </div>
        </div>
      </div>
    </div>

    <div class="login-box card-panel">
      <h2 class="box-title">安全登入身份验证</h2>
      <form @submit.prevent="handleLogin" class="form-group">
        <div class="input-wrapper">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
          </span>
          <input
            v-model="loginForm.username"
            type="text"
            placeholder="账号 (默认: admin)"
            class="input-field"
            required
          />
        </div>
        <div class="input-wrapper">
          <span class="input-icon">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12.65 10A5.99 5.99 0 007 6c-3.31 0-6 2.69-6 6s2.69 6 6 6a5.99 5.99 0 005.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
            </svg>
          </span>
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="密码 (默认: admin123admin)"
            class="input-field"
            required
          />
        </div>
        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        <button type="submit" class="btn-solid login-btn" :disabled="submitting">
          {{ submitting ? '登录中...' : '立即进入后台' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-screen {
  min-height: 100vh;
  background: var(--bg-body);
  display: flex;
  flex-direction: column;
}

.hero-section {
  position: relative;
  height: 480px;
  overflow: hidden;
  background: #120d0c;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 800px;
  padding: 0 40px;
}

.brand-logo {
  color: #ffffff;
  font-size: 40px;
  font-weight: 900;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  letter-spacing: 2px;
}

.brand-logo .spade {
  color: #111111;
  -webkit-text-stroke: 2px #ffffff;
  font-size: 58px;
  margin: 0 8px;
  font-weight: 900;
  line-height: 1;
  filter: drop-shadow(0 2px 5px rgba(0,0,0,0.5));
}

.hero-title, .hero-subtitle {
  color: #ffffff;
  font-size: 80px;
  font-weight: 900;
  font-style: italic;
  font-family: var(--font-impact);
  letter-spacing: 2px;
  line-height: 1.1;
  text-shadow: 0 10px 30px rgba(0, 0, 0, 0.9);
}
.hero-subtitle {
  margin-bottom: 20px;
}

.console-subtitle {
  color: #b7b0a8;
  font-size: 16px;
  letter-spacing: 2px;
}

/* 扑克装饰 */
.hero-elements {
  position: absolute;
  top: 50px;
  right: 50px;
  display: flex;
  align-items: center;
  transform: scale(1.5);
}
.poker-chip {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 4px dashed #ffffff;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.6);
  position: absolute;
}
.chip-red {
  background: var(--tx-red);
  left: 0;
  z-index: 2;
  transform: rotate(15deg);
}
.chip-black {
  background: #1a1a1a;
  left: 40px;
  z-index: 1;
  transform: rotate(-10deg);
}

.poker-card {
  width: 64px;
  height: 90px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.5);
  margin-left: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transform: rotate(18deg);
  border: 2px solid #e0e0e0;
}
.poker-card .card-suit {
  color: #1a1a1a;
  font-size: 32px;
  line-height: 1;
  margin-bottom: 2px;
}
.poker-card .card-val {
  color: #1a1a1a;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

/* 登录面板 (实心白底框) */
.login-box {
  width: 440px;
  padding: 40px;
  margin: -60px auto 40px; 
  z-index: 10;
  position: relative;
}

.box-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 20px;
  font-weight: bold;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  color: #111111; /* Changed to solid black */
}

.input-field {
  width: 100%;
  padding: 14px 14px 14px 40px;
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  border-radius: var(--radius-sm);
  color: var(--text-main);
  font-size: 16px;
  transition: all 0.2s;
}

.input-field:focus {
  outline: none;
  border-color: var(--tx-red);
  background: #fff;
}

.error-msg {
  color: var(--tx-red);
  font-size: 14px;
  background: rgba(193, 39, 45, 0.1);
  padding: 10px;
  border-radius: 6px;
  text-align: center;
}

.login-btn {
  margin-top: 10px;
  width: 100%;
  padding: 16px;
  font-size: 18px;
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* ====================
   H5 (Mobile) 响应式适配
   ==================== */
@media (max-width: 768px) {
  .hero-section {
    height: 380px; 
  }
  .hero-overlay {
    padding: 0 20px;
    margin-top: -40px;
  }
  .hero-title, .hero-subtitle {
    font-size: 54px; /* 字体缩小以适应屏幕 */
  }
  .brand-logo { 
    font-size: 32px; 
    margin-bottom: 12px;
  }
  .brand-logo .spade { 
    font-size: 46px; 
  }
  .hero-elements {
    transform: scale(0.7); /* 缩小装饰元素 */
    right: -20px;
    top: 20px;
    opacity: 0.6; /* 移动端作为弱化背景 */
  }
  .login-box {
    width: 92%;
    margin: -80px auto 40px;
    padding: 30px 24px;
    border-radius: 20px;
  }
  .box-title {
    font-size: 18px;
  }
}
</style>
