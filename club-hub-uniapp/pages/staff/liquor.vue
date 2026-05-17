<template>
  <view class="page-wrap" v-if="member">
    <view class="hero">
      <view class="custom-nav">
        <view class="nav-back" @tap="goBack">
          <view class="back-arrow"></view>
        </view>
        <view class="nav-title">存酒管理</view>
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
        <view class="wine-total">
          <view class="total-label">当前存酒</view>
          <view class="total-value">{{ member.liquor }}<text>瓶</text></view>
        </view>
      </view>
    </view>

    <view class="content">
      <view class="section-title">请选择操作</view>
      <view class="action-grid">
        <view class="action-card" :class="{ active: opType === 'add' }" @tap="selectType('add')">
          <view class="action-icon">
            <view class="icon-mark icon-bottle-arrow">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view>存入酒水</view>
        </view>
        <view class="action-card" :class="{ active: opType === 'sub' }" @tap="selectType('sub')">
          <view class="action-icon">
            <view class="icon-mark icon-bottle-arrow">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view>取出酒水</view>
        </view>
      </view>

      <view class="panel">
        <view class="sub-title">数量选择</view>
        <label class="group-buy-row" v-if="opType === 'add'">
          <checkbox :checked="isGroupBuy" @tap.stop="toggleGroupBuy" />
          <view>
            <view class="group-buy-title">线上团购散客订单</view>
            <view class="group-buy-desc">勾选后不扣会员储值，按酒水数量统计团购营收</view>
          </view>
        </label>
        <view class="amount-grid" :class="{ 'group-buy-grid': isGroupBuy && opType === 'add' }">
          <view
            v-for="item in amountOptions"
            :key="item.value"
            class="amount-btn"
            :class="{ active: selectedAmount === item.value && !customMode }"
            @tap="selectAmount(item.value)"
          >
            <text>{{ item.label }}</text>
            <view class="check-dot" v-if="selectedAmount === item.value && !customMode">✓</view>
          </view>
          <view v-if="!isGroupBuy || opType !== 'add'" class="amount-btn" :class="{ active: customMode }" @tap="selectCustom">
            <text>自定义</text>
            <view class="check-dot" v-if="customMode">✓</view>
          </view>
        </view>
        <view class="group-buy-revenue" v-if="isGroupBuy && opType === 'add'">
          团购营收：¥{{ groupBuyRevenue }}
        </view>
        <view class="custom-input" v-if="customMode">
          <input
            type="number"
            v-model="customAmount"
            placeholder="请输入瓶数"
            placeholder-style="color:#a7a09a;"
          />
          <text>瓶</text>
        </view>
      </view>

      <view class="panel">
        <view class="sub-title">操作备注</view>
        <view class="remark-input">
          <view class="icon-mark icon-note">
            <view class="icon-part part-a"></view>
            <view class="icon-part part-b"></view>
            <view class="icon-part part-c"></view>
            <view class="icon-part part-d"></view>
            <view class="icon-part part-e"></view>
            <view class="icon-part part-f"></view>
          </view>
          <input
            v-model="remark"
            maxlength="50"
            placeholder="请输入备注信息（选填）"
            placeholder-style="color:#a7a09a;"
          />
          <text>{{ remark.length }}/50</text>
        </view>
      </view>

      <view class="confirm-btn" @tap="submitOp">{{ confirmText }}</view>

    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { memberDetail, memberOperate, memberGroupBuyWine } from '@/api/staff'

const member = ref(null)
const memberId = ref('')
const opType = ref('add')
const selectedAmount = ref(4)
const customMode = ref(false)
const customAmount = ref('')
const remark = ref('')
const submitting = ref(false)
const isGroupBuy = ref(false)

const normalAmountOptions = [
  { label: '1瓶', value: 1 },
  { label: '2瓶', value: 2 },
  { label: '4瓶', value: 4 }
]

const groupBuyAmountOptions = [
  { label: '2瓶', value: 2 },
  { label: '4瓶', value: 4 },
  { label: '8瓶', value: 8 }
]

const amountOptions = computed(() => isGroupBuy.value && opType.value === 'add' ? groupBuyAmountOptions : normalAmountOptions)

const groupBuyRevenue = computed(() => {
  const amount = getAmount()
  return [2, 4, 8].includes(amount) ? amount * 25 : 0
})

const confirmText = computed(() => opType.value === 'add' ? '确认存入酒水' : '确认取出酒水')

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
        liquor: data.wine != null ? data.wine : (data.liquor || 0),
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

const selectType = (type) => {
  opType.value = type
  if (type === 'sub') {
    isGroupBuy.value = false
  }
}

const selectAmount = (amount) => {
  selectedAmount.value = amount
  customMode.value = false
}

const selectCustom = () => {
  if (isGroupBuy.value && opType.value === 'add') {
    uni.showToast({ title: '团购订单仅支持2瓶、4瓶、8瓶', icon: 'none' })
    return
  }
  customMode.value = true
  customAmount.value = ''
}

