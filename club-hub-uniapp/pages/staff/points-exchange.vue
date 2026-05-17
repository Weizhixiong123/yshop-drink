<template>
  <view class="page-wrap" v-if="member">
    <view class="hero">
      <view class="custom-nav">
        <view class="nav-back" @tap="goBack">
          <view class="back-arrow"></view>
        </view>
        <view class="nav-title">积分兑换</view>
      </view>

      <view class="member-card">
        <image src="/static/texas_bar_bg.png" mode="aspectFill" class="member-card-bg" />
        <view class="member-card-mask"></view>
        <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
        <view class="member-info">
          <view class="member-name">{{ member.name }}</view>
          <view class="member-phone">{{ getDisplayPhone() }}</view>
          <view class="level-tag">
            <text class="tag-gem">◆</text>
            <text>{{ levelLabel(member.level) }}</text>
          </view>
        </view>
        <view class="card-divider"></view>
        <view class="points-total">
          <view class="total-label">当前积分</view>
          <view class="total-value">{{ member.points }}</view>
        </view>
      </view>
    </view>

    <view class="content">
      <view class="section-title">请选择要兑换的套餐</view>

      <view class="plan-list">
        <view
          v-for="plan in packagePlans"
          :key="plan.amount"
          class="plan-card"
          :class="{ recommend: plan.recommend }"
        >
          <view class="recommend-badge" v-if="plan.recommend">推荐</view>
          <view class="plan-icon">
            <view class="icon-mark" :class="plan.icon">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="plan-copy">
            <view class="plan-title">{{ plan.amount }}套餐</view>
            <view class="plan-desc">赠送{{ plan.chipsLabel }}筹码 / {{ plan.cost }}分</view>
          </view>
          <view class="exchange-btn" @tap="confirmExchange(plan)">立即兑换</view>
        </view>
      </view>

      <view class="tip-card">
        <view class="tip-icon">♢</view>
        <view>兑换后系统将自动扣减积分并生成兑换记录，请操作前确认会员信息。</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { memberDetail, memberPointsExchange } from '@/api/staff'

const member = ref(null)
const memberId = ref('')
const submitting = ref(false)

const packagePlans = [
  { amount: 128, cost: 200, chipsLabel: '1万', multiplier: 1, icon: 'icon-bottle' },
  { amount: 228, cost: 400, chipsLabel: '1万', multiplier: 1, icon: 'icon-cheers', recommend: true },
  { amount: 328, cost: 600, chipsLabel: '1万', multiplier: 1, icon: 'icon-wine-set' },
  { amount: 498, cost: 1200, chipsLabel: '1万', multiplier: 1, icon: 'icon-party' }
]

onLoad((options) => {
  memberId.value = options.id || ''
  if (memberId.value) {
    loadMember(memberId.value)
  }
})

const loadMember = async (id) => {
  try {
    const data = await memberDetail(id)
    if (data) {
      member.value = {
        ...data,
        points: data.points || 0,
        level: data.level || 'normal'
      }
    }
  } catch (err) {
    console.error('[memberDetail] error:', err)
    uni.showToast({ title: '加载会员信息失败', icon: 'none' })
  }
}

const goBack = () => {
  uni.navigateBack({
    delta: 1,
    fail: () => uni.redirectTo({ url: `/pages/staff/detail?id=${memberId.value}` })
  })
}

const getDisplayPhone = () => {
  const phone = String(member.value?.phone || '')
  if (!phone) return '未知'
  return phone.length >= 4 ? `*****${phone.slice(-4)}` : phone
}

const levelLabel = (level) => ({
  normal: '普通会员',
  gold: '黄金会员',
  platinum: '白金会员',
  black_gold: '黑金会员',
  black_diamond: '黑钻会员'
}[level] || '普通会员')

