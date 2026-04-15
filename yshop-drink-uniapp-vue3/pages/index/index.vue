<template>
  <layout>
    <view class="home-page">
      <view class="hero-section">
        <image class="hero-image" src="/static/home-hero.png" mode="aspectFill"></image>
        <view class="hero-overlay">
          <view class="hero-tag">
            <text class="suit-icon">♠</text> TEXAS HOLD'EM & PUB
          </view>
          <view class="hero-title">ALL IN</view>
          <view class="hero-subtitle">FOR THE NIGHT</view>
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

      <view class="content-panel">
        <view class="service-card">
          <view class="welcome-card">
            <view class="welcome-copy">
              <view class="welcome-title">{{ welcomeTitle }}</view>
              <view class="welcome-desc">{{ welcomeDesc }}</view>
            </view>
            <view class="welcome-btn" @tap="openMemberCenter">{{ memberButtonText }}</view>
          </view>

          <view class="feature-grid-modern">
            <view class="feature-left" @tap="takein">
              <view class="feature-icon feature-icon-large cocktail-icon">
                <view class="martini-glass">
                  <view class="martini-top"></view>
                  <view class="martini-liquid"></view>
                  <view class="martini-straw"></view>
                </view>
                <view class="martini-stem"></view>
                <view class="martini-base"></view>
              </view>
              <view class="feature-text-center">
                <view class="feature-title">堂食点单</view>
                <view class="feature-subtitle">ORDER</view>
              </view>
            </view>

            <view class="feature-divider"></view>

            <view class="feature-right">
              <view class="feature-sub-item feature-sub-item-top" @tap="openStorage">
                <view class="feature-icon feature-icon-small storage-icon">
                  <view class="storage-glass storage-glass-left"></view>
                  <view class="storage-bucket">
                    <view class="storage-bottle-skew"></view>
                    <view class="storage-bucket-line"></view>
                  </view>
                  <view class="storage-glass storage-glass-right"></view>
                </view>
                <view class="feature-text-left">
                  <view class="feature-title">存酒</view>
                  <view class="feature-subtitle">STORING</view>
                </view>
              </view>

              <view class="feature-sub-item" @tap="openRecharge">
                <view class="feature-icon feature-icon-small vip-icon">
                  <view class="vip-card">VIP</view>
                </view>
                <view class="feature-text-left">
                  <view class="feature-title">会员充值</view>
                  <view class="feature-subtitle">RECHARGE</view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="home-notice" @tap="goScore">
          <view class="notice-left">
            <view class="notice-icon">✧</view>
            <view class="notice-text">会员积分拿好礼，微醺小酒0元提</view>
          </view>
          <view class="notice-arrow"></view>
        </view>

        <view class="booking-card">
          <view class="booking-copy">
            <view class="booking-title">预约订座</view>
            <view class="booking-subtitle">MIDNIGHT BOOK</view>
            <view class="booking-btn" @tap="openBooking">点击预约</view>
          </view>
          <view class="booking-illustration">
            <view class="table-top"></view>
            <view class="table-leg"></view>
            <view class="chair-back"></view>
            <view class="chair-seat"></view>
            <view class="chair-leg chair-leg-left"></view>
            <view class="chair-leg chair-leg-right"></view>
            <view class="glass-cup"></view>
            <view class="glass-stem"></view>
            <view class="glass-drink"></view>
          </view>
        </view>

        <view class="benefit-card">
          <view class="benefit-half benefit-half-left" @tap="goCoupons">
            <view class="benefit-icon coupon-pack-icon">
              <view class="coupon-bottle"></view>
              <view class="coupon-card-back"></view>
              <view class="coupon-card-front"></view>
              <view class="coupon-badge">积分</view>
            </view>
            <view class="benefit-copy">
              <view class="benefit-title">MIDNIGHT</view>
              <view class="benefit-title">COUPONS</view>
            </view>
          </view>
          <view class="benefit-divider"></view>
          <view class="benefit-half benefit-half-right" @tap="goScore">
            <view class="benefit-title-cn">积分兑换</view>
            <view class="benefit-desc">更多好礼等你兑换</view>
          </view>
        </view>

        <view class="location-card">
          <view class="location-header">
            <view class="location-meta">
              <view class="location-pin"></view>
              <view class="location-name">{{ shopDisplayName }}</view>
              <view class="location-distance">{{ shopDistance }}</view>
            </view>
            <view class="location-btn" @tap="openCurrentStoreLocation">门店位置</view>
          </view>
          <view class="location-address">{{ shopAddress }}</view>
        </view>
      </view>
    </view>
  </layout>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { storeToRefs } from 'pinia'
