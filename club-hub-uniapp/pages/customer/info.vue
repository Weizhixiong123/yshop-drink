<template>
  <view class="page">
    <!-- Top Image Background -->
    <view class="hero-bg">
      <image src="/static/texas_bar_bg.png" mode="aspectFill" class="hero-image" />
      <view class="hero-overlay"></view>
      
      <!-- Brand in the center of the image -->
      <view class="hero-brand">
        <view class="logo">醉<text class="spade">♠</text>岛</view>
        <view class="subtitle">TEXAS HOLD'EM BAR</view>
        <view class="slogan">FOLLOW YOUR HEART</view>
      </view>
    </view>

    <!-- Bottom White Panel -->
    <view class="panel">
      <!-- Welcome Header -->
      <view class="panel-header">
        <view class="welcome-text">
          <view class="greeting">欢迎回来</view>
          <view class="name">{{ displayName }}</view>
          <view class="member-level">
            <text>{{ levelLabel(info.level) }}</text>
            <text class="help-icon" @tap="showLevelModal = true">?</text>
          </view>
        </view>
      </view>

      <!-- Member Assets -->
      <view class="asset-card">
        <view class="asset-top">
          <view>
            <view class="asset-title">会员资产</view>
            <view class="asset-subtitle">ID {{ memberCode }}</view>
          </view>
          <view class="asset-mark">♠</view>
        </view>

        <view class="balance-row">
          <view class="field-label">余额<text class="en">BALANCE</text></view>
          <view class="balance-value">¥{{ formatMoney(info.balance) }}</view>
        </view>
        <view class="split-balance">
          <view>本金 ¥{{ formatMoney(info.principalBalance) }}</view>
          <view>赠送 ¥{{ formatMoney(info.bonusBalance) }}</view>
        </view>

        <view class="mini-stats">
          <view class="mini-stat">
            <view class="mini-value">{{ info.wine || 0 }}</view>
            <view class="field-label">存酒<text class="en">WINE</text></view>
          </view>
          <view class="mini-divider"></view>
          <view class="mini-stat">
            <view class="mini-value">{{ formatMoney(info.points) }}</view>
            <view class="field-label">积分<text class="en">POINTS</text></view>
          </view>
        </view>
      </view>
    </view>

    <view class="modal-mask" v-if="showLevelModal" @tap="showLevelModal = false"></view>
    <view class="level-modal" v-if="showLevelModal">
      <view class="modal-title">会员等级说明</view>
      <view class="rule-line">黄金会员：单笔充值本金满2000元，或单月累计充值本金满4000元即可自动升级</view>
      <view class="rule-line">白金会员：单笔充值本金满5000元即可升级</view>
      <view class="rule-line">温馨提示：会员升级仅统计实际充值本金，赠送金额不参与等级晋升</view>
      <view class="modal-btn" @tap="showLevelModal = false">知道了</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onHide, onLoad, onShareAppMessage, onShareTimeline, onShow, onUnload } from '@dcloudio/uni-app'
import { customerInfo } from '@/api/customer'
import cookie from '@/utils/cookie'
import { connectCustomerSocket, disconnectCustomerSocket } from '@/utils/staffSocket'

const CUSTOMER_SHARE_PATH = '/pages/customer/member-login'
const CUSTOMER_SHARE_TITLE = '醉岛 Bar 会员服务'
const CUSTOMER_SHARE_IMAGE = '/static/texas_bar_bg.png'

const loading = ref(false)
const info = ref({})
const pageVisible = ref(false)
const showLevelModal = ref(false)

const formatMoney = (value) => {
  const numberValue = Number(value || 0)
  return numberValue.toFixed(2)
}

const memberCode = computed(() => {
  const id = info.value.id || info.value.memberId || info.value.member_id
  return id ? String(id).padStart(6, '0') : '--'
})

const displayName = computed(() => {
  const name = String(info.value.name || '').trim()
  if (name && name !== '会员用户') {
    return name
  }

  const phone = String(info.value.phone || cookie.get('customerPhone') || '')
  const tail = phone.slice(-4)
  return tail ? `会员_${tail}` : '尊贵的会员'
})

const levelLabel = (level) => ({
  normal: '普通会员',
  gold: '黄金会员',
  platinum: '白金会员',
  black_gold: '黑金会员',
  black_diamond: '黑钻会员'
}[level] || '普通会员')

const enableCustomerShareMenu = () => {
  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline', 'copyUrl']
  })
  // #endif
}

const loadInfo = async () => {
  const phone = cookie.get('customerPhone')
  if (!phone) {
    uni.reLaunch({ url: '/pages/customer/member-login' })
    return
  }
  if (loading.value) return
  loading.value = true

  try {
    info.value = await customerInfo(phone)
  } catch (err) {
    console.error('[customerInfo] error:', err)
  } finally {
    loading.value = false
  }
}

const onMemberDataUpdate = (message = {}) => {
  if (!pageVisible.value) {
    return
  }

  const currentId = info.value.id || info.value.memberId || info.value.member_id
  if (message.memberId && currentId && Number(message.memberId) !== Number(currentId)) {
    return
  }

  loadInfo()
}

onLoad(() => {
  enableCustomerShareMenu()
  uni.$on('MEMBER_DATA_UPDATE', onMemberDataUpdate)
  uni.$on('USER_DATA_UPDATE', onMemberDataUpdate)
})

