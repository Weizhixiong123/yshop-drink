<template>
  <view class="page-wrap" v-if="member">
    <view class="custom-nav">
      <view class="nav-back" @tap="goBack">
        <view class="back-arrow"></view>
      </view>
      <view class="nav-title">快捷消费套餐</view>
    </view>

    <view class="content">
      <view class="member-card" @tap="openBalanceManage">
        <view class="avatar-wrap">
          <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
        </view>
        <view class="member-info">
          <view class="member-name">{{ member.name }}</view>
          <view class="member-phone">{{ getDisplayPhone() }}</view>
          <view class="level-tag">
            <text class="tag-gem">◆</text>
            <text>{{ levelLabel(member.level) }}</text>
          </view>
        </view>
        <view class="card-divider"></view>
        <view class="balance-total">
          <view class="total-label">当前储值</view>
          <view class="total-value">¥{{ member.balance }}</view>
        </view>
        <view class="chevron"></view>
      </view>

      <view class="section-title">请选择要执行的消费套餐</view>

      <view class="plan-list">
        <view
          v-for="plan in packagePlans"
          :key="plan.amount"
          class="plan-card"
          :class="{ selected: selectedAmount === plan.amount, recommend: plan.recommend }"
          @tap="selectPlan(plan.amount)"
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
            <view class="plan-desc">扣¥{{ plan.amount }} / 加{{ plan.wine }}酒 / {{ plan.chips }}千</view>
          </view>
          <view
            class="plan-btn"
            :class="{ active: selectedAmount === plan.amount }"
            @tap.stop="handlePlanButton(plan)"
          >{{ selectedAmount === plan.amount ? '立即执行' : '选择套餐' }}</view>
        </view>
      </view>

      <view class="tip-card">
        <view class="tip-icon">♢</view>
        <view>套餐执行后将自动扣减余额并增加对应存酒/筹码，请操作前确认会员信息。</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { memberDetail, memberPackageConsume } from '@/api/staff'

const member = ref(null)
const memberId = ref('')
const selectedAmount = ref(228)
const submitting = ref(false)

const packagePlans = [
  { amount: 128, wine: 4, chips: 10, icon: 'icon-bottle' },
  { amount: 228, wine: 8, chips: 20, icon: 'icon-cheers', recommend: true },
  { amount: 328, wine: 12, chips: 30, icon: 'icon-wine-set' },
  { amount: 498, wine: 20, chips: 60, icon: 'icon-party' }
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
        balance: data.balance != null ? Number(data.balance).toFixed(2) : '0.00',
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

const selectPlan = (amount) => {
  selectedAmount.value = amount
}

const handlePlanButton = (plan) => {
  if (selectedAmount.value !== plan.amount) {
    selectPlan(plan.amount)
    return
  }
  consumePlan(plan)
}

const consumePlan = async (plan) => {
  if (submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '执行中...' })
  try {
    await memberPackageConsume({
      memberId: Number(memberId.value),
      packageAmount: plan.amount
    })
    uni.hideLoading()
    uni.showToast({ title: '套餐执行成功', icon: 'success' })
    await loadMember(memberId.value)
  } catch (err) {
    uni.hideLoading()
    console.error('[memberPackageConsume] error:', err)
  } finally {
    submitting.value = false
  }
}