import { useMainStore } from '@/store/store'
import { shopGetList } from '@/api/market'
import { kmUnit } from '@/utils/util'

const main = useMainStore()
const { member, isLogin, store, location } = storeToRefs(main)
const currentShop = ref({})

const welcomeTitle = computed(() => {
  if (!isLogin.value) {
    return '欢迎加入seer'
  }

  return `欢迎回来，${member.value.nickname || 'seer会员'}`
})

const welcomeDesc = computed(() => {
  if (!isLogin.value) {
    return '注册后享受更多专属特权'
  }

  return '专属权益与会员服务已为你开启'
})

const memberButtonText = computed(() => {
  return isLogin.value ? '会员中心' : '开通会员'
})

const shopDisplayName = computed(() => {
  return currentShop.value.name || store.value.name || 'seer'
})

const shopDistance = computed(() => {
  if (currentShop.value.dis !== undefined) {
    return kmUnit(currentShop.value.dis)
  }

  if (store.value.dis !== undefined) {
    return kmUnit(store.value.dis)
  }

  return '附近门店'
})

const shopAddress = computed(() => {
  const addressMap = currentShop.value.addressMap || store.value.addressMap || ''
  const address = currentShop.value.address || store.value.address || '点击查看门店位置'
  return `${addressMap} ${address}`.trim()
})

const goLogin = () => {
  uni.navigateTo({
    url: '/pages/components/pages/login/login'
  })
}

const ensureLogin = () => {
  if (isLogin.value) {
    return true
  }

  goLogin()
  return false
}

const takein = () => {
  main.SET_ORDER_TYPE('takein')
  uni.switchTab({
    url: '/pages/menu/menu'
  })
}

const openMemberCenter = () => {
  if (!ensureLogin()) {
    return
  }

  uni.switchTab({
    url: '/pages/mine/mine'
  })
}

const openRecharge = () => {
  if (!ensureLogin()) {
    return
  }

  uni.navigateTo({
    url: '/pages/components/pages/balance/bill?cate=0'
  })
}

const goCoupons = () => {
  if (!ensureLogin()) {
    return
  }

  uni.navigateTo({
    url: '/pages/components/pages/coupons/coupons'
  })
}

const openStorage = () => {
  if (!ensureLogin()) {
    return
  }

  uni.showToast({
    title: '存酒功能开发中',
    icon: 'none'
  })
}

const openBooking = () => {
  uni.showToast({
    title: '预约订座功能开发中',
    icon: 'none'
  })
}

const goScore = () => {
  uni.navigateTo({
    url: '/pages/components/pages/scoreproduct/list'
  })
}

const getHomeShop = async () => {
  const data = await shopGetList({
    lat: location.value.latitude ? location.value.latitude : 0,
    lng: location.value.longitude ? location.value.longitude : 0,
    kw: '',
    shop_id: 0
  })

  if (!data || !data.length) {
    return
  }

  if (store.value.id) {
    const selectedShop = data.find((item) => item.id === store.value.id)
    currentShop.value = selectedShop || data[0]
    return
  }

  currentShop.value = data[0]
}