onShareAppMessage(() => ({
  title: CUSTOMER_SHARE_TITLE,
  path: CUSTOMER_SHARE_PATH,
  imageUrl: CUSTOMER_SHARE_IMAGE
}))

onShareTimeline(() => ({
  title: CUSTOMER_SHARE_TITLE,
  query: '',
  imageUrl: CUSTOMER_SHARE_IMAGE
}))

onShow(() => {
  pageVisible.value = true
  connectCustomerSocket()
  loadInfo()
})

onHide(() => {
  pageVisible.value = false
})

onUnload(() => {
  pageVisible.value = false
  uni.$off('MEMBER_DATA_UPDATE', onMemberDataUpdate)
  uni.$off('USER_DATA_UPDATE', onMemberDataUpdate)
  disconnectCustomerSocket()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f7f7f7;
  overflow-x: hidden;
}

.hero-bg {
  position: relative;
  width: 100%;
  height: 620rpx;
  min-height: 42vh;
  max-height: 48vh;
  z-index: 1;
  overflow: hidden;
  background: #111;

  .hero-image {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
  }

  .hero-overlay {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    /* 减轻遮罩，让背景更亮 */
    background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0.3) 100%);
  }

  .hero-brand {
    position: absolute;
    top: 46%;
    transform: translateY(-50%);
    left: 0; right: 0;
    text-align: center;
    color: #fff;
    
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
}

.panel {
  position: relative;
  z-index: 2;
  background: #ffffff;
  border-radius: 40rpx 40rpx 0 0;
  min-height: calc(100vh - 580rpx);
  padding: 56rpx 40rpx calc(72rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  box-shadow: 0 -10rpx 30rpx rgba(0,0,0,0.15);
  margin-top: -40rpx;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 44rpx;
  
  .welcome-text {
    .greeting {
      font-size: 26rpx;
      color: #666;
      margin-bottom: 8rpx;
      font-weight: bold;
    }
    .name {
      font-size: 42rpx;
      font-weight: 900;
      color: #111;
    }
    .member-level {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-top: 14rpx;
      color: #5b3a22;
      font-size: 24rpx;
      font-weight: 800;
    }
    .help-icon {
      width: 34rpx;
      height: 34rpx;
      border-radius: 50%;
      background: #111;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22rpx;
    }
  }
}

.asset-card {
  position: relative;
  overflow: hidden;
  padding: 34rpx 34rpx 30rpx;
  border-radius: 30rpx;
  background: linear-gradient(145deg, #2f2018 0%, #5b3a22 100%);
  border: 2rpx solid rgba(255, 226, 181, 0.2);
  box-shadow: 0 22rpx 44rpx rgba(60, 39, 24, 0.18);

  &::after {
    content: '';
    position: absolute;
    right: -90rpx;
    top: -120rpx;
    width: 280rpx;
    height: 280rpx;
    border: 2rpx solid rgba(255, 229, 186, 0.08);
    border-radius: 50%;
  }
}

.asset-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 34rpx;
}

.asset-title {
  color: #fff8ea;
  font-size: 29rpx;
  font-weight: 800;
  line-height: 1.2;
}

.asset-subtitle {
  margin-top: 8rpx;
  color: rgba(255, 232, 194, 0.52);
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 3rpx;
}

.asset-mark {
  color: rgba(255, 236, 204, 0.78);
  font-size: 48rpx;
  line-height: 1;
}

.balance-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-bottom: 30rpx;
  border-bottom: 2rpx solid rgba(255, 232, 194, 0.14);
}

.split-balance {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0 4rpx;
  color: rgba(255, 244, 225, 0.86);
  font-size: 24rpx;
  font-weight: 800;
  border-bottom: 2rpx solid rgba(255, 232, 194, 0.14);
}

.balance-value {
  max-width: 460rpx;
  color: #fff8ea;
  font-size: 52rpx;
  font-weight: 900;
  line-height: 1;
  text-align: right;
  word-break: break-all;
}

.mini-stats {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: stretch;
  padding-top: 26rpx;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.58);
  z-index: 50;
}

.level-modal {
  position: fixed;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 620rpx;
  background: #ffffff;
  border-radius: 28rpx;
  padding: 34rpx;
  z-index: 51;
  box-shadow: 0 20rpx 50rpx rgba(0,0,0,0.18);
}

.modal-title {
  color: #111;
  font-size: 34rpx;
  font-weight: 900;
  margin-bottom: 24rpx;
  text-align: center;
}

.rule-line {
  color: #333;
  font-size: 26rpx;
  line-height: 1.6;
  margin-bottom: 18rpx;
}

.modal-btn {
  height: 78rpx;
  margin-top: 24rpx;
  border-radius: 16rpx;
  background: #111;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
}

.mini-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.mini-value {
  margin-bottom: 8rpx;
  color: #fff8ea;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.1;
}

.field-label {
  display: flex;
  flex-direction: column;
  color: rgba(255, 244, 225, 0.84);
  font-size: 23rpx;
  font-weight: 800;
  line-height: 1.2;

  .en {
    margin-top: 6rpx;
    color: rgba(255, 232, 194, 0.46);
    font-size: 17rpx;
    font-weight: 700;
    letter-spacing: 2rpx;
  }
}

.mini-divider {
  width: 2rpx;
  margin: 8rpx 34rpx 2rpx;
  background: linear-gradient(180deg, rgba(255, 232, 194, 0), rgba(255, 232, 194, 0.2), rgba(255, 232, 194, 0));
}
</style>