const getAmount = () => {
  return customMode.value ? Number(customAmount.value) : Number(selectedAmount.value)
}

const toggleGroupBuy = () => {
  isGroupBuy.value = !isGroupBuy.value
  if (isGroupBuy.value) {
    selectedAmount.value = 2
    customMode.value = false
    customAmount.value = ''
  }
}

const submitOp = async () => {
  const amount = getAmount()
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入有效瓶数', icon: 'none' })
    return
  }
  if (opType.value === 'sub' && amount > Number(member.value?.liquor || 0)) {
    uni.showToast({ title: '取出数量不能大于当前存酒', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    if (opType.value === 'add' && isGroupBuy.value) {
      if (![2, 4, 8].includes(amount)) {
        uni.hideLoading()
        uni.showToast({ title: '团购订单仅支持2瓶、4瓶、8瓶', icon: 'none' })
        return
      }
      await memberGroupBuyWine({
        memberId: Number(memberId.value),
        wineQuantity: amount
      })
      uni.hideLoading()
      uni.showToast({ title: `团购营收¥${groupBuyRevenue.value}`, icon: 'none' })
      remark.value = ''
      await loadMember(memberId.value)
      return
    }

    await memberOperate({
      memberId: Number(memberId.value),
      type: opType.value === 'add' ? 'ADD_WINE' : 'SUB_WINE',
      value: amount
    })
    uni.hideLoading()
    uni.showToast({ title: '操作成功', icon: 'success' })
    remark.value = ''
    await loadMember(memberId.value)
  } catch (err) {
    uni.hideLoading()
    console.error('[memberOperate] error:', err)
  } finally {
    submitting.value = false
  }
}

</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  background: #f8f3ec;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.hero {
  padding: var(--status-bar-height) 22rpx 28rpx;
  background: #030303;
}

.custom-nav {
  height: 96rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  color: #ffffff;
  font-size: 34rpx;
  font-weight: 500;
}

.nav-back {
  position: absolute;
  left: 0;
  top: 8rpx;
  width: 76rpx;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-arrow {
  width: 28rpx;
  height: 28rpx;
  border-left: 7rpx solid #ffffff;
  border-bottom: 7rpx solid #ffffff;
  transform: rotate(45deg);
}

.member-card {
  position: relative;
  display: grid;
  grid-template-columns: 96rpx minmax(0, 1fr) 1rpx 160rpx;
  align-items: center;
  gap: 24rpx;
  min-height: 148rpx;
  padding: 22rpx 26rpx;
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
  background: linear-gradient(100deg, rgba(8, 7, 6, 0.96) 0%, rgba(39, 29, 23, 0.92) 55%, rgba(17, 14, 12, 0.97) 100%);
}

.avatar,
.member-info,
.card-divider,
.wine-total {
  position: relative;
  z-index: 2;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 5rpx solid #f3dfbd;
  background: #030303;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 46rpx;
  font-weight: 900;
  box-shadow: 0 0 22rpx rgba(237, 196, 130, 0.24);
}

.member-name {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.2;
}

.member-phone {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.68);
  font-size: 24rpx;
}

.level-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  height: 42rpx;
  margin-top: 12rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  color: #ffe9bd;
  font-size: 24rpx;
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
  height: 82rpx;
  background: rgba(234, 214, 188, 0.32);
}

.wine-total {
  text-align: center;
}

.total-label {
  color: #e2c69d;
  font-size: 24rpx;
  font-weight: 700;
}

.total-value {
  margin-top: 10rpx;
  color: #f5dfbd;
  font-size: 58rpx;
  font-weight: 900;
  line-height: 1;
  text-shadow: 0 3rpx 0 rgba(0, 0, 0, 0.5);
}

.total-value text {
  margin-left: 8rpx;
  font-size: 24rpx;
}

.content {
  margin-top: -2rpx;
  padding: 30rpx 28rpx 0;
  border-radius: 24rpx 24rpx 0 0;
  background: #fbf8f2;
}

.section-title,
.sub-title {
  position: relative;
  padding-left: 18rpx;
  color: #090807;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.2;
}

.section-title::before,
.sub-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4rpx;
  width: 6rpx;
  height: 28rpx;
  border-radius: 99rpx;
  background: linear-gradient(180deg, #dfb674 0%, #b1762a 100%);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 34rpx;
  margin-top: 26rpx;
}

.action-card {
  height: 112rpx;
  border-radius: 12rpx;
  border: 2rpx solid rgba(196, 161, 119, 0.68);
  color: #b98a4b;
  background: #fffdf9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18rpx;
  font-size: 30rpx;
  font-weight: 800;
  box-shadow: 0 10rpx 28rpx rgba(69, 43, 20, 0.06);
}

