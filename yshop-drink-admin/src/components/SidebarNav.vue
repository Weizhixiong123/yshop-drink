<script setup>
import { useRouter, useRoute } from 'vue-router'
import { authStore } from '../stores/auth.js'

const router = useRouter()
const route = useRoute()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="sidebar">
    <div class="brand-zone">
      <div class="brand-logo">
        醉<text class="spade">♠</text>岛 Bar
      </div>
      <div class="brand-sub">BOSS CONSOLE</div>
      <div class="brand-desc">最高权限管理台</div>
    </div>
    
    <ul class="nav-links">
      <li
        :class="{ active: route.name === 'Members' }"
        @click="router.push('/dashboard/members')"
      >
        <span class="icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-1.95 0-3.73-.68-5.12-1.81C7.8 16.5 10.3 15.5 12 15.5s4.2 1 5.12 2.69C15.73 19.32 13.95 20 12 20zm6.36-4.04c-1.63-1.98-4.63-2.46-6.36-2.46s-4.73.48-6.36 2.46C4.6 14.51 4 13.33 4 12c0-4.41 3.59-8 8-8s8 3.59 8 8c0 1.33-.6 2.51-1.64 3.96zM12 6c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
          </svg>
        </span>
        <div class="text">
          <span class="title">会员档案</span>
        </div>
      </li>
      <li
        :class="{ active: route.name === 'Logs' }"
        @click="router.push('/dashboard/logs')"
      >
        <span class="icon">查</span>
        <div class="text">
          <span class="title">操作审计</span>
        </div>
      </li>
      <li
        :class="{ active: route.name === 'Account' }"
        @click="router.push('/dashboard/account')"
      >
        <span class="icon">账</span>
        <div class="text">
          <span class="title">门店对账</span>
        </div>
      </li>
      <li
        :class="{ active: route.name === 'Staff' }"
        @click="router.push('/dashboard/staff')"
      >
        <span class="icon">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/>
          </svg>
        </span>
        <div class="text">
          <span class="title">店员管理</span>
        </div>
      </li>
    </ul>

    <!-- 桌面级退出按钮 -->
    <div class="logout-btn" @click="handleLogout">
      退出管控 (Logout)
    </div>
  </nav>
</template>

<style scoped>
.sidebar {
  width: 260px;
  min-width: 260px;
  background: var(--bg-dark);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 50;
}

.brand-zone {
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
}

.brand-logo {
  color: #ffffff;
  font-size: 42px;
  font-weight: 900;
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.brand-logo .spade {
  color: #111111;
  -webkit-text-stroke: 1.5px #ffffff;
  font-size: 56px;
  margin: 0 4px;
  font-weight: 900;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
}

.brand-sub {
  color: #ffffff;
  font-size: 32px;
  font-weight: 900;
  font-style: italic;
  font-family: var(--font-impact);
  letter-spacing: 1px;
}

.brand-desc {
  color: #b7b0a8;
  font-size: 14px;
  letter-spacing: 1px;
  margin-top: 4px;
}

.nav-links {
  list-style: none;
  padding: 20px 0;
  margin: 0;
  flex-grow: 1;
}

.nav-links li {
  display: flex;
  align-items: center;
  padding: 16px 30px;
  cursor: pointer;
  transition: all 0.2s;
  color: #8a8a8a;
  border-left: 4px solid transparent;
}

.nav-links li:hover {
  background: rgba(255,255,255,0.05);
  color: #ffffff;
}

.nav-links li.active {
  background: rgba(255,255,255,0.1);
  color: #ffffff;
  border-left-color: var(--tx-red);
}

.nav-links li .icon {
  width: 36px;
  height: 36px;
  background: #ffffff;
  border-radius: 8px;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-right: 15px;
}

.nav-links li.active .icon {
  background: var(--tx-red);
  color: #ffffff;
}

.nav-links li .text {
  display: flex;
  flex-direction: column;
}

.nav-links li .title {
  font-size: 16px;
  font-weight: bold;
}

.logout-btn {
  margin: 30px;
  padding: 15px;
  text-align: center;
  background: #2a2a2a;
  color: #ffffff;
  border-radius: 12px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: var(--tx-red);
}

.mobile-logout {
  display: none !important;
}

/* ====================
   H5 (Mobile) 响应式适配：底部悬浮TabBar
   ==================== */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    min-width: 100%;
    height: auto;
    flex-direction: row;
    position: fixed;
    bottom: 0;
    left: 0;
    box-shadow: 0 -4px 10px rgba(0,0,0,0.1);
  }

  /* 隐藏桌面端特有元素 */
  .brand-zone { display: none; }
  .logout-btn { display: none; }

  /* 导航栏转为水平Tab */
  .nav-links {
    display: flex;
    flex-direction: row;
    padding: 0;
    width: 100%;
  }

  .nav-links li {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 10px 0;
    border-left: none;
    border-top: 3px solid transparent; /* 顶部代替左边框高亮 */
    justify-content: center;
    gap: 4px;
  }

  .nav-links li:hover {
    background: transparent;
  }

  .nav-links li.active {
    background: #1a1a1a;
    border-left: none;
    border-top-color: var(--tx-red);
  }

  .nav-links li .icon {
    width: 24px;
    height: 24px;
    margin-right: 0;
    font-size: 12px;
    border-radius: 6px;
    background: transparent;
    color: #8a8a8a;
  }

  .nav-links li.active .icon {
    background: var(--tx-red);
    color: #ffffff;
  }

  .nav-links li .text {
    align-items: center;
  }

  .nav-links li .title {
    font-size: 12px;
  }

}
</style>