const openBalanceManage = () => {
  uni.showToast({ title: `当前储值 ¥${member.value?.balance || '0.00'}`, icon: 'none' })
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f7f2eb;
  padding-bottom: calc(34rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.custom-nav {
  height: calc(104rpx + var(--status-bar-height));
  padding-top: var(--status-bar-height);
  box-sizing: border-box;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.nav-title {
  color: #050505;
  font-size: 34rpx;
  font-weight: 500;
}

.nav-back {
  position: absolute;
  left: 22rpx;
  bottom: 14rpx;
  width: 76rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-arrow {
  width: 30rpx;
  height: 30rpx;
  border-left: 7rpx solid #050505;
  border-bottom: 7rpx solid #050505;
  transform: rotate(45deg);
}

.content {
  padding: 30rpx 26rpx 0;
}

.member-card {
  display: grid;
  grid-template-columns: 122rpx minmax(0, 1fr) 1rpx 206rpx 24rpx;
  align-items: center;
  gap: 24rpx;
  min-height: 172rpx;
  padding: 26rpx 28rpx;
  border-radius: 16rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 34rpx rgba(69, 43, 20, 0.08);
  border: 1rpx solid rgba(230, 216, 203, 0.72);
  box-sizing: border-box;
}

.avatar-wrap {
  position: relative;
  width: 110rpx;
  height: 110rpx;
}

.avatar-wrap::before {
  content: '';
  position: absolute;
  left: -8rpx;
  top: -8rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #d8b478;
  box-shadow: 0 0 0 6rpx #f7ead6;
  z-index: 2;
}

.avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  border: 3rpx solid #d8b478;
  background: #030303;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50rpx;
  font-weight: 900;
  box-shadow: 0 10rpx 24rpx rgba(0, 0, 0, 0.12);
}

.member-name {
  color: #050505;
  font-size: 40rpx;
  font-weight: 900;
  line-height: 1.2;
}

.member-phone {
  margin-top: 8rpx;
  color: #6d6864;
  font-size: 27rpx;
}

.level-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  height: 42rpx;
  margin-top: 14rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #ffe9bd;
  font-size: 25rpx;
  font-weight: 800;
  background: linear-gradient(145deg, #2a241d 0%, #030303 100%);
}

.tag-gem {
  color: #d4ad72;
  font-size: 18rpx;
}

.card-divider {
  width: 1rpx;
  height: 104rpx;
  background: #eee3d7;
}

.total-label {
  color: #5c5753;
  font-size: 28rpx;
  line-height: 1.2;
}

.total-value {
  margin-top: 18rpx;
  color: #050505;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1;
  white-space: nowrap;
}

.chevron {
  width: 18rpx;
  height: 18rpx;
  border-top: 4rpx solid #bda890;
  border-right: 4rpx solid #bda890;
  transform: rotate(45deg);
  box-sizing: border-box;
}

.section-title {
  position: relative;
  margin-top: 40rpx;
  padding-left: 26rpx;
  color: #050505;
  font-size: 34rpx;
  font-weight: 500;
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
  grid-template-columns: 132rpx minmax(0, 1fr) 166rpx;
  align-items: center;
  gap: 28rpx;
  min-height: 172rpx;
  padding: 30rpx 34rpx;
  border-radius: 16rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 32rpx rgba(69, 43, 20, 0.07);
  border: 1rpx solid rgba(230, 216, 203, 0.68);
  overflow: hidden;
  box-sizing: border-box;
}

.plan-card.selected {
  border-color: #c79555;
}

.recommend-badge {
  position: absolute;
  right: -2rpx;
  top: -2rpx;
  width: 90rpx;
  height: 58rpx;
  border-radius: 0 16rpx 0 26rpx;
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
  color: #050505;
  font-size: 40rpx;
  font-weight: 900;
  line-height: 1.2;
}

.plan-desc {
  margin-top: 20rpx;
  color: #5f5b57;
  font-size: 28rpx;
  line-height: 1.25;
}

.plan-btn {
  width: 150rpx;
  height: 68rpx;
  border-radius: 9rpx;
  border: 2rpx solid #d0a66f;
  color: #b88645;
  background: #fffdf9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.plan-btn.active {
  border-color: #c79555;
  color: #f1cea0;
  background: linear-gradient(145deg, #27221b 0%, #030303 100%);
  box-shadow: 0 10rpx 22rpx rgba(48, 28, 8, 0.22);
}

.plan-btn:active,
.plan-card:active,
.member-card:active {
  transform: scale(0.99);
  opacity: 0.88;
}

.tip-card {
  display: grid;
  grid-template-columns: 62rpx minmax(0, 1fr);
  align-items: center;
  gap: 20rpx;
  min-height: 106rpx;
  margin-top: 34rpx;
  padding: 22rpx 30rpx;
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