const confirmExchange = (plan) => {
  if (submitting.value) return
  if (Number(member.value?.points || 0) < Number(plan.cost || 0)) {
    uni.showToast({ title: '当前积分不足', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认兑换',
    content: `${member.value?.name || '会员'} 将兑换 ${plan.amount} 套餐，赠送${plan.chipsLabel}筹码，扣减 ${plan.cost} 积分。是否确认？`,
    confirmText: '确认兑换',
    cancelText: '取消',
    success: ({ confirm }) => {
      if (confirm) {
        exchangePlan(plan)
      }
    }
  })
}

const exchangePlan = async (plan) => {
  if (submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '兑换中...' })
  try {
    await memberPointsExchange({
      memberId: Number(memberId.value),
      packageAmount: plan.amount,
      multiplier: plan.multiplier
    })
    uni.hideLoading()
    uni.showToast({ title: '兑换成功', icon: 'success' })
    await loadMember(memberId.value)
  } catch (err) {
    uni.hideLoading()
    console.error('[memberPointsExchange] error:', err)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f8f3ec;
  padding-bottom: calc(34rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.hero {
  padding: var(--status-bar-height) 22rpx 34rpx;
  background: #030303;
}

.custom-nav {
  height: 104rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  color: #ffffff;
  font-size: 38rpx;
  font-weight: 500;
}

.nav-back {
  position: absolute;
  left: 0;
  top: 12rpx;
  width: 78rpx;
  height: 78rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-arrow {
  width: 30rpx;
  height: 30rpx;
  border-left: 7rpx solid #ffffff;
  border-bottom: 7rpx solid #ffffff;
  transform: rotate(45deg);
}

.member-card {
  position: relative;
  display: grid;
  grid-template-columns: 116rpx minmax(0, 1fr) 1rpx 178rpx;
  align-items: center;
  gap: 26rpx;
  min-height: 188rpx;
  padding: 28rpx 34rpx;
  border-radius: 14rpx;
  border: 1rpx solid rgba(226, 187, 143, 0.72);
  background: #080706;
  box-sizing: border-box;
  overflow: hidden;
}

.member-card-bg,
.member-card-mask {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.member-card-bg {
  z-index: 0;
}

.member-card-mask {
  z-index: 1;
  background: linear-gradient(100deg, rgba(8, 7, 6, 0.96) 0%, rgba(39, 29, 23, 0.9) 55%, rgba(17, 14, 12, 0.97) 100%);
}

.avatar,
.member-info,
.card-divider,
.points-total {
  position: relative;
  z-index: 2;
}

.avatar {
  width: 116rpx;
  height: 116rpx;
  border-radius: 50%;
  border: 5rpx solid #f3dfbd;
  background: #030303;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 54rpx;
  font-weight: 900;
  box-shadow: 0 0 22rpx rgba(237, 196, 130, 0.24);
}

.member-name {
  color: #ffffff;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.2;
}

.member-phone {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.68);
  font-size: 28rpx;
}

.level-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  height: 44rpx;
  margin-top: 14rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #ffe9bd;
  font-size: 26rpx;
  font-weight: 800;
  background: rgba(4, 4, 4, 0.72);
  border: 1rpx solid rgba(211, 176, 124, 0.36);
}

.tag-gem {
  color: #d4ad72;
  font-size: 18rpx;
}

.card-divider {
  width: 1rpx;
  height: 104rpx;
  background: rgba(234, 214, 188, 0.36);
}

.points-total {
  text-align: center;
}

.total-label {
  color: #e2c69d;
  font-size: 30rpx;
  font-weight: 700;
}

.total-value {
  margin-top: 18rpx;
  color: #f5d8a8;
  font-size: 66rpx;
  font-weight: 900;
  line-height: 1;
}

.content {
  padding: 36rpx 26rpx 0;
  border-radius: 24rpx 24rpx 0 0;
  background: #fbf8f2;
}

.section-title {
  position: relative;
  padding-left: 28rpx;
  color: #090807;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.2;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4rpx;
  width: 7rpx;
  height: 34rpx;
  border-radius: 99rpx;
  background: linear-gradient(180deg, #dfb674 0%, #b1762a 100%);
}

.plan-list {
  display: flex;
  flex-direction: column;
  gap: 26rpx;
  margin-top: 30rpx;
}

.plan-card {
  position: relative;
  display: grid;
  grid-template-columns: 132rpx minmax(0, 1fr) 168rpx;
  align-items: center;
  gap: 28rpx;
  min-height: 178rpx;
  padding: 30rpx 34rpx;
  border-radius: 14rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 32rpx rgba(69, 43, 20, 0.07);
  border: 1rpx solid rgba(230, 216, 203, 0.66);
  overflow: hidden;
}

.plan-card.recommend {
  border-color: #c79555;
}

.recommend-badge {
  position: absolute;
  right: -2rpx;
  top: -2rpx;
  width: 88rpx;
  height: 56rpx;
  border-radius: 0 14rpx 0 24rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #d6a565 0%, #b97b36 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.plan-icon {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  color: #b88645;
  background: #f5eadc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-title {
  color: #060606;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.2;
}

.plan-desc {
  margin-top: 18rpx;
  color: #777370;
  font-size: 30rpx;
  line-height: 1.2;
}

.exchange-btn {
  width: 154rpx;
  height: 72rpx;
  border-radius: 9rpx;
  border: 1rpx solid #c79555;
  color: #f1cea0;
  background: linear-gradient(145deg, #27221b 0%, #030303 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 22rpx rgba(48, 28, 8, 0.22);
}

.exchange-btn:active {
  transform: scale(0.98);
  opacity: 0.86;
}

.tip-card {
  display: grid;
  grid-template-columns: 62rpx minmax(0, 1fr);
  align-items: center;
  gap: 20rpx;
  min-height: 106rpx;
  margin-top: 30rpx;
  padding: 22rpx 28rpx;
  border-radius: 14rpx;
  border: 1rpx solid rgba(218, 190, 157, 0.72);
  color: #2d2925;
  background: linear-gradient(100deg, #f3e6d5 0%, #fffaf2 100%);
  box-sizing: border-box;
  font-size: 26rpx;
  line-height: 1.6;
}

.tip-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  color: #b18448;
  background: #f7eddf;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-mark {
  position: relative;
  width: 52rpx;
  height: 52rpx;
  color: currentColor;
  flex: 0 0 52rpx;
}

.icon-part {
  display: none;
  position: absolute;
  box-sizing: border-box;
  border-color: currentColor;
}

.icon-bottle .part-a,
.icon-wine-set .part-a,
.icon-party .part-a {
  display: block;
  left: 16rpx;
  top: 16rpx;
  width: 16rpx;
  height: 31rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx 5rpx 8rpx 8rpx;
}

.icon-bottle .part-b,
.icon-wine-set .part-b,
.icon-party .part-b {
  display: block;
  left: 19rpx;
  top: 5rpx;
  width: 9rpx;
  height: 15rpx;
  border: 3rpx solid currentColor;
  border-bottom: 0;
  border-radius: 4rpx 4rpx 0 0;
}

.icon-bottle .part-c,
.icon-wine-set .part-c,
.icon-party .part-c {
  display: block;
  left: 20rpx;
  top: 31rpx;
  width: 8rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-bottle .part-d {
  display: block;
  right: 5rpx;
  top: 24rpx;
  width: 14rpx;
  height: 21rpx;
  border: 3rpx solid currentColor;
  border-radius: 2rpx 2rpx 8rpx 8rpx;
}

.icon-cheers .part-a,
.icon-cheers .part-d,
.icon-party .part-d,
.icon-party .part-e,
.icon-party .part-f {
  display: block;
  top: 14rpx;
  width: 14rpx;
  height: 19rpx;
  border: 3rpx solid currentColor;
  border-radius: 3rpx 3rpx 8rpx 8rpx;
}

.icon-cheers .part-a {
  left: 7rpx;
  transform: rotate(-12deg);
}

.icon-cheers .part-d {
  right: 8rpx;
  transform: rotate(12deg);
}

.icon-cheers .part-b,
.icon-cheers .part-e {
  display: block;
  top: 33rpx;
  width: 0;
  height: 11rpx;
  border-left: 3rpx solid currentColor;
}

.icon-cheers .part-b {
  left: 14rpx;
  transform: rotate(-12deg);
}

.icon-cheers .part-e {
  right: 15rpx;
  transform: rotate(12deg);
}

.icon-cheers .part-c,
.icon-cheers .part-f {
  display: block;
  top: 44rpx;
  width: 15rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-cheers .part-c {
  left: 7rpx;
}

.icon-cheers .part-f {
  right: 8rpx;
}

.icon-wine-set .part-d {
  display: block;
  right: 6rpx;
  top: 17rpx;
  width: 22rpx;
  height: 25rpx;
  border: 3rpx solid currentColor;
  border-radius: 4rpx;
}

.icon-wine-set .part-e,
.icon-wine-set .part-f {
  display: block;
  right: 10rpx;
  width: 14rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-wine-set .part-e {
  top: 25rpx;
}

.icon-wine-set .part-f {
  top: 34rpx;
}

.icon-party .part-d {
  left: 24rpx;
  width: 10rpx;
}

.icon-party .part-e {
  left: 35rpx;
  width: 10rpx;
}

.icon-party .part-f {
  left: 46rpx;
  width: 10rpx;
}
</style>
