<template>
  <view class="page-wrap" v-if="member">
    <view class="hero">
      <view class="custom-nav">
        <view class="nav-back" @tap="goBack">
          <view class="back-arrow"></view>
        </view>
        <view class="nav-title">会员详情</view>
      </view>

      <view class="member-card">
        <image src="/static/texas_bar_bg.png" mode="aspectFill" class="member-card-bg" />
        <view class="member-card-mask"></view>
        <view class="member-main">
          <view class="avatar-wrap">
            <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
          </view>
          <view class="identity">
            <view class="name">{{ member.name }}</view>
            <view class="phone">{{ getDisplayPhone() }}</view>
            <view class="level-tag">
              <text class="tag-gem">◆</text>
              <text>{{ levelLabel(member.level) }}</text>
            </view>
          </view>
          <view class="level-emblem" :class="`level-${member.level || 'normal'}`">
            <view class="emblem-crown">♛</view>
            <view class="emblem-badge">
              <view class="laurel-branch left">
                <view class="leaf leaf-1"></view>
                <view class="leaf leaf-2"></view>
                <view class="leaf leaf-3"></view>
                <view class="leaf leaf-4"></view>
                <view class="leaf leaf-5"></view>
              </view>
              <view class="laurel">{{ levelInitial }}</view>
              <view class="laurel-branch right">
                <view class="leaf leaf-1"></view>
                <view class="leaf leaf-2"></view>
                <view class="leaf leaf-3"></view>
                <view class="leaf leaf-4"></view>
                <view class="leaf leaf-5"></view>
              </view>
            </view>
            <view class="emblem-en">{{ levelEnglish }}</view>
            <view class="emblem-cn">{{ levelLabel(member.level) }}</view>
          </view>
        </view>
        <view class="remark-box">
          <view class="remark-icon">
            <view class="icon-mark icon-note">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="remark-content">
            <text class="label">客户备注：</text>
            <text class="r-text">{{ member.remark || '暂无备注' }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="content-panel">
      <view class="section-title">
        <text>会员概览</text>
        <text class="section-en">OVERVIEW</text>
      </view>

      <view class="overview-card">
        <view class="overview-row" @tap="openLiquorManage">
          <view class="overview-icon">
            <view class="icon-mark icon-bottle">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="overview-label">当前存酒库 (瓶)</view>
          <view class="overview-value">{{ member.liquor }}</view>
          <view class="chevron"></view>
        </view>

        <view class="divider"></view>

        <view class="overview-row" @tap="openPointsManage">
          <view class="overview-icon">
            <view class="icon-mark icon-star">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="overview-label">剩余积分</view>
          <view class="overview-value">{{ member.points }}</view>
          <view class="chevron"></view>
        </view>

        <view class="divider"></view>

        <view class="overview-row balance-overview" @tap="openBalanceManage">
          <view class="overview-icon">
            <view class="icon-mark icon-wallet">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="overview-label">储值余额</view>
          <view class="balance-copy">
            <view class="overview-value price">¥{{ member.balance }}</view>
            <view class="sub-balance">本金 ¥{{ member.principalBalance }} / 赠送 ¥{{ member.bonusBalance }}</view>
          </view>
          <view class="chevron"></view>
        </view>
      </view>

      <view class="section-title entrance-title">
        <text>业务入口</text>
        <text class="section-en">ENTRANCE</text>
      </view>

      <view class="entrance-grid">
        <view class="entry-card" @tap="openPackageSheet">
          <view class="entry-icon">
            <view class="icon-mark icon-cocktail">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="entry-copy">
            <view class="entry-title">快捷消费套餐</view>
            <view class="entry-desc">128 / 228 / 328 / 498 套餐</view>
          </view>
          <view class="chevron entry-chevron"></view>
        </view>

        <view class="entry-card" @tap="openPointExchangeSheet">
          <view class="entry-icon">
            <view class="icon-mark icon-gift">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="entry-copy">
            <view class="entry-title">积分兑换</view>
            <view class="entry-desc">按套餐快速加分</view>
          </view>
          <view class="chevron entry-chevron"></view>
        </view>

        <view class="entry-card" @tap="openRecords">
          <view class="entry-icon">
            <view class="icon-mark icon-note">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="entry-copy">
            <view class="entry-title">消费记录</view>
            <view class="entry-desc">查看本次会员操作记录</view>
          </view>
          <view class="chevron entry-chevron"></view>
        </view>

        <view class="entry-card manager-card" v-if="canRechargeBalance" @tap="openGiftModal">
          <view class="entry-icon">
            <view class="icon-mark icon-crown">
              <view class="icon-part part-a"></view>
              <view class="icon-part part-b"></view>
              <view class="icon-part part-c"></view>
              <view class="icon-part part-d"></view>
              <view class="icon-part part-e"></view>
              <view class="icon-part part-f"></view>
            </view>
          </view>
          <view class="entry-copy">
            <view class="entry-title">赠码</view>
            <view class="entry-desc">以千为单位登记赠送码量</view>
          </view>
          <view class="chevron entry-chevron"></view>
        </view>
      </view>
    </view>

    <!-- Operation Modal -->
    <view class="modal-mask" v-if="showModal" @tap="closeModal"></view>
    <view class="custom-modal" :class="{'show': showModal}">
      <view class="m-header">{{ modalTitle }}</view>
      <view class="m-body">
        <view class="m-info">当前系统值：<text class="v">{{ originalVal }}</text></view>

        <label class="check-row" v-if="opTarget === 'liquor' && opType === 'add'">
          <checkbox :checked="isGroupBuy" @tap.stop="isGroupBuy = !isGroupBuy" />
          <text>团购散客订单，不扣会员余额</text>
        </label>
        
        <view class="input-wrap">
          <view class="prefix">{{ opType === 'add' ? '+' : '-' }}</view>
          <input 
            class="m-input" 
            type="number" 
            v-model="opAmount" 
            placeholder="输入变更数量" 
            placeholder-style="color:#a0a0a0;"
            :focus="showModal"
          />
        </view>
        
        <view class="m-preview">
          操作后最终结果为：
          <text class="highlight">{{ previewVal }}</text>
        </view>
      </view>
      
      <view class="m-footer">
        <view class="f-btn cancel" @tap="closeModal">取消</view>
        <view class="f-btn confirm" @tap="submitOp">确认执行</view>
      </view>
    </view>

    <view class="modal-mask" v-if="showRechargeModal || showGiftModal" @tap="closeSpecialModals"></view>
    <view class="custom-modal" :class="{'show': showRechargeModal}">
      <view class="m-header">店长储值充值</view>
      <view class="m-body">
        <view class="m-info">仅店长可操作，充值金额为固定档位</view>
        <picker
          mode="selector"
          :range="rechargePlanLabels"
          :value="rechargePlanIndex"
          @change="selectRechargePlan"
        >
          <view class="picker-field">
            <view class="picker-copy">
              <view class="picker-label">充值档位</view>
              <view class="picker-value">{{ selectedRechargePlan.label }}</view>
            </view>
            <view class="chevron"></view>
          </view>
        </picker>
        <view class="split-preview">
          <view>
            <text>本金余额增加</text>
            <text class="amount">¥{{ selectedRechargePlan.amount }}</text>
          </view>
          <view>
            <text>赠送余额增加</text>
            <text class="amount">¥{{ selectedRechargePlan.bonus }}</text>
          </view>
        </view>
        <view class="recharge-note">对账仅统计实际充值本金，赠送金额不计入营收。</view>
      </view>
      <view class="m-footer">
        <view class="f-btn cancel" @tap="closeSpecialModals">取消</view>
        <view class="f-btn confirm" @tap="submitRecharge">确认充值</view>
      </view>
    </view>

    <view class="custom-modal" :class="{'show': showGiftModal}">
      <view class="m-header">赠码</view>
      <view class="m-body">
        <view class="input-wrap">
          <input class="m-input" type="number" v-model="giftAmount" placeholder="赠送码量，如 2 表示 2000" />
        </view>
        <view class="input-wrap remark-input">
          <input class="m-input" v-model="giftRemark" placeholder="备注，可不填" />
        </view>
      </view>
      <view class="m-footer">
        <view class="f-btn cancel" @tap="closeSpecialModals">取消</view>
        <view class="f-btn confirm" @tap="submitGift">提交</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import {
  memberDetail,
  memberOperate,
  memberRecharge,
  memberGroupBuyWine,
  giftChips
} from '@/api/staff'
import { connectStaffSocket } from '@/utils/staffSocket'
import cookie from '@/utils/cookie'

const member = ref(null)
const memberId = ref(null)
const userRole = ref('')

const showModal = ref(false)
const opTarget = ref('') // 'liquor' | 'points' | 'balance'
const opType = ref('')   // 'add' | 'sub'
const opAmount = ref('')
const submitting = ref(false)
const isGroupBuy = ref(false)
const showRechargeModal = ref(false)
const rechargePlanIndex = ref(0)
const showGiftModal = ref(false)
const giftAmount = ref('')
const giftRemark = ref('')
const platinumBadgeReady = ref(true)

const rechargePlans = [
  { amount: 500, bonus: 168, label: '充500送168' },
  { amount: 1000, bonus: 388, label: '充1000送388' },
  { amount: 2000, bonus: 888, label: '充2000送888' },
  { amount: 5000, bonus: 3888, label: '充5000送3888' }
]

const rechargePlanLabels = computed(() => rechargePlans.map(item => item.label))
const selectedRechargePlan = computed(() => rechargePlans[rechargePlanIndex.value] || rechargePlans[0])

/**
 * 加载会员详情
 */
const loadMember = async (id) => {
  try {
    const data = await memberDetail(id)
    if (data) {
      member.value = {
        ...data,
        // API 返回 wine，前端保持 liquor 命名以兼容模板
        liquor: data.wine != null ? data.wine : (data.liquor || 0),
        balance: data.balance != null ? Number(data.balance).toFixed(2) : '0.00',
        principalBalance: data.principalBalance != null ? Number(data.principalBalance).toFixed(2) : '0.00',
        bonusBalance: data.bonusBalance != null ? Number(data.bonusBalance).toFixed(2) : '0.00',
        points: data.points || 0,
        level: data.level || 'normal'
      }
    }
  } catch (err) {
    console.error('[memberDetail] error:', err)
    uni.showToast({ title: '加载会员信息失败', icon: 'none' })
  }
}

onLoad((options) => {
  uni.$on('MEMBER_DATA_UPDATE', onMemberDataUpdate)
  if (options.id) {
    memberId.value = options.id
    loadMember(options.id)
  } else if (options.member) {
    // 兼容旧的 JSON 传参方式
    try {
      const parsed = JSON.parse(decodeURIComponent(options.member))
      member.value = parsed
      memberId.value = parsed.id
    } catch(e) {
      console.error(e)
    }
  }
})

onShow(() => {
  userRole.value = cookie.get('userRole') || ''
  connectStaffSocket()
})

onUnload(() => {
  uni.$off('MEMBER_DATA_UPDATE', onMemberDataUpdate)
})

const onMemberDataUpdate = (message) => {
  if (!memberId.value || Number(message.memberId) !== Number(memberId.value)) {
    return
  }
  loadMember(memberId.value)
}

const goBack = () => {
  uni.navigateBack({
    delta: 1,
    fail: () => uni.redirectTo({ url: '/pages/staff/search' })
  })
}

const getDisplayPhone = () => {
  const phone = String(member.value?.phone || '')
  if (!phone) return '未知'
  return phone.length >= 4 ? `*****${phone.slice(-4)}` : phone
}

const levelEnglish = computed(() => ({
  normal: 'MEMBER',
  gold: 'GOLD',
  platinum: 'PLATINUM',
  black_gold: 'BLACK GOLD',
  black_diamond: 'DIAMOND'
}[member.value?.level] || 'MEMBER'))

const levelInitial = computed(() => ({
  normal: 'M',
  gold: 'G',
  platinum: 'P',
  black_gold: 'B',
  black_diamond: 'D'
}[member.value?.level] || 'M'))

const modalTitle = computed(() => {
  let t = '存酒';
  if (opTarget.value === 'points') t = '积分';
  if (opTarget.value === 'balance') t = '储值余额';
  const action = opType.value === 'add' ? '增加' : '扣减';
  return `${action}${t}操作`;
})

const originalVal = computed(() => {
  if(!member.value) return 0;
  return Number(member.value[opTarget.value]) || 0
})

const previewVal = computed(() => {
  const current = originalVal.value;
  const mod = Number(opAmount.value) || 0;
  
  let res = 0;
  if (opType.value === 'add') {
    res = current + mod;
  } else {
    res = current - mod;
    if (res < 0) res = 0;
  }
  
  if (opTarget.value === 'balance') {
    return res.toFixed(2);
  }
  return res;
})

const canRechargeBalance = computed(() => userRole.value === 'manager')
const canExchangePoints = computed(() => ['gold', 'platinum', 'black_gold', 'black_diamond'].includes(member.value?.level))

const levelLabel = (level) => ({
  normal: '普通会员',
  gold: '黄金会员',
  platinum: '白金会员',
  black_gold: '黑金会员',
  black_diamond: '黑钻会员'
}[level] || '普通会员')

const openModal = (target, type) => {
  if (target === 'balance' && type === 'add' && !canRechargeBalance.value) {
    uni.showToast({ title: '暂无储值充值权限', icon: 'none' })
    return
  }

  opTarget.value = target
  opType.value = type
  opAmount.value = ''
  isGroupBuy.value = false
  showModal.value = true
}

const openLiquorManage = () => {
  if (!memberId.value) return
  uni.navigateTo({ url: `/pages/staff/liquor?id=${memberId.value}` })
}

const openPointsManage = () => {
  if (!memberId.value) return
  uni.navigateTo({ url: `/pages/staff/points-manage?id=${memberId.value}` })
}

const openBalanceManage = () => {
  const itemList = canRechargeBalance.value ? ['店长储值充值', '消费扣减'] : ['消费扣减']
  uni.showActionSheet({
    itemList,
    success: ({ tapIndex }) => {
      if (canRechargeBalance.value && tapIndex === 0) {
        openRechargeModal()
        return
      }
      openModal('balance', 'sub')
    }
  })
}

const openPackageSheet = () => {
  if (!memberId.value) return
  uni.navigateTo({ url: `/pages/staff/package-consume?id=${memberId.value}` })
}

const openPointExchangeSheet = () => {
  if (!memberId.value) return
  if (!canExchangePoints.value) {
    uni.showToast({ title: '当前会员等级暂不支持积分兑换', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/staff/points-exchange?id=${memberId.value}` })
}

const openRecords = () => {
  if (!memberId.value) return
  uni.navigateTo({ url: `/pages/staff/records?id=${memberId.value}` })
}

const closeModal = () => {
  showModal.value = false
}

/**
 * 将前端的 target + type 映射为 API 的 enum 类型
 * target: 'liquor' | 'points' | 'balance'
 * type: 'add' | 'sub'
 * => 'ADD_WINE' | 'SUB_WINE' | 'ADD_POINTS' | 'SUB_POINTS' | 'ADD_BALANCE' | 'SUB_BALANCE'
 */
const getOperateType = (target, type) => {
  const targetMap = {
    liquor: 'WINE',
    points: 'POINTS',
    balance: 'BALANCE'
  }
  const prefix = type === 'add' ? 'ADD' : 'SUB'
  return `${prefix}_${targetMap[target]}`
}

const submitOp = async () => {
  if (opTarget.value === 'balance' && opType.value === 'add') {
    uni.showToast({ title: '储值充值请选择固定档位', icon: 'none' })
    openRechargeModal()
    return
  }

  const mod = Number(opAmount.value)
  if(!mod || mod <= 0) {
    uni.showToast({ title: '请输入有效的数量', icon: 'none' })
    return
  }

  if (opType.value === 'sub' && mod > originalVal.value) {
    uni.showToast({ title: '扣减数量不能大于当前总量', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  uni.showLoading({ title: '处理中...' })

  try {
    if (opTarget.value === 'liquor' && opType.value === 'add' && isGroupBuy.value) {
      if (![2, 4, 8].includes(mod)) {
        uni.hideLoading()
        uni.showToast({ title: '团购订单仅支持2瓶、4瓶、8瓶', icon: 'none' })
        return
      }
      await memberGroupBuyWine({
        memberId: Number(memberId.value),
        wineQuantity: mod
      })
      uni.hideLoading()
      uni.showToast({ title: `团购营收¥${mod * 25}`, icon: 'none' })
      closeModal()
      await loadMember(memberId.value)
      return
    }

    await memberOperate({
      memberId: Number(memberId.value),
      type: getOperateType(opTarget.value, opType.value),
      value: mod
    })

    uni.hideLoading()
    uni.showToast({ title: '操作成功', icon: 'success' })
    closeModal()

    // 刷新会员数据以获取最新状态
    await loadMember(memberId.value)
  } catch (err) {
    uni.hideLoading()
    console.error('[memberOperate] error:', err)
  } finally {
    submitting.value = false
  }
}

const openRechargeModal = () => {
  if (!canRechargeBalance.value) {
    uni.showToast({ title: '只有店长可以进行储值充值', icon: 'none' })
    return
  }
  rechargePlanIndex.value = 0
  showRechargeModal.value = true
}

const selectRechargePlan = (event) => {
  rechargePlanIndex.value = Number(event.detail.value || 0)
}

const openGiftModal = () => {
  giftAmount.value = ''
  giftRemark.value = ''
  showGiftModal.value = true
}

const closeSpecialModals = () => {
  showRechargeModal.value = false
  showGiftModal.value = false
}

const submitRecharge = async () => {
  if (submitting.value) return
  submitting.value = true
  try {
    await memberRecharge({ memberId: Number(memberId.value), amount: selectedRechargePlan.value.amount })
    uni.showToast({ title: '充值成功', icon: 'success' })
    closeSpecialModals()
    await loadMember(memberId.value)
  } finally {
    submitting.value = false
  }
}

const submitGift = async () => {
  const amount = Number(giftAmount.value)
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入赠送码量', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await giftChips({ chipAmount: amount, remark: giftRemark.value })
    uni.showToast({ title: '赠码已记录', icon: 'success' })
    closeSpecialModals()
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page-wrap {
  min-height: 100vh;
  box-sizing: border-box;
  padding-bottom: calc(36rpx + env(safe-area-inset-bottom));
  background: #f8f3ec;
  position: relative;
}

.hero {
  padding: var(--status-bar-height) 22rpx 34rpx;
  background: #030303;
  position: relative;
  overflow: visible;
}

.hero::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1rpx;
  height: 96rpx;
  background: linear-gradient(180deg, rgba(248, 243, 236, 0) 0%, #f8f3ec 76%);
  z-index: 0;
  pointer-events: none;
}

.custom-nav {
  height: 112rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.nav-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: 500;
  line-height: 1;
}

.nav-back {
  position: absolute;
  left: 0;
  top: 16rpx;
  width: 88rpx;
  height: 80rpx;
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
  z-index: 1;
  padding: 38rpx 30rpx 32rpx;
  border-radius: 22rpx;
  border: 2rpx solid rgba(226, 187, 143, 0.66);
  background: #080706;
  box-shadow: 0 18rpx 42rpx rgba(29, 15, 4, 0.35);
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
  background: linear-gradient(105deg, rgba(8, 7, 6, 0.95) 0%, rgba(38, 28, 22, 0.92) 54%, rgba(14, 11, 9, 0.96) 100%);
}

.member-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 69% 30%, rgba(226, 180, 120, 0.22) 0, transparent 28%),
    radial-gradient(circle at 92% 72%, rgba(193, 139, 78, 0.18) 0, transparent 22%);
  pointer-events: none;
  z-index: 1;
}

.member-main,
.remark-box {
  position: relative;
  z-index: 2;
}

.member-main {
  position: relative;
  display: flex;
  align-items: center;
  gap: 26rpx;
}

.avatar-wrap {
  position: relative;
  flex: 0 0 120rpx;
}

.avatar-wrap::before {
  content: '';
  position: absolute;
  inset: -10rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 238, 199, 0.32) 0%, rgba(203, 159, 99, 0) 68%);
}

.avatar {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #030303;
  border: 5rpx solid #f2dfbd;
  box-sizing: border-box;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  font-weight: 900;
  box-shadow: inset 0 0 0 2rpx rgba(177, 128, 64, 0.68), 0 0 26rpx rgba(237, 196, 130, 0.28);
}

.identity {
  flex: 1;
  min-width: 0;
}

.name {
  color: #ffffff;
  font-size: 40rpx;
  font-weight: 900;
  line-height: 1.2;
  text-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.38);
}

.phone {
  margin-top: 8rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 28rpx;
  line-height: 1.2;
}

.level-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  height: 46rpx;
  margin-top: 16rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, rgba(33, 28, 23, 0.95) 0%, rgba(2, 2, 2, 0.95) 100%);
  border: 1rpx solid rgba(211, 176, 124, 0.38);
  color: #ffe9bd;
  font-size: 26rpx;
  font-weight: 800;
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.18), 0 6rpx 18rpx rgba(0, 0, 0, 0.24);
}

.tag-gem {
  color: #d3ad70;
  font-size: 20rpx;
}

.level-emblem {
  width: 172rpx;
  flex: 0 0 172rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #d8c1a2;
}

.emblem-crown {
  color: #f2e8d6;
  font-size: 32rpx;
  line-height: 1;
  margin-bottom: -4rpx;
  text-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.4);
}

.emblem-badge {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 150rpx;
  height: 92rpx;
}

.laurel {
  position: relative;
  z-index: 2;
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
  border: 5rpx solid rgba(236, 224, 207, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f6efe3;
  font-size: 38rpx;
  font-weight: 900;
  background:
    radial-gradient(circle at 34% 24%, rgba(255, 255, 255, 0.92) 0%, rgba(207, 199, 188, 0.72) 23%, rgba(98, 91, 83, 0.95) 58%, rgba(39, 34, 30, 0.98) 100%);
  box-shadow:
    inset 0 0 0 3rpx rgba(58, 50, 44, 0.58),
    inset 0 0 22rpx rgba(255, 255, 255, 0.28),
    0 9rpx 18rpx rgba(0, 0, 0, 0.38);
  text-shadow: 0 3rpx 5rpx rgba(0, 0, 0, 0.54);
}

.laurel-branch {
  position: absolute;
  top: 10rpx;
  width: 38rpx;
  height: 78rpx;
  z-index: 1;
}

.laurel-branch.left {
  left: 8rpx;
}

.laurel-branch.right {
  right: 8rpx;
  transform: scaleX(-1);
}

.leaf {
  position: absolute;
  width: 17rpx;
  height: 8rpx;
  border-radius: 16rpx 16rpx 16rpx 2rpx;
  background: linear-gradient(90deg, #f3ead9 0%, #bda27e 100%);
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.24);
  transform-origin: right center;
}

.leaf-1 {
  left: 17rpx;
  top: 4rpx;
  transform: rotate(-52deg);
}

.leaf-2 {
  left: 9rpx;
  top: 18rpx;
  transform: rotate(-38deg);
}

.leaf-3 {
  left: 5rpx;
  top: 34rpx;
  transform: rotate(-23deg);
}

.leaf-4 {
  left: 6rpx;
  top: 50rpx;
  transform: rotate(-7deg);
}

.leaf-5 {
  left: 13rpx;
  top: 64rpx;
  transform: rotate(12deg);
}

.emblem-en {
  margin-top: 8rpx;
  color: #d6bd9a;
  font-size: 23rpx;
  font-weight: 900;
  line-height: 1;
  letter-spacing: 2rpx;
  text-shadow: 0 3rpx 5rpx rgba(0, 0, 0, 0.42);
}

.emblem-cn {
  margin-top: 8rpx;
  color: #ead6b8;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
  text-shadow: 0 3rpx 5rpx rgba(0, 0, 0, 0.42);
}

.level-platinum .emblem-crown,
.level-platinum .emblem-cn {
  color: #f7f1e4;
}

.level-platinum .emblem-en {
  color: #d7c3a1;
}

.level-platinum .laurel {
  border-color: rgba(237, 226, 211, 0.96);
  background:
    radial-gradient(circle at 34% 24%, rgba(255, 255, 255, 0.96) 0%, rgba(214, 210, 202, 0.8) 24%, rgba(112, 106, 98, 0.98) 60%, rgba(37, 33, 30, 0.99) 100%);
}

.level-platinum .leaf {
  background: linear-gradient(90deg, #fff6e7 0%, #c9ad84 100%);
}

.remark-box {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  gap: 22rpx;
  margin-top: 28rpx;
  padding: 24rpx 28rpx;
  min-height: 88rpx;
  border-radius: 14rpx;
  background: rgba(2, 2, 2, 0.36);
  border: 1rpx solid rgba(226, 204, 176, 0.24);
}

.remark-icon {
  width: 40rpx;
  height: 40rpx;
  flex: 0 0 40rpx;
  color: #dbb98d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remark-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.label {
  color: #e8c898;
  font-size: 26rpx;
  font-weight: 700;
}

.r-text {
  color: #ffffff;
  font-size: 27rpx;
  line-height: 1.35;
  word-break: break-all;
}

.content-panel {
  position: relative;
  z-index: 3;
  padding: 34rpx 24rpx 0;
}

.section-title {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10rpx 0 18rpx;
  padding-left: 26rpx;
  color: #050505;
  font-size: 34rpx;
  font-weight: 900;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  width: 7rpx;
  height: 34rpx;
  border-radius: 99rpx;
  background: linear-gradient(180deg, #dfb674 0%, #ad7224 100%);
}

.section-en {
  color: #d7c8bc;
  font-size: 26rpx;
  font-weight: 500;
  letter-spacing: 10rpx;
}

.entrance-title {
  margin-top: 42rpx;
}

.overview-card {
  overflow: hidden;
  border-radius: 18rpx;
  background: #ffffff;
  border: 1rpx solid rgba(221, 196, 169, 0.72);
  box-shadow: 0 14rpx 40rpx rgba(69, 43, 20, 0.08);
}

.overview-row {
  display: grid;
  grid-template-columns: 78rpx minmax(0, 1fr) auto 30rpx;
  align-items: center;
  column-gap: 20rpx;
  min-height: 110rpx;
  padding: 16rpx 24rpx;
  box-sizing: border-box;
}

.overview-icon {
  width: 66rpx;
  height: 66rpx;
  border-radius: 50%;
  background: #f6eee5;
  color: #bd8541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.overview-label {
  color: #2f2925;
  font-size: 30rpx;
  line-height: 1.25;
}

.overview-value {
  color: #030303;
  font-size: 40rpx;
  font-weight: 900;
  line-height: 1.1;
  text-align: right;
}

.price {
  font-size: 36rpx;
}

.balance-copy {
  min-width: 0;
  text-align: right;
}

.sub-balance {
  margin-top: 8rpx;
  color: #a09a96;
  font-size: 24rpx;
  line-height: 1.25;
}

.divider {
  height: 1rpx;
  margin-left: 108rpx;
  background: #efe7df;
}

.chevron {
  width: 18rpx;
  height: 18rpx;
  border-top: 4rpx solid #c3b2a2;
  border-right: 4rpx solid #c3b2a2;
  transform: rotate(45deg);
  box-sizing: border-box;
}

.entrance-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.entry-card {
  position: relative;
  display: grid;
  grid-template-columns: 68rpx minmax(0, 1fr) 16rpx;
  align-items: center;
  column-gap: 12rpx;
  min-height: 116rpx;
  padding: 18rpx 16rpx;
  box-sizing: border-box;
  border-radius: 18rpx;
  background: #ffffff;
  border: 1rpx solid rgba(221, 196, 169, 0.72);
  box-shadow: 0 12rpx 32rpx rgba(69, 43, 20, 0.08);
  overflow: hidden;
}

.entry-card:active,
.overview-row:active {
  opacity: 0.86;
  transform: scale(0.99);
}

.balance-entry {
  background:
    radial-gradient(circle at 88% 70%, rgba(231, 205, 169, 0.34) 0, rgba(231, 205, 169, 0) 32%),
    #fffaf4;
  border-color: rgba(195, 151, 99, 0.7);
}

.manager-card {
  background:
    radial-gradient(circle at 88% 70%, rgba(231, 205, 169, 0.32) 0, rgba(231, 205, 169, 0) 34%),
    #fffaf4;
  border-color: rgba(195, 151, 99, 0.7);
}

.entry-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #262019 0%, #050505 100%);
  border: 2rpx solid rgba(215, 172, 112, 0.72);
  color: #dbb07c;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 24rpx rgba(27, 14, 4, 0.18);
}

.entry-copy {
  min-width: 0;
}

.entry-title {
  color: #080808;
  font-size: 28rpx;
  font-weight: 500;
  line-height: 1.2;
}

.entry-desc {
  margin-top: 7rpx;
  color: #9b9998;
  font-size: 23rpx;
  line-height: 1.28;
  word-break: break-all;
}

.entry-chevron {
  justify-self: end;
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
  background: #f7f0e7;
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

.icon-note .part-e {
  display: block;
  right: 0;
  bottom: 4rpx;
  width: 15rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
  border-radius: 3rpx;
  transform: rotate(-45deg);
}

.icon-note .part-f {
  display: block;
  right: 0;
  bottom: 2rpx;
  width: 5rpx;
  height: 5rpx;
  border-left: 3rpx solid currentColor;
  border-bottom: 3rpx solid currentColor;
  transform: rotate(-45deg);
}

.icon-bottle .part-a {
  display: block;
  left: 12rpx;
  top: 11rpx;
  width: 13rpx;
  height: 22rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx 5rpx 8rpx 8rpx;
}

.icon-bottle .part-b {
  display: block;
  left: 14rpx;
  top: 3rpx;
  width: 9rpx;
  height: 11rpx;
  border: 3rpx solid currentColor;
  border-bottom: 0;
  border-radius: 4rpx 4rpx 0 0;
}

.icon-bottle .part-c {
  display: block;
  left: 14rpx;
  top: 22rpx;
  width: 9rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-star .part-a,
.icon-star .part-b,
.icon-star .part-c,
.icon-star .part-d,
.icon-star .part-e {
  display: block;
  height: 0;
  width: 15rpx;
  border-top: 3rpx solid currentColor;
  border-radius: 3rpx;
  transform-origin: center center;
}

.icon-star .part-a {
  left: 11rpx;
  top: 4rpx;
  transform: rotate(72deg);
}

.icon-star .part-b {
  left: 18rpx;
  top: 12rpx;
  transform: rotate(144deg);
}

.icon-star .part-c {
  left: 14rpx;
  top: 25rpx;
}

.icon-star .part-d {
  left: 5rpx;
  top: 12rpx;
  transform: rotate(36deg);
}

.icon-star .part-e {
  left: 7rpx;
  top: 25rpx;
  transform: rotate(-72deg);
}

.icon-wallet .part-a {
  display: block;
  left: 3rpx;
  top: 10rpx;
  width: 29rpx;
  height: 20rpx;
  border: 3rpx solid currentColor;
  border-radius: 6rpx;
}

.icon-wallet .part-b {
  display: block;
  left: 7rpx;
  top: 8rpx;
  width: 18rpx;
  height: 10rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx;
  transform: rotate(-12deg);
}

.icon-wallet .part-c {
  display: block;
  right: 2rpx;
  top: 16rpx;
  width: 13rpx;
  height: 9rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx;
  background: #f7f1e8;
}

.icon-cocktail .part-a {
  display: block;
  left: 5rpx;
  top: 6rpx;
  width: 26rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-cocktail .part-b,
.icon-cocktail .part-c {
  display: block;
  top: 5rpx;
  width: 0;
  height: 17rpx;
  border-left: 3rpx solid currentColor;
  border-radius: 3rpx;
}

.icon-cocktail .part-b {
  left: 8rpx;
  transform: rotate(-25deg);
}

.icon-cocktail .part-c {
  right: 8rpx;
  transform: rotate(25deg);
}

.icon-cocktail .part-d {
  display: block;
  left: 17rpx;
  top: 21rpx;
  width: 0;
  height: 10rpx;
  border-left: 3rpx solid currentColor;
}

.icon-cocktail .part-e {
  display: block;
  left: 10rpx;
  top: 31rpx;
  width: 16rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-cocktail .part-f {
  display: block;
  left: 18rpx;
  top: 2rpx;
  width: 14rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
  transform: rotate(-35deg);
}

.icon-cheers .part-a,
.icon-cheers .part-d {
  display: block;
  top: 5rpx;
  width: 11rpx;
  height: 15rpx;
  border: 3rpx solid currentColor;
  border-top-width: 4rpx;
  border-radius: 2rpx 2rpx 7rpx 7rpx;
}

.icon-cheers .part-a {
  left: 5rpx;
  transform: rotate(-18deg);
}

.icon-cheers .part-d {
  right: 5rpx;
  transform: rotate(18deg);
}

.icon-cheers .part-b,
.icon-cheers .part-e {
  display: block;
  top: 20rpx;
  width: 0;
  height: 9rpx;
  border-left: 3rpx solid currentColor;
}

.icon-cheers .part-b {
  left: 13rpx;
  transform: rotate(-18deg);
}

.icon-cheers .part-e {
  right: 13rpx;
  transform: rotate(18deg);
}

.icon-cheers .part-c,
.icon-cheers .part-f {
  display: block;
  top: 29rpx;
  width: 12rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-cheers .part-c {
  left: 7rpx;
}

.icon-cheers .part-f {
  right: 7rpx;
}

.icon-wine-set .part-a {
  display: block;
  left: 3rpx;
  top: 11rpx;
  width: 12rpx;
  height: 22rpx;
  border: 3rpx solid currentColor;
  border-radius: 5rpx 5rpx 8rpx 8rpx;
}

.icon-wine-set .part-b {
  display: block;
  left: 5rpx;
  top: 3rpx;
  width: 8rpx;
  height: 11rpx;
  border: 3rpx solid currentColor;
  border-bottom: 0;
  border-radius: 4rpx 4rpx 0 0;
}

.icon-wine-set .part-c {
  display: block;
  left: 5rpx;
  top: 22rpx;
  width: 8rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-wine-set .part-d {
  display: block;
  right: 4rpx;
  top: 8rpx;
  width: 12rpx;
  height: 14rpx;
  border: 3rpx solid currentColor;
  border-radius: 2rpx 2rpx 7rpx 7rpx;
}

.icon-wine-set .part-e {
  display: block;
  right: 9rpx;
  top: 22rpx;
  width: 0;
  height: 8rpx;
  border-left: 3rpx solid currentColor;
}

.icon-wine-set .part-f {
  display: block;
  right: 5rpx;
  top: 30rpx;
  width: 11rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-crown .icon-part {
  display: block;
  height: 0;
  border-top: 3rpx solid currentColor;
  border-radius: 3rpx;
  transform-origin: center center;
}

.icon-crown .part-a {
  left: 5rpx;
  top: 27rpx;
  width: 26rpx;
}

.icon-crown .part-b {
  left: 6rpx;
  top: 18rpx;
  width: 15rpx;
  transform: rotate(48deg);
}

.icon-crown .part-c {
  left: 13rpx;
  top: 15rpx;
  width: 14rpx;
  transform: rotate(74deg);
}

.icon-crown .part-d {
  right: 6rpx;
  top: 18rpx;
  width: 15rpx;
  transform: rotate(-48deg);
}

.icon-crown .part-e,
.icon-crown .part-f {
  width: 5rpx;
  height: 5rpx;
  border: 3rpx solid currentColor;
  border-radius: 50%;
  background: currentColor;
}

.icon-crown .part-e {
  left: 4rpx;
  top: 12rpx;
}

.icon-crown .part-f {
  right: 4rpx;
  top: 12rpx;
}

.icon-gift .part-a {
  display: block;
  left: 6rpx;
  top: 15rpx;
  width: 24rpx;
  height: 17rpx;
  border: 3rpx solid currentColor;
  border-radius: 3rpx;
}

.icon-gift .part-b {
  display: block;
  left: 4rpx;
  top: 10rpx;
  width: 28rpx;
  height: 8rpx;
  border: 3rpx solid currentColor;
  border-radius: 3rpx;
}

.icon-gift .part-c {
  display: block;
  left: 16rpx;
  top: 10rpx;
  width: 4rpx;
  height: 22rpx;
  background: currentColor;
}

.icon-gift .part-d {
  display: block;
  left: 6rpx;
  top: 20rpx;
  width: 24rpx;
  height: 0;
  border-top: 3rpx solid currentColor;
}

.icon-gift .part-e,
.icon-gift .part-f {
  display: block;
  top: 3rpx;
  width: 11rpx;
  height: 8rpx;
  border: 3rpx solid currentColor;
  border-radius: 50% 50% 3rpx 50%;
}

.icon-gift .part-e {
  left: 7rpx;
  transform: rotate(28deg);
}

.icon-gift .part-f {
  right: 7rpx;
  transform: rotate(62deg);
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.58);
  z-index: 100;
}

.custom-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -40%);
  width: 620rpx;
  max-width: calc(100vw - 64rpx);
  background: #ffffff;
  border-radius: 26rpx;
  z-index: 101;
  opacity: 0;
  pointer-events: none;
  transition: all 0.24s ease;
  box-shadow: 0 18rpx 60rpx rgba(0, 0, 0, 0.18);

  &.show {
    opacity: 1;
    pointer-events: auto;
    transform: translate(-50%, -50%);
  }

  .m-header {
    text-align: center;
    color: #1d1711;
    font-size: 32rpx;
    font-weight: 900;
    padding: 36rpx 0 18rpx;
  }

  .m-body {
    padding: 18rpx 36rpx;

    .m-info {
      color: #897a6c;
      font-size: 25rpx;
      text-align: center;
      margin-bottom: 28rpx;

      .v {
        color: #1d1711;
        font-weight: 900;
      }
    }

    .input-wrap {
      display: flex;
      align-items: center;
      height: 88rpx;
      padding: 0 28rpx;
      border-radius: 14rpx;
      background: #faf7f2;
      border: 1rpx solid #eadccb;

      .prefix {
        color: #1d1711;
        font-size: 36rpx;
        font-weight: 900;
        margin-right: 16rpx;
      }

      .m-input {
        flex: 1;
        height: 100%;
        color: #1d1711;
        font-size: 30rpx;
      }
    }

    .remark-input {
      margin-top: 18rpx;
    }

    .check-row {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 24rpx;
      color: #3c3128;
      font-size: 24rpx;
    }

    .plan-list {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16rpx;
    }

    .plan-item {
      height: 78rpx;
      border-radius: 14rpx;
      border: 1rpx solid #e2d1bb;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #2a2119;
      font-size: 25rpx;
      font-weight: 900;
      background: #faf7f2;
    }

    .plan-item.active {
      background: #111111;
      color: #fff7e8;
      border-color: #111111;
    }

    .picker-field {
      display: flex;
      align-items: center;
      justify-content: space-between;
      min-height: 92rpx;
      padding: 0 26rpx;
      border-radius: 14rpx;
      background: #faf7f2;
      border: 1rpx solid #eadccb;
      box-sizing: border-box;
    }

    .picker-copy {
      display: flex;
      flex-direction: column;
      gap: 8rpx;
    }

    .picker-label {
      color: #9b8a78;
      font-size: 23rpx;
      line-height: 1;
    }

    .picker-value {
      color: #1d1711;
      font-size: 31rpx;
      font-weight: 900;
      line-height: 1;
    }

    .split-preview {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 14rpx;
      margin-top: 18rpx;

      view {
        min-height: 86rpx;
        padding: 16rpx;
        border-radius: 14rpx;
        background: #fffaf4;
        border: 1rpx solid #eadccb;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 8rpx;
      }

      text {
        color: #8a7a6b;
        font-size: 23rpx;
        line-height: 1;
      }

      .amount {
        color: #1d1711;
        font-size: 30rpx;
        font-weight: 900;
      }
    }

    .recharge-note {
      margin-top: 18rpx;
      color: #8a7a6b;
      font-size: 23rpx;
      line-height: 1.45;
    }

    .m-preview {
      margin-top: 30rpx;
      text-align: center;
      color: #897a6c;
      font-size: 25rpx;

      .highlight {
        color: #1d1711;
        font-size: 36rpx;
        font-weight: 900;
        margin-left: 10rpx;
      }
    }
  }

  .m-footer {
    display: flex;
    border-top: 1rpx solid #eee5da;
    margin-top: 30rpx;

    .f-btn {
      flex: 1;
      height: 104rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30rpx;

      &.cancel {
        color: #8a7a6b;
        border-right: 1rpx solid #eee5da;
      }

      &.confirm {
        color: #111111;
        font-weight: 900;
      }

      &:active {
        background: #faf7f2;
      }
    }
  }
}
</style>