.action-card.active {
  color: #e7c69a;
  background: linear-gradient(145deg, #252019 0%, #030303 100%);
  border-color: #c79655;
  box-shadow: 0 12rpx 28rpx rgba(48, 28, 8, 0.22);
}

.action-icon {
  color: currentColor;
}

.panel {
  margin-top: 26rpx;
  padding: 26rpx 24rpx;
  border-radius: 14rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 30rpx rgba(69, 43, 20, 0.06);
}

.group-buy-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 22rpx;
  padding: 18rpx 20rpx;
  border-radius: 12rpx;
  border: 1rpx solid #ead6bd;
  background: #fffaf4;
  box-sizing: border-box;
}

.group-buy-title {
  color: #241b14;
  font-size: 27rpx;
  font-weight: 900;
  line-height: 1.2;
}

.group-buy-desc {
  margin-top: 7rpx;
  color: #9a8064;
  font-size: 23rpx;
  line-height: 1.3;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22rpx;
  margin-top: 24rpx;
}

.amount-grid.group-buy-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.amount-btn {
  position: relative;
  height: 74rpx;
  border-radius: 12rpx;
  border: 2rpx solid #deccb8;
  color: #221b17;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  background: #fffdf9;
  box-sizing: border-box;
  overflow: hidden;
}

.amount-btn.active {
  color: #f0c992;
  background: linear-gradient(145deg, #211c16 0%, #030303 100%);
  border-color: #c79655;
  box-shadow: 0 8rpx 18rpx rgba(48, 28, 8, 0.18);
}

.check-dot {
  position: absolute;
  right: 0;
  top: 0;
  width: 36rpx;
  height: 32rpx;
  border-radius: 0 8rpx 0 16rpx;
  background: #f3d299;
  color: #030303;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 900;
}

.group-buy-revenue {
  margin-top: 18rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background: #15110e;
  color: #f3d299;
  font-size: 26rpx;
  font-weight: 900;
  text-align: center;
}

.custom-input {
  display: flex;
  align-items: center;
  height: 76rpx;
  margin-top: 22rpx;
  padding: 0 22rpx;
  border-radius: 12rpx;
  border: 1rpx solid #eadfd3;
  background: #fffdf9;
  color: #9b7040;
  font-size: 26rpx;
}

.custom-input input {
  flex: 1;
  height: 100%;
  color: #15110e;
  font-size: 28rpx;
}

.remark-input {
  display: grid;
  grid-template-columns: 34rpx minmax(0, 1fr) 72rpx;
  align-items: center;
  gap: 12rpx;
  height: 68rpx;
  margin-top: 20rpx;
  padding: 0 18rpx;
  border-radius: 10rpx;
  border: 1rpx solid #eadfd3;
  background: #fffdf9;
  color: #b99467;
  box-sizing: border-box;
}

.remark-input input {
  height: 100%;
  color: #15110e;
  font-size: 26rpx;
}

.remark-input text {
  color: #9f9a96;
  font-size: 22rpx;
  text-align: right;
}

.confirm-btn {
  height: 88rpx;
  margin-top: 24rpx;
  border-radius: 14rpx;
  color: #fff5e5;
  background: linear-gradient(145deg, #2b241b 0%, #050505 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 900;
  box-shadow: 0 14rpx 30rpx rgba(48, 28, 8, 0.2);
}

.icon-mark {
  position: relative;
  width: 36rpx;
  height: 36rpx;
  color: currentColor;
  flex: 0 0 36rpx;
}

.icon-part {
  display: none;
  position: absolute;
  box-sizing: border-box;
  border-color: currentColor;
}

.icon-bottle-arrow .part-a {
  display: block;
  left: 4rpx;
  top: 9rpx;
  width: 13rpx;
  height: 24rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx 5rpx 8rpx 8rpx;
}

.icon-bottle-arrow .part-b {
  display: block;
  left: 7rpx;
  top: 2rpx;
  width: 7rpx;
  height: 11rpx;
  border: 3rpx solid currentColor;
  border-bottom: 0;
  border-radius: 4rpx 4rpx 0 0;
}

.icon-bottle-arrow .part-c {
  display: block;
  left: 16rpx;
  top: 17rpx;
  width: 16rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-bottle-arrow .part-d,
.icon-bottle-arrow .part-e {
  display: block;
  right: 2rpx;
  top: 13rpx;
  width: 10rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
  transform-origin: right center;
}

.icon-bottle-arrow .part-d {
  transform: rotate(38deg);
}

.icon-bottle-arrow .part-e {
  transform: rotate(-38deg);
}

.icon-note .part-a {
  display: block;
  left: 3rpx;
  top: 2rpx;
  width: 22rpx;
  height: 28rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx;
}

.icon-note .part-b {
  display: block;
  left: 20rpx;
  top: 2rpx;
  width: 8rpx;
  height: 8rpx;
  border-left: 3rpx solid currentColor;
  border-bottom: 3rpx solid currentColor;
  background: #fffdf9;
  transform: rotate(-45deg);
}

.icon-note .part-c,
.icon-note .part-d {
  display: block;
  left: 9rpx;
  width: 10rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
  border-radius: 3rpx;
}

.icon-note .part-c {
  top: 12rpx;
}

.icon-note .part-d {
  top: 19rpx;
}
</style>
