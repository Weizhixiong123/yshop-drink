<template>
  <view class="login-page">
    <!-- Top Image Background -->
    <view class="hero-bg">
      <image src="/static/texas_bar_bg.png" mode="aspectFill" class="hero-image" />
      <view class="hero-overlay"></view>
    </view>

    <view class="login-content">
      <!-- Brand Header -->
      <view class="hero-brand">
        <view class="logo">醉<text class="spade">♠</text>岛</view>
        <view class="subtitle">TEXAS HOLD'EM BAR</view>
        <view class="slogan">FOLLOW YOUR HEART</view>
      </view>

      <!-- Login Card -->
      <view class="login-card">
        <!-- #ifdef MP-WEIXIN -->
        <button
          class="wx-login-btn"
          open-type="getPhoneNumber"
          @getphonenumber="onGetPhoneNumber"
          :disabled="loading"
        >
          <view class="btn-content">
            <view class="wx-icon">
              <view class="wx-eye-l"></view>
              <view class="wx-eye-r"></view>
              <view class="wx-smile"></view>
            </view>
            <text>{{ loading ? '登录中...' : '微信手机号一键登录' }}</text>
          </view>
        </button>
        <!-- #endif -->

        <!-- #ifndef MP-WEIXIN -->
        <view class="platform-tip">请在微信小程序中使用快捷登录</view>
        <!-- #endif -->
        
        <!-- 开发调试环境专属：快速模拟登录入口 -->
        <view v-if="STAFF_WX_LOGIN_MOCK" class="mock-actions">
          <view class="mock-title">-- 开发调试快捷入口 --</view>
          <view class="mock-btns">
            <text class="mock-btn" @tap="mockStaffLogin">🧔🏻 模拟员工</text>
            <text class="mock-btn" @tap="mockCustomerLogin">🧑🏻 模拟客户</text>
          </view>
        </view>
      </view>
      <!-- Footer removed -->
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { staffWxLogin } from '@/api/staffAuth'
import { customerLogin } from '@/api/customerAuth' // 引入客户登录接口用于测试
import cookie from '@/utils/cookie'
import { STAFF_WX_LOGIN_MOCK } from '@/config'

const loading = ref(false)
const MOCK_PHONE_CODE = 'dev-mock-phone-code'

/**
 * 登录成功统一处理，根据 role 自动跳转
 */
const onLoginSuccess = (data) => {
  const token = data?.token || data?.accessToken || (typeof data === 'string' ? data : '')
  if (!token) {
    uni.showToast({ title: '登录失败：未返回登录凭证', icon: 'none' })
    return
  }
  cookie.set('accessToken', token)

  // 默认作为 customer 处理
  const role = data?.role || 'customer'
  cookie.set('userRole', role)
  
  if (data?.phone) {
    cookie.set('customerPhone', data.phone)
  }

  uni.showToast({ title: '登录成功', icon: 'success' })

  // 无感区分：根据角色自动分配跳转页面
  setTimeout(() => {
    if (role === 'staff' || role === 'admin') {
      uni.reLaunch({ url: '/pages/index/index' }) // 员工进入工作台
    } else {
      uni.reLaunch({ url: '/pages/customer/info' }) // 客户进入个人中心
    }
  }, 1000)
}