const openCurrentStoreLocation = () => {
  const shopInfo = currentShop.value.id ? currentShop.value : store.value

  if (!shopInfo || !shopInfo.lat || !shopInfo.lng) {
    uni.navigateTo({
      url: '/pages/components/pages/shop/shop'
    })
    return
  }

  uni.openLocation({
    latitude: parseFloat(shopInfo.lat),
    longitude: parseFloat(shopInfo.lng),
    name: shopInfo.name,
    address: `${shopInfo.addressMap || ''}${shopInfo.address || ''}`,
    fail: () => {
      uni.navigateTo({
        url: '/pages/components/pages/shop/shop'
      })
    }
  })
}

onLoad(() => {
  getHomeShop()
})
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: #f5f2eb;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes floatElement {
  0% { transform: translateY(0); }
  50% { transform: translateY(-4rpx); }
  100% { transform: translateY(0); }
}

.content-panel > view {
  animation: fadeInUp 0.7s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
  opacity: 0;
}

.content-panel > view:nth-child(1) { animation-delay: 0.1s; }
.content-panel > view:nth-child(2) { animation-delay: 0.15s; }
.content-panel > view:nth-child(3) { animation-delay: 0.2s; }
.content-panel > view:nth-child(4) { animation-delay: 0.25s; }
.content-panel > view:nth-child(5) { animation-delay: 0.3s; }

.hero-section {
  position: relative;
  height: 874rpx;
  overflow: hidden;
  background: #120d0c;
}

.hero-image {
  position: absolute;
  top: -46rpx;
  left: 0;
  width: 100%;
  height: 925rpx;
  display: block;
}

.hero-overlay {
  position: absolute;
  top: 140rpx;
  left: 46rpx;
  z-index: 2;
  display: flex;
  flex-direction: column;
  animation: fadeInUp 1s ease-out 0.2s forwards;
  opacity: 0;
}

.hero-tag {
  color: #d4a45b;
  font-size: 26rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  margin-bottom: 12rpx;
  text-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
}
.hero-tag .suit-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.hero-title {
  color: #ffffff;
  font-size: 88rpx;
  font-weight: 900;
  font-style: italic;
  font-family: Impact, Arial, sans-serif;
  letter-spacing: 4rpx;
  line-height: 1.1;
  text-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.9);
}

.hero-subtitle {
  color: #ffffff;
  font-size: 56rpx;
  font-weight: 900;
  font-style: italic;
  font-family: Impact, Arial, sans-serif;
  letter-spacing: 2rpx;
  line-height: 1.1;
  text-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.9);
  margin-bottom: 30rpx;
}

.hero-elements {
  display: flex;
  align-items: center;
  margin-top: 20rpx;
  position: relative;
}

.poker-chip {
  width: 48rpx;
  height: 48rpx;
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
  left: 30rpx;
  z-index: 1;
  transform: rotate(-10deg);
}

.poker-card {
  width: 56rpx;
  height: 80rpx;
  background: #ffffff;
  border-radius: 8rpx;
  box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.5);
  margin-left: 100rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transform: rotate(12deg);
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
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1;
}

.content-panel {
  position: relative;
  z-index: 2;
  margin-top: -8rpx;
  padding: 0 12rpx calc(180rpx + env(safe-area-inset-bottom));
}

.service-card {
  overflow: hidden;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 14rpx 36rpx rgba(32, 20, 12, 0.08);
  transition: transform 0.3s ease;
}
.service-card:active {
  transform: scale(0.98);
}

.welcome-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 26rpx 24rpx;
}

.welcome-copy {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.welcome-title {
  color: #17120f;
  font-size: 32rpx;
  font-weight: 700;
}

.welcome-desc {
  margin-top: 8rpx;
  color: #b2aba2;
  font-size: 20rpx;
}

.welcome-btn {
  flex-shrink: 0;
  padding: 16rpx 30rpx;
  border-radius: 999rpx;
  background: #090909;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
  transition: transform 0.2s ease, opacity 0.2s ease;
}
.welcome-btn:active {
  transform: scale(0.95);
  opacity: 0.8;
}

.feature-grid-modern {
  display: flex;
  align-items: stretch;
  padding: 10rpx 0 24rpx;
}

.feature-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30rpx 10rpx;
  transition: background-color 0.2s;
}
.feature-left:active {
  background-color: rgba(0, 0, 0, 0.03);
}

