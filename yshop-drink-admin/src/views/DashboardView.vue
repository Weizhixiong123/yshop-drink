<script setup>
import { useRouter } from 'vue-router'
import { authStore } from '../stores/auth.js'
import SidebarNav from '../components/SidebarNav.vue'

const router = useRouter()
const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="dashboard">
    <SidebarNav />
    
    <main class="main-content">
      <!-- 移动端专属顶部导航栏 (带退出按钮) -->
      <div class="mobile-topbar">
        <div class="mobile-brand">醉♠岛 Bar <span class="dim">|</span> BOSS</div>
        <div class="mobile-logout" @click="handleLogout">
          <span class="icon">🚪</span> 退出
        </div>
      </div>

      <router-view v-slot="{ Component }">
        <Transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  height: 100vh;
  background: var(--bg-body); 
  overflow: hidden;
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  padding: 40px;
  overflow-y: auto;
}

.mobile-topbar {
  display: none;
}

/* 子页面切换过渡 */
.page-fade-enter-active {
  transition: all 0.3s ease;
}
.page-fade-leave-active {
  transition: all 0.2s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ====================
   H5 (Mobile) 响应式适配
   ==================== */
@media (max-width: 768px) {
  .dashboard {
    flex-direction: column;
  }
  
  .main-content {
    /* 缩减四周内边距，并保留底部导航的防遮挡间隙 */
    padding: 15px; 
    padding-bottom: 80px; 
  }
  
  .mobile-topbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #120d0c;
    color: #fff;
    padding: 15px 20px;
    margin: -15px -15px 20px -15px;
    border-radius: 0 0 16px 16px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  }
  
  .mobile-brand {
    font-size: 16px;
    font-weight: bold;
    letter-spacing: 1px;
  }
  
  .mobile-brand .dim {
    color: #555;
    margin: 0 4px;
    font-weight: normal;
  }
  
  .mobile-logout {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--tx-red);
    font-size: 14px;
    font-weight: bold;
    background: rgba(193, 39, 45, 0.1);
    padding: 6px 12px;
    border-radius: 20px;
    cursor: pointer;
  }
  .mobile-logout .icon {
    font-size: 16px;
  }
}
</style>
