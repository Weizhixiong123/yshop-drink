<template>
  <layout>
    <view class="home-page">
      <view class="hero-section">
        <image class="hero-image" src="/static/home-hero.png" mode="aspectFill"></image>
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

          <view class="feature-grid">
            <view class="feature-item feature-border-right feature-border-bottom" @tap="takein">
              <view class="feature-icon cocktail-icon">
                <view class="cocktail-cup"></view>
                <view class="cocktail-stem"></view>
                <view class="cocktail-base"></view>
                <view class="cocktail-olive"></view>
              </view>
              <view class="feature-title">堂食点单</view>
              <view class="feature-subtitle">ORDER</view>
            </view>

            <view class="feature-item feature-border-bottom" @tap="openStorage">
              <view class="feature-icon storage-icon">
                <view class="storage-glass storage-glass-left"></view>
                <view class="storage-bottle">
                  <view class="storage-cap"></view>
                  <view class="storage-label"></view>
                </view>
                <view class="storage-glass storage-glass-right"></view>
              </view>
              <view class="feature-title">存酒</view>
              <view class="feature-subtitle">STORING</view>
            </view>

            <view class="feature-item feature-border-right" @tap="goCoupons">
              <view class="feature-icon coupon-icon">
                <view class="coupon-body"></view>
                <view class="coupon-cut coupon-cut-left"></view>
                <view class="coupon-cut coupon-cut-right"></view>
                <view class="coupon-mark"></view>
              </view>
              <view class="feature-title">卡券中心</view>
              <view class="feature-subtitle">COUPONS</view>
            </view>

            <view class="feature-item" @tap="openRecharge">
              <view class="feature-icon vip-icon">
                <view class="vip-card">VIP</view>
              </view>
              <view class="feature-title">会员充值</view>
              <view class="feature-subtitle">RECHARGE</view>
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
}

.feature-grid {
  display: flex;
  flex-wrap: wrap;
  padding-bottom: 6rpx;
}

.feature-item {
  box-sizing: border-box;
  width: 50%;
  padding: 32rpx 18rpx 34rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.feature-border-right {
  border-right: 1px dashed #d5c8bb;
}

.feature-border-bottom {
  border-bottom: 1px solid #f0e6da;
}

.feature-icon {
  position: relative;
  width: 108rpx;
  height: 108rpx;
}

.feature-title {
  margin-top: 10rpx;
  color: #17120f;
  font-size: 28rpx;
  font-weight: 600;
}

.feature-subtitle {
  margin-top: 6rpx;
  color: #b7b0a8;
  font-size: 18rpx;
  letter-spacing: 2rpx;
}

.cocktail-cup {
  position: absolute;
  top: 20rpx;
  left: 24rpx;
  width: 58rpx;
  height: 34rpx;
  border-bottom: 6rpx solid #101010;
  border-left: 6rpx solid #101010;
  border-right: 6rpx solid #101010;
  transform: skew(-18deg);
}

.cocktail-cup::after {
  content: '';
  position: absolute;
  top: -6rpx;
  left: -10rpx;
  width: 78rpx;
  height: 6rpx;
  background: #101010;
  transform: skew(18deg);
}

.cocktail-stem {
  position: absolute;
  top: 62rpx;
  left: 56rpx;
  width: 6rpx;
  height: 28rpx;
  background: #101010;
}

.cocktail-base {
  position: absolute;
  top: 92rpx;
  left: 34rpx;
  width: 50rpx;
  height: 6rpx;
  background: #101010;
}

.cocktail-olive {
  position: absolute;
  top: 18rpx;
  right: 18rpx;
  width: 16rpx;
  height: 16rpx;
  border: 4rpx solid #101010;
  border-radius: 50%;
}

.storage-icon {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8rpx;
}

.storage-bottle {
  position: relative;
  width: 40rpx;
  height: 74rpx;
  border: 6rpx solid #101010;
  border-radius: 10rpx 10rpx 12rpx 12rpx;
}

.storage-cap {
  position: absolute;
  top: -18rpx;
  left: 8rpx;
  width: 18rpx;
  height: 18rpx;
  border: 6rpx solid #101010;
  border-bottom: 0;
  border-radius: 8rpx 8rpx 0 0;
}

.storage-label {
  position: absolute;
  top: 24rpx;
  left: 8rpx;
  width: 16rpx;
  height: 20rpx;
  border: 4rpx solid #101010;
}

.storage-glass {
  position: relative;
  width: 24rpx;
  height: 34rpx;
  border: 5rpx solid #101010;
  border-top: 0;
  border-radius: 0 0 10rpx 10rpx;
}

.storage-glass::after {
  content: '';
  position: absolute;
  right: 5rpx;
  bottom: -20rpx;
  left: 5rpx;
  height: 5rpx;
  background: #101010;
}

.storage-glass-left {
  margin-right: 4rpx;
}

.storage-glass-right {
  margin-left: 4rpx;
}

.coupon-body {
  position: absolute;
  top: 28rpx;
  left: 14rpx;
  width: 92rpx;
  height: 54rpx;
  border: 6rpx solid #101010;
  border-radius: 14rpx;
}

.coupon-cut {
  position: absolute;
  top: 46rpx;
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: #ffffff;
  border: 6rpx solid #101010;
}

.coupon-cut-left {
  left: 4rpx;
}

.coupon-cut-right {
  right: 4rpx;
}

.coupon-mark {
  position: absolute;
  top: 42rpx;
  left: 36rpx;
  width: 48rpx;
  height: 6rpx;
  background: #101010;
  box-shadow: 0 16rpx 0 #101010;
}

.vip-card {
  position: absolute;
  top: 32rpx;
  left: 14rpx;
  width: 92rpx;
  height: 56rpx;
  border-radius: 12rpx;
  background: #101010;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 56rpx;
  text-align: center;
  letter-spacing: 2rpx;
}

.home-notice,
.booking-card,
.benefit-card,
.location-card {
  margin-top: 18rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 24rpx rgba(40, 28, 17, 0.06);
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
