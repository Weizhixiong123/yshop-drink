<template>
  <view class="staff-console">
    <!-- Hero / Header with Texas Bar vibe -->
    <view class="hero-section">
      <image class="hero-image" src="/static/home-hero.png" mode="aspectFill"></image>
      <view class="hero-overlay">
        <view class="brand-logo">
          醉<text class="spade">♠</text>岛 Bar
        </view>
        <view class="hero-title">STAFF</view>
        <view class="hero-subtitle">CONSOLE</view>
        <view class="console-subtitle">内部员工操作台</view>
        <view class="hero-elements">
          <view class="poker-chip chip-red"></view>
          <view class="poker-chip chip-black"></view>
          <view class="poker-card">
            <view class="card-suit">♠</view>
            <view class="card-val">A</view>
          </view>
        </view>
      </view>
    </view>

    <!-- Console Grid -->
    <view class="content-panel">
      <view class="tools-grid">
        <!-- Member Add Component -->
        <view class="tool-card" @tap="navTo('/pages/staff/add')">
          <view class="staff-icon-wrap">
            <view class="vip-card-icon">
              <text class="vip-star">★</text>
              <view class="vip-dots">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
          <view class="tool-info">
            <text class="tool-name">会员开卡</text>
            <text class="tool-desc">录入新客信息与初始化</text>
          </view>
        </view>

        <!-- Member Search Component -->
        <view class="tool-card" @tap="navTo('/pages/staff/search')">
          <view class="staff-icon-wrap">
            <view class="contact-search-icon">
              <view class="person-silhouette">
                <view class="head"></view>
                <view class="body"></view>
              </view>
              <view class="mag-glass"></view>
            </view>
          </view>
          <view class="tool-info">
            <text class="tool-name">会员查询</text>
            <text class="tool-desc">积分/存酒/余额管理</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import cookie from '@/utils/cookie'

// 进入操作台时检查登录状态
onShow(() => {
  const token = cookie.get('accessToken')
  if (!token) {
    uni.redirectTo({ url: '/pages/staff/login' })
  }
})

const navTo = (url) => {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.staff-console {
  min-height: 100vh;
  background: #f5f2eb;
}

.hero-section {
  position: relative;
  height: 620rpx;
  overflow: hidden;
  background: #120d0c;
}

.hero-image {
  position: absolute;
  top: -46rpx;
  left: 0;
  width: 100%;
  height: 800rpx;
  display: block;
}

.hero-overlay {
  position: absolute;
  top: 140rpx;  /* Slightly shift up since logo is bigger */
  left: 46rpx;
  z-index: 2;
  display: flex;
  flex-direction: column;
}

.brand-logo {
  color: #ffffff;
  font-size: 52rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  letter-spacing: 4rpx;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.6);
  font-family: sans-serif;
  
  .spade {
    color: #111111;
    -webkit-text-stroke: 2.5px #ffffff; /* Thick white stroke around black spade */
    font-size: 76rpx; /* Noticeably larger than the text */
    margin: 0 10rpx;
    font-weight: 900;
    line-height: 1;
    text-shadow: none; /* remove text shadow to preserve sharp white stroke */
    filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.5)); /* Add shadow via filter instead */
  }
}

.hero-title {
  color: #ffffff;
  font-size: 76rpx;
  font-weight: 900;
  font-style: italic;
  font-family: Impact, Arial, sans-serif;
  letter-spacing: 4rpx;
  line-height: 1.1;
  text-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.9);
}

.hero-subtitle {
  color: #ffffff;
  font-size: 76rpx;
  font-weight: 900;
  font-style: italic;
  font-family: Impact, Arial, sans-serif;
  letter-spacing: 2rpx;
  line-height: 1.1;
  text-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.9);
  margin-bottom: 24rpx;
}

.console-subtitle {
  color: #b7b0a8;
  font-size: 26rpx;
  letter-spacing: 2rpx;
}

.hero-elements {
  position: absolute;
  top: 140rpx;
  right: -320rpx;
  display: flex;
  align-items: center;
}

.poker-chip {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 4rpx dashed #ffffff;
  box-shadow: 0 10rpx 20rpx rgba(0, 0, 0, 0.6);
  position: absolute;
}
.chip-red {
  background: #c1272d;
  left: 0;
  z-index: 2;
  transform: rotate(15deg);
}
.chip-black {
  background: #1a1a1a;
  left: 40rpx;
  z-index: 1;
  transform: rotate(-10deg);
}

.poker-card {
  width: 64rpx;
  height: 90rpx;
  background: #ffffff;
  border-radius: 8rpx;
  box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.5);
  margin-left: 120rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transform: rotate(18deg);
  border: 2rpx solid #e0e0e0;
}
.poker-card .card-suit {
  color: #1a1a1a;
  font-size: 32rpx;
  line-height: 1;
  margin-bottom: 2rpx;
}
.poker-card .card-val {
  color: #1a1a1a;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 1;
}

.content-panel {
  position: relative;
  margin-top: -40rpx;
  padding: 0 30rpx 40rpx;
  z-index: 10;
}

.tools-grid {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.tool-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 28rpx;
  padding: 40rpx;
  box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.08);
  transition: transform 0.2s;

  &:active {
    transform: scale(0.98);
    background: #fafafa;
  }

  .staff-icon-wrap {
    width: 90rpx;
    height: 90rpx;
    border-radius: 24rpx;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    margin-right: 30rpx;
    flex-shrink: 0;
  }

  .vip-card-icon {
    width: 58rpx;
    height: 42rpx;
    background: #111111;
    border-radius: 8rpx;
    position: relative;
    display: flex;
    align-items: center;

    .vip-star {
      color: #ffffff;
      font-size: 26rpx;
      margin-left: 8rpx;
      margin-top: -2rpx;
    }

    .vip-dots {
      position: absolute;
      right: 6rpx;
      bottom: 8rpx;
      display: flex;
      gap: 3rpx;

      .dot {
        width: 4rpx;
        height: 4rpx;
        background: #ffffff;
        border-radius: 1rpx;
      }
    }
  }

  .contact-search-icon {
    width: 60rpx;
    height: 60rpx;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;

    .person-silhouette {
      position: absolute;
      top: 6rpx;
      left: 4rpx;
      display: flex;
      flex-direction: column;
      align-items: center;

      .head {
        width: 22rpx;
        height: 26rpx;
        background: #111111;
        border-radius: 11rpx;
      }
      .body {
        margin-top: 2rpx;
        width: 44rpx;
        height: 22rpx;
        background: #111111;
        border-radius: 22rpx 22rpx 4rpx 4rpx;
      }
    }

    .mag-glass {
      position: absolute;
      bottom: 2rpx;
      right: -2rpx;
      width: 32rpx;
      height: 32rpx;
      border: 4rpx solid #111111;
      border-radius: 50%;
      background: #f5f5f5;
      box-sizing: border-box;

      &::after {
        content: '';
        position: absolute;
        bottom: -8rpx;
        right: -8rpx;
        width: 14rpx;
        height: 4rpx;
        background: #111111;
        transform: rotate(45deg);
        border-radius: 2rpx;
      }
    }
  }

  .tool-info {
    display: flex;
    flex-direction: column;

    .tool-name {
      color: #111111;
      font-size: 32rpx;
      font-weight: bold;
      margin-bottom: 8rpx;
    }

    .tool-desc {
      color: #8a8a8a;
      font-size: 24rpx;
    }
  }
}
</style>