const loginWithPhoneCode = async (code) => {
  if (loading.value) return
  loading.value = true

  try {
    // 如果后端的 /api/auth/customer/login 已经支持接收 code 来统一微信登录，这里可以直接换成 customerLogin
    const data = await staffWxLogin({ code })
    onLoginSuccess(data)
  } catch (err) {
    console.error('[wxLogin error]:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 微信小程序：手机号授权回调
 */
const onGetPhoneNumber = async (e) => {
  const detail = e?.detail || {}
  const errMsg = String(detail.errMsg || '')
  console.log('[getPhoneNumber] detail:', detail)

  const code = detail.code
  if (code) {
    await loginWithPhoneCode(code)
    return
  }

  if (STAFF_WX_LOGIN_MOCK) {
    // 开启 MOCK 时，点击微信按钮默认走员工 mock（保持和以前一样）
    mockStaffLogin()
    return
  }

  if (errMsg && errMsg.indexOf('deny') > -1) {
    uni.showToast({ title: '需要授权手机号才能体验服务', icon: 'none' })
    return
  }
  if (errMsg && errMsg.indexOf('fail') > -1) {
    uni.showToast({ title: '获取手机号失败，请重试', icon: 'none' })
    return
  }
  uni.showToast({ title: '获取授权码失败', icon: 'none' })
}

/**
 * 模拟员工登录
 */
const mockStaffLogin = async () => {
  uni.showToast({ title: '模拟员工登录中...', icon: 'none' })
  await loginWithPhoneCode(MOCK_PHONE_CODE)
}

/**
 * 模拟客户登录
 */
const mockCustomerLogin = async () => {
  if (loading.value) return
  loading.value = true
  uni.showToast({ title: '模拟客户登录中...', icon: 'none' })
  
  try {
    // 调用之前的普通客户手机号登录接口进行模拟
    const data = await customerLogin({ phone: '13800138000' })
    // 为了防止后端不返回 role 导致的一些问题，这里强制打上 customer 标签
    onLoginSuccess({ ...data, role: 'customer', phone: '13800138000' })
  } catch (err) {
    console.error('[mockCustomerLogin error]:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #111;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 1;

  .hero-image {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
  }

  .hero-overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.8) 100%);
  }
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  min-height: 100vh;
  padding: 0 60rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-brand {
  text-align: center;
  color: #fff;
  margin-bottom: 80rpx;
  margin-top: -10vh; /* 微微上移，视觉更平衡 */
  
  .logo {
    font-size: 80rpx;
    font-weight: 900;
    letter-spacing: 4rpx;
    margin-bottom: 16rpx;
    display: flex;
    justify-content: center;
    align-items: center;

    .spade { 
      color: #000000; 
      margin: 0 16rpx; 
      font-size: 90rpx; 
      -webkit-text-stroke: 2rpx #ffffff;
    }
  }
  
  .subtitle {
    font-size: 26rpx;
    letter-spacing: 12rpx;
    margin-bottom: 40rpx;
    opacity: 0.9;
  }
  
  .slogan {
    font-size: 34rpx;
    font-weight: bold;
    letter-spacing: 8rpx;
    font-family: Georgia, serif;
  }
}

/* Login Card */
.login-card {
  padding: 20rpx 20rpx;

  .card-title {
    color: #ffffff;
    font-size: 34rpx;
    font-weight: bold;
    text-align: center;
    margin-bottom: 16rpx;
  }

  .card-desc {
    color: #666666;
    font-size: 24rpx;
    text-align: center;
    line-height: 1.6;
    margin-bottom: 50rpx;
  }
}

/* WeChat Login Button */
.wx-login-btn {
  width: 100% !important;
  height: 100rpx;
  background: #07c160 !important;
  color: #ffffff !important;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 50rpx !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none !important;
  letter-spacing: 2rpx;
  box-shadow: 0 12rpx 40rpx rgba(7, 193, 96, 0.25);
  padding: 0 !important;
  margin: 0 !important;
  line-height: 100rpx !important;

  &::after {
    border: none !important;
  }

  &:active {
    opacity: 0.85;
    transform: scale(0.97);
  }

  &[disabled] {
    opacity: 0.5;
  }

  .btn-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
  }

  /* CSS WeChat icon */
  .wx-icon {
    width: 44rpx;
    height: 36rpx;
    background: #ffffff;
    border-radius: 8rpx 8rpx 12rpx 12rpx;
    position: relative;

    .wx-eye-l, .wx-eye-r {
      position: absolute;
      width: 8rpx;
      height: 8rpx;
      background: #07c160;
      border-radius: 50%;
      top: 10rpx;
    }
    .wx-eye-l { left: 10rpx; }
    .wx-eye-r { right: 10rpx; }
    .wx-smile {
      position: absolute;
      bottom: 6rpx;
      left: 50%;
      transform: translateX(-50%);
      width: 16rpx;
      height: 8rpx;
      border-bottom: 3rpx solid #07c160;
      border-radius: 0 0 10rpx 10rpx;
    }
  }
}

.platform-tip {
  margin-top: 30rpx;
  color: #ffffff;
  font-size: 26rpx;
  line-height: 1.5;
  text-align: center;
  opacity: 0.72;
}

.footer-hint {
  text-align: center;
  margin-top: 60rpx;
  color: #444444;
  font-size: 24rpx;
  letter-spacing: 2rpx;
}

/* 开发调试入口样式 */
.mock-actions {
  margin-top: 50rpx;
  padding-top: 40rpx;
  border-top: 1px dashed rgba(255, 255, 255, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mock-title {
  color: #e6a23c;
  font-size: 24rpx;
  margin-bottom: 20rpx;
  opacity: 0.8;
}

.mock-btns {
  display: flex;
  gap: 30rpx;
}

.mock-btn {
  background: rgba(230, 162, 60, 0.15);
  color: #e6a23c;
  padding: 12rpx 30rpx;
  border-radius: 40rpx;
  font-size: 26rpx;
  border: 1px solid rgba(230, 162, 60, 0.3);
  
  &:active {
    opacity: 0.7;
  }
}
</style>