.feature-divider {
  width: 1px;
  margin: 30rpx 0;
  border-right: 1px dashed #d5c8bb;
}

.feature-right {
  flex: 1.1;
  display: flex;
  flex-direction: column;
}

.feature-sub-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 10rpx;
  transition: background-color 0.2s;
}
.feature-sub-item:active {
  background-color: rgba(0, 0, 0, 0.03);
}

.feature-sub-item-top {
  position: relative;
}
.feature-sub-item-top::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20rpx;
  right: 20rpx;
  height: 1px;
  border-bottom: 1px dashed #f0e6da;
}

.feature-icon-large {
  width: 160rpx;
  height: 140rpx;
  position: relative;
}

.feature-icon-small {
  width: 100rpx;
  height: 80rpx;
  position: relative;
  margin-right: 20rpx;
}

.feature-title {
  color: #17120f;
  font-size: 28rpx;
  font-weight: 700;
}

.feature-subtitle {
  margin-top: 6rpx;
  color: #b7b0a8;
  font-size: 18rpx;
  letter-spacing: 2rpx;
}

.feature-text-center {
  text-align: center;
  margin-top: 10rpx;
}

.feature-text-left {
  display: flex;
  flex-direction: column;
  min-width: 120rpx;
}

/* MARTINI GLASS ICON (Left side) */
.martini-glass {
  position: absolute;
  top: 10rpx;
  left: 25rpx;
  width: 110rpx;
  height: 60rpx;
}
.martini-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
  border-left: 55rpx solid transparent;
  border-right: 55rpx solid transparent;
  border-top: 60rpx solid #101010;
}
.martini-top::after {
  content: '';
  position: absolute;
  top: -64rpx;
  left: -47rpx;
  width: 0;
  height: 0;
  border-left: 47rpx solid transparent;
  border-right: 47rpx solid transparent;
  border-top: 52rpx solid #ffffff;
}
.martini-liquid {
  position: absolute;
  top: 24rpx;
  left: 22rpx;
  width: 0;
  height: 0;
  border-left: 33rpx solid transparent;
  border-right: 33rpx solid transparent;
  border-top: 36rpx solid #101010;
}
.martini-straw {
  position: absolute;
  top: -28rpx;
  left: 64rpx;
  width: 6rpx;
  height: 60rpx;
  background: #101010;
  transform: rotate(25deg);
  border-radius: 4rpx;
}
.martini-stem {
  position: absolute;
  top: 70rpx;
  left: 77rpx;
  width: 6rpx;
  height: 48rpx;
  background: #101010;
}
.martini-base {
  position: absolute;
  top: 118rpx;
  left: 47rpx;
  width: 66rpx;
  height: 6rpx;
  background: #101010;
  border-radius: 4rpx;
}

/* STORAGE BUCKET ICON (Top Right) */
.storage-icon {
  margin-top: 16rpx;
}
.storage-bucket {
  position: absolute;
  top: 12rpx;
  left: 24rpx;
  width: 44rpx;
  height: 36rpx;
  border: 6rpx solid #101010;
  border-top: 0;
  border-radius: 0 0 10rpx 10rpx;
  background: #ffffff;
  z-index: 2;
}
.storage-bucket-line {
  position: absolute;
  top: -6rpx;
  left: -8rpx;
  width: 60rpx;
  height: 6rpx;
  background: #101010;
  border-radius: 4rpx;
}
.storage-bottle-skew {
  position: absolute;
  top: -30rpx;
  left: 12rpx;
  width: 14rpx;
  height: 32rpx;
  background: #101010;
  transform: rotate(15deg);
  border-radius: 4rpx 4rpx 0 0;
}
.storage-bottle-skew::after {
  content: '';
  position: absolute;
  top: -12rpx;
  left: 2rpx;
  width: 10rpx;
  height: 12rpx;
  background: #101010;
  border-radius: 4rpx 4rpx 0 0;
}
.storage-glass {
  position: absolute;
  top: 14rpx;
  width: 18rpx;
  height: 24rpx;
  border: 5rpx solid #101010;
  border-top: 0;
  border-radius: 0 0 8rpx 8rpx;
  z-index: 1;
}
.storage-glass::after {
  content: '';
  position: absolute;
  top: 28rpx;
  left: 1.5rpx;
  width: 5rpx;
  height: 14rpx;
  background: #101010;
}
.storage-glass::before {
  content: '';
  position: absolute;
  top: 42rpx;
  left: -4.5rpx;
  width: 18rpx;
  height: 5rpx;
  background: #101010;
  border-radius: 2rpx;
}
.storage-glass-left {
  left: 2rpx;
}
.storage-glass-right {
  left: 68rpx;
}

