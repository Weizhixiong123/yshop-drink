<template>
  <view class="login-page">
    <!-- Background decorative elements -->
    <view class="bg-decor">
      <view class="circle c1"></view>
      <view class="circle c2"></view>
      <view class="circle c3"></view>
    </view>

    <view class="login-content">
      <!-- Brand Header -->
      <view class="brand-section">
        <view class="brand-logo">
          醉<text class="spade">♠</text>岛 Bar
        </view>
        <view class="brand-subtitle">STAFF LOGIN</view>
        <view class="brand-desc">员工工作台</view>
      </view>

      <!-- Login Card -->
      <view class="login-card">
        <view class="card-title">微信快捷登录</view>
        <view class="card-desc">使用微信授权手机号登录，请确保老板已添加你的手机号为员工</view>

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
        <view class="platform-tip">请在微信小程序中使用员工手机号登录</view>
        <!-- #endif -->
      </view>

      <view class="footer-hint">请联系老板添加你的手机号为员工</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { staffWxLogin } from '@/api/staffAuth'
import cookie from '@/utils/cookie'
import { STAFF_WX_LOGIN_MOCK } from '@/config'

const loading = ref(false)
const MOCK_PHONE_CODE = 'dev-mock-phone-code'

/**
 * 登录成功统一处理
 */
const onLoginSuccess = (data) => {
  // 从返回数据中提取 token
  const token = data?.token || data?.accessToken || (typeof data === 'string' ? data : '')
  if (!token) {
    uni.showToast({ title: '登录失败：未返回登录凭证', icon: 'none' })
    return
  }
  cookie.set('accessToken', token)

  // 存储角色信息（如果返回了）
  if (data?.role) {
    cookie.set('userRole', data.role)
  }

  uni.showToast({ title: '登录成功', icon: 'success' })

  setTimeout(() => {
    uni.reLaunch({ url: '/pages/index/index' })
  }, 1000)
}

const loginWithPhoneCode = async (code) => {
  if (loading.value) return
  loading.value = true

  try {
    const data = await staffWxLogin({ code })
    onLoginSuccess(data)
  } catch (err) {
    console.error('[staffWxLogin] error:', err)
    // api.js 已自动 toast 错误
  } finally {
    loading.value = false
  }
}

/**
 * 微信小程序：手机号授权回调
 * wx.getPhoneNumber 成功后拿到 code，发给后端换 token
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
    uni.showToast({ title: '本地模拟登录中...', icon: 'none' })
    await loginWithPhoneCode(MOCK_PHONE_CODE)
    return
  }

  // 用户拒绝授权
  if (errMsg && errMsg.indexOf('deny') > -1) {
    uni.showToast({ title: '需要授权手机号才能登录', icon: 'none' })
    return
  }
  if (errMsg && errMsg.indexOf('fail') > -1) {
    uni.showToast({ title: '获取手机号失败，请重试', icon: 'none' })
    return
  }
  uni.showToast({ title: '获取授权码失败', icon: 'none' })
}

</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: #0d0b0a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* Background decorations */
.bg-decor {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.06;
  }
  .c1 {
    width: 600rpx; height: 600rpx;
    background: #ffffff;
    top: -200rpx; right: -150rpx;
  }
  .c2 {
    width: 400rpx; height: 400rpx;
    background: #ffffff;
    bottom: -100rpx; left: -100rpx;
  }
  .c3 {
    width: 200rpx; height: 200rpx;
    background: #c1272d;
    top: 40%; left: -60rpx;
  }
}

.login-content {
  width: 100%;
  padding: 0 60rpx;
  box-sizing: border-box;
  z-index: 2;
}

/* Brand */
.brand-section {
  text-align: center;
  margin-bottom: 80rpx;

  .brand-logo {
    color: #ffffff;
    font-size: 56rpx;
    font-weight: 900;
    letter-spacing: 4rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20rpx;

    .spade {
      color: #c1272d;
      font-size: 80rpx;
      margin: 0 10rpx;
      line-height: 1;
    }
  }

  .brand-subtitle {
    color: #ffffff;
    font-size: 36rpx;
    font-weight: 900;
    font-style: italic;
    font-family: Impact, Arial, sans-serif;
    letter-spacing: 6rpx;
    margin-bottom: 12rpx;
    opacity: 0.9;
  }

  .brand-desc {
    color: #666666;
    font-size: 26rpx;
    letter-spacing: 4rpx;
  }
}

/* Login Card */
.login-card {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 32rpx;
  padding: 50rpx 40rpx;
  backdrop-filter: blur(20px);

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
</style>