/* VIP CARD ICON (Bottom Right) */
.vip-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}
.vip-card {
  width: 80rpx;
  height: 52rpx;
  border-radius: 12rpx;
  background: #101010;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.vip-card::after {
  content: '';
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  width: 16rpx;
  height: 6rpx;
  background: #ffffff;
  border-radius: 2rpx;
}

.home-notice,
.booking-card,
.benefit-card,
.location-card {
  margin-top: 18rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 24rpx rgba(40, 28, 17, 0.06);
  transition: transform 0.3s ease;
}
.home-notice:active,
.booking-card:active,
.benefit-card:active,
.location-card:active {
  transform: scale(0.98);
}

.home-notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
}

.notice-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.notice-icon {
  flex-shrink: 0;
  color: #8b755e;
  font-size: 24rpx;
}

.notice-text {
  margin-left: 14rpx;
  color: #44362b;
  font-size: 22rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-arrow {
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #b9b0a6;
  border-right: 3rpx solid #b9b0a6;
  transform: rotate(45deg);
}

.booking-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 26rpx 24rpx;
}

.booking-copy {
  display: flex;
  flex-direction: column;
}

.booking-title {
  color: #1a1715;
  font-size: 28rpx;
  font-weight: 700;
}

.booking-subtitle {
  margin-top: 8rpx;
  color: #141414;
  font-size: 24rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
}

.booking-btn {
  width: 132rpx;
  margin-top: 20rpx;
  padding: 12rpx 0;
  border-radius: 999rpx;
  background: #0e0e0e;
  color: #ffffff;
  font-size: 22rpx;
  text-align: center;
}

.booking-illustration {
  position: relative;
  width: 200rpx;
  height: 128rpx;
}

.table-top {
  position: absolute;
  top: 28rpx;
  right: 18rpx;
  width: 84rpx;
  height: 8rpx;
  background: #161616;
  border-radius: 999rpx;
}

.table-leg {
  position: absolute;
  top: 36rpx;
  right: 56rpx;
  width: 6rpx;
  height: 60rpx;
  background: #161616;
}

.table-leg::after {
  content: '';
  position: absolute;
  bottom: -4rpx;
  left: -18rpx;
  width: 42rpx;
  height: 6rpx;
  background: #161616;
  border-radius: 999rpx;
}

.chair-back {
  position: absolute;
  top: 22rpx;
  left: 30rpx;
  width: 8rpx;
  height: 76rpx;
  background: #161616;
}

.chair-seat {
  position: absolute;
  top: 62rpx;
  left: 38rpx;
  width: 44rpx;
  height: 8rpx;
  background: #161616;
  border-radius: 999rpx;
}

.chair-leg {
  position: absolute;
  top: 70rpx;
  width: 6rpx;
  height: 32rpx;
  background: #161616;
}

.chair-leg-left {
  left: 44rpx;
}

.chair-leg-right {
  left: 74rpx;
}

.glass-cup {
  position: absolute;
  top: 18rpx;
  right: 34rpx;
  width: 18rpx;
  height: 20rpx;
  border: 4rpx solid #161616;
  border-top: 0;
  border-radius: 0 0 8rpx 8rpx;
}

.glass-stem {
  position: absolute;
  top: 38rpx;
  right: 42rpx;
  width: 4rpx;
  height: 14rpx;
  background: #161616;
}

.glass-drink {
  position: absolute;
  top: 23rpx;
  right: 38rpx;
  width: 10rpx;
  height: 6rpx;
  background: #d4a45b;
  border-radius: 999rpx;
}

.benefit-card {
  display: flex;
  align-items: stretch;
  padding: 0 10rpx;
}

.benefit-half {
  box-sizing: border-box;
  min-height: 138rpx;
  display: flex;
  align-items: center;
}

.benefit-half-left {
  flex: 1.25;
  padding: 20rpx 12rpx 20rpx 10rpx;
}

.benefit-half-right {
  flex: 1;
  padding: 20rpx 16rpx;
  justify-content: center;
  flex-direction: column;
  align-items: flex-start;
}

.benefit-divider {
  width: 1px;
  margin: 22rpx 12rpx;
  border-right: 1px dashed #d5c8bb;
}

.benefit-icon {
  position: relative;
  width: 88rpx;
  height: 88rpx;
  flex-shrink: 0;
}

.coupon-pack-icon {
  margin-right: 18rpx;
}

.coupon-bottle {
  position: absolute;
  bottom: 8rpx;
  left: 8rpx;
  width: 20rpx;
  height: 54rpx;
  border: 4rpx solid #141414;
  border-radius: 8rpx 8rpx 10rpx 10rpx;
  transform: rotate(-14deg);
}

.coupon-bottle::before {
  content: '';
  position: absolute;
  top: -14rpx;
  left: 4rpx;
  width: 8rpx;
  height: 12rpx;
  border: 4rpx solid #141414;
  border-bottom: 0;
  border-radius: 6rpx 6rpx 0 0;
}

.coupon-card-back,
.coupon-card-front {
  position: absolute;
  border: 4rpx solid #141414;
  border-radius: 10rpx;
  background: #ffffff;
}

.coupon-card-back {
  right: 12rpx;
  bottom: 10rpx;
  width: 34rpx;
  height: 42rpx;
  transform: rotate(-10deg);
}

.coupon-card-front {
  right: 22rpx;
  bottom: 20rpx;
  width: 34rpx;
  height: 42rpx;
  transform: rotate(10deg);
}

.coupon-badge {
  position: absolute;
  right: 18rpx;
  bottom: 34rpx;
  z-index: 1;
  color: #141414;
  font-size: 14rpx;
  font-weight: 700;
  transform: rotate(10deg);
}

.benefit-copy {
  display: flex;
  flex-direction: column;
}

.benefit-title,
.benefit-title-cn {
  color: #181411;
  font-size: 28rpx;
  font-weight: 700;
}

.benefit-title + .benefit-title {
  margin-top: 4rpx;
}

.benefit-desc {
  margin-top: 10rpx;
  color: #8f867d;
  font-size: 20rpx;
}

.location-card {
  padding: 24rpx 26rpx 28rpx;
}

.location-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.location-meta {
  display: flex;
  align-items: center;
  min-width: 0;
}

.location-pin {
  position: relative;
  width: 20rpx;
  height: 28rpx;
  margin-right: 12rpx;
  border-radius: 50% 50% 50% 0;
  background: #141414;
  transform: rotate(-45deg);
}

.location-pin::after {
  content: '';
  position: absolute;
  top: 5rpx;
  left: 5rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #ffffff;
}

.location-name {
  max-width: 230rpx;
  color: #1a1715;
  font-size: 28rpx;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.location-distance {
  margin-left: 12rpx;
  color: #b3aaa1;
  font-size: 22rpx;
}

.location-btn {
  flex-shrink: 0;
  padding: 14rpx 24rpx;
  border-radius: 999rpx;
  background: #111111;
  color: #ffffff;
  font-size: 22rpx;
}

.location-address {
  margin-top: 22rpx;
  color: #9c948b;
  font-size: 22rpx;
  line-height: 1.5;
}

</style>
