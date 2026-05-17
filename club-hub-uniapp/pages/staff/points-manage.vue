<template>
  <view class="page-wrap" v-if="member">
    <view class="hero">
      <view class="custom-nav">
        <view class="nav-back" @tap="goBack"><view class="back-arrow"></view></view>
        <view class="nav-title">积分管理</view>
      </view>

      <view class="member-card">
        <image src="/static/texas_bar_bg.png" mode="aspectFill" class="member-card-bg" />
        <view class="member-card-mask"></view>
        <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
        <view class="member-info">
          <view class="member-name">{{ member.name }}</view>
          <view class="member-phone">{{ getDisplayPhone() }}</view>
          <view class="level-tag"><text class="tag-gem">◆</text><text>{{ levelLabel(member.level) }}</text></view>
        </view>
        <view class="card-divider"></view>
        <view class="points-total">
          <view class="total-label">当前积分</view>
          <view class="total-value">{{ member.points }}</view>
        </view>
      </view>
    </view>

    <view class="content">
      <view class="panel">
        <view class="section-title">请选择积分操作</view>
        <view class="action-grid">
          <view class="action-card" :class="{ active: opType === 'add' }" @tap="selectType('add')">
            <view class="action-symbol">□</view>
            <view>筹码兑换积分</view>
          </view>
          <view class="action-card" :class="{ active: opType === 'sub' }" @tap="selectType('sub')">
            <view class="action-symbol">↻</view>
            <view>积分兑换筹码</view>
          </view>
        </view>
      </view>

      <view class="panel">
        <view class="section-title">快捷积分</view>
        <view class="amount-grid">
          <view
            v-for="amount in quickAmounts"
            :key="amount"
            class="amount-btn"
            :class="{ active: selectedAmount === amount && !customMode }"
            @tap="selectAmount(amount)"
          >{{ opType === 'add' ? '+' : '-' }}{{ amount }}</view>
          <view class="amount-btn custom-btn" :class="{ active: customMode }" @tap="selectCustom">自定义</view>
        </view>
        <view class="custom-input" v-if="customMode">
          <input
            type="number"
            v-model="customAmount"
            placeholder="请输入积分"
            placeholder-style="color:#a5a09a;"
          />
          <text>分</text>
        </view>
      </view>

      <view class="operate-card">
        <view class="operate-row">
          <view class="row-icon">◎</view>
          <view class="row-label">本次操作积分</view>
          <view class="row-value">{{ opType === 'add' ? '+' : '-' }}{{ operateAmount }}</view>
        </view>
        <view class="operate-divider"></view>
        <view class="operate-row" @tap="focusRemark">
          <view class="row-icon">✎</view>
          <view class="row-label">操作备注</view>
          <input
            class="remark-input"
            v-model="remark"
            maxlength="50"
            :focus="remarkFocus"
            placeholder="请输入备注（选填）"
            placeholder-style="color:#a5a09a;"
          />
          <view class="chevron"></view>
        </view>
      </view>

      <view class="confirm-btn" @tap="submitOp">{{ opType === 'add' ? '确认筹码兑积分' : '确认积分兑筹码' }}</view>

      <view class="record-title">
        <view class="section-title">最近积分记录</view>
        <view class="more-link" @tap="showMore">查看更多 <text>›</text></view>
      </view>
      <view class="record-card">
        <view v-if="pointRecords.length === 0" class="empty-record">暂无积分记录</view>
        <view v-for="item in pointRecords" :key="item.id || item.time || item.createTime" class="record-row">
          <view class="record-icon" :class="{ sub: isSubRecord(item) }">{{ recordIcon(item) }}</view>
          <view class="record-type">{{ pointRecordTitle(item) }}</view>
          <view class="record-value" :class="{ sub: isSubRecord(item) }">{{ formatPointValue(item) }}</view>
          <view class="record-time">{{ item.time || item.createTime || item.createdAt || '' }}</view>
          <view class="chevron small"></view>
        </view>
      </view>

      <view class="tip-card">
        <view class="tip-icon">♢</view>
        <view>确认后将实时更新会员积分。</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { memberDetail, memberOperate } from '@/api/staff'

const member = ref(null)
const memberId = ref('')
const opType = ref('add')
const selectedAmount = ref(1000)
const customMode = ref(false)
const customAmount = ref('')
const remark = ref('')
const remarkFocus = ref(false)
const submitting = ref(false)

const quickAmounts = [100, 500, 1000, 2000]

const operateAmount = computed(() => customMode.value ? Number(customAmount.value || 0) : Number(selectedAmount.value || 0))

const pointRecords = computed(() => {
  const list = [
    ...(member.value?.pointRecords || []),
    ...(member.value?.pointsRecords || []),
    ...(member.value?.operationRecords || []),
    ...(member.value?.businessRecords || [])
  ]
  return list.filter(item => {
    const type = String(item.type || item.operateType || item.operationType || item.recordType || item.action || item.title || '').toUpperCase()
    return type.includes('POINT') || type.includes('积分') || item.points != null || item.pointsAmount != null
  }).sort((a, b) => parseRecordTime(b) - parseRecordTime(a)).slice(0, 4)
})

onLoad((options) => {
  memberId.value = options.id || ''
  if (memberId.value) loadMember(memberId.value)
})

const loadMember = async (id) => {
  try {
    const data = await memberDetail(id)
    if (data) {
      member.value = { ...data, points: data.points || 0, level: data.level || 'normal' }
    }
  } catch (err) {
    console.error('[memberDetail] error:', err)
    uni.showToast({ title: '加载会员信息失败', icon: 'none' })
  }
}

const goBack = () => {
  uni.navigateBack({ delta: 1, fail: () => uni.redirectTo({ url: `/pages/staff/detail?id=${memberId.value}` }) })
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
}

const selectAmount = (amount) => {
  selectedAmount.value = amount
  customMode.value = false
  customAmount.value = ''
}

const selectCustom = () => {
  customMode.value = true
  customAmount.value = ''
}

const focusRemark = () => {
  remarkFocus.value = true
  setTimeout(() => { remarkFocus.value = false }, 200)
}

const submitOp = async () => {
  const amount = operateAmount.value
  if (!amount || amount <= 0) {
    uni.showToast({ title: '请输入有效积分', icon: 'none' })
    return
  }
  if (opType.value === 'sub' && amount > Number(member.value?.points || 0)) {
    uni.showToast({ title: '扣减积分不能大于当前积分', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    await memberOperate({
      memberId: Number(memberId.value),
      type: opType.value === 'add' ? 'ADD_POINTS' : 'SUB_POINTS',
      value: amount
    })
    uni.hideLoading()
    uni.showToast({ title: opType.value === 'add' ? '积分已增加' : '积分已扣减', icon: 'success' })
    customAmount.value = ''
    customMode.value = false
    remark.value = ''
    await loadMember(memberId.value)
  } catch (err) {
    uni.hideLoading()
    console.error('[memberOperate] error:', err)
  } finally {
    submitting.value = false
  }
}

const isSubRecord = (item) => {
  const type = String(item.type || item.operateType || item.operationType || item.recordType || item.action || item.title || '').toUpperCase()
  return type.includes('SUB') || type.includes('EXCHANGE') || type.includes('扣减') || Number(item.value || item.points || item.pointsAmount || item.operationValue || 0) < 0
}

const recordIcon = (item) => {
  if (isSubRecord(item)) return '↻'
  return '□'
}

const pointRecordTitle = (item) => {
  if (item.title) return item.title
  if (isSubRecord(item)) return '积分兑换筹码'
  return '筹码兑换积分'
}

const formatPointValue = (item) => {
  const raw = Number(item.value || item.points || item.pointsAmount || item.operationValue || item.amount || 0)
  const sign = isSubRecord(item) ? '-' : '+'
  return `${sign}${Math.abs(raw)}`
}

const parseRecordTime = (item) => {
  const value = item.createTime || item.time || item.createdAt || ''
  if (!value) return 0
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value.map(Number)
    return new Date(year, month - 1, day, hour, minute, second).getTime() || 0
  }
  return new Date(String(value).replace('T', ' ').replace(/-/g, '/')).getTime() || 0
}

const showMore = () => {
  uni.showToast({ title: '更多记录请到消费记录查看', icon: 'none' })
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
  padding: var(--status-bar-height) 22rpx 0;
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
  font-size: 36rpx;
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
  min-height: 180rpx;
  margin: 0 22rpx;
  padding: 28rpx 34rpx;
  border-radius: 14rpx 14rpx 0 0;
  border: 1rpx solid rgba(226, 187, 143, 0.72);
  border-bottom: 0;
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
  margin-top: 16rpx;
  color: #f5d8a8;
  font-size: 68rpx;
  font-weight: 900;
  line-height: 1;
}

.content {
  padding: 30rpx 26rpx 0;
  border-radius: 0;
  background: #fbf8f2;
}

.panel,
.operate-card,
.record-card {
  border-radius: 14rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 32rpx rgba(69, 43, 20, 0.07);
  border: 1rpx solid rgba(230, 216, 203, 0.66);
}

.panel {
  padding: 24rpx 26rpx 28rpx;
  margin-bottom: 24rpx;
}

.section-title {
  position: relative;
  padding-left: 26rpx;
  color: #050505;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 1.2;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4rpx;
  width: 7rpx;
  height: 32rpx;
  border-radius: 99rpx;
  background: linear-gradient(180deg, #dfb674 0%, #b1762a 100%);
}

.action-grid,
.amount-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 28rpx;
  margin-top: 28rpx;
}

.amount-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 18rpx;
}

.action-card,
.amount-btn {
  height: 88rpx;
  border-radius: 12rpx;
  border: 2rpx solid #d0a66f;
  color: #b88645;
  background: #fffdf9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18rpx;
  font-size: 29rpx;
  font-weight: 800;
  box-sizing: border-box;
}

.amount-btn {
  height: 64rpx;
  font-size: 25rpx;
}

.action-card.active,
.amount-btn.active {
  color: #f1cea0;
  background: linear-gradient(145deg, #27221b 0%, #030303 100%);
  box-shadow: 0 10rpx 22rpx rgba(48, 28, 8, 0.2);
}

.custom-input {
  display: flex;
  align-items: center;
  height: 76rpx;
  margin-top: 20rpx;
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

.action-symbol {
  font-size: 42rpx;
  line-height: 1;
}

.operate-card {
  overflow: hidden;
  margin-bottom: 28rpx;
}

.operate-row {
  display: grid;
  grid-template-columns: 58rpx 178rpx minmax(0, 1fr) 22rpx;
  align-items: center;
  gap: 20rpx;
  min-height: 88rpx;
  padding: 18rpx 26rpx;
  box-sizing: border-box;
}

.row-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  color: #b88645;
  background: #f5eadc;
  display: flex;
  align-items: center;
  justify-content: center;
}

.row-label {
  color: #17120d;
  font-size: 27rpx;
}

.row-value {
  color: #050505;
  font-size: 36rpx;
  font-weight: 900;
  text-align: right;
}

.remark-input {
  min-width: 0;
  height: 68rpx;
  color: #17120d;
  font-size: 27rpx;
}

.operate-divider {
  height: 1rpx;
  margin-left: 104rpx;
  background: #eee7df;
}

.chevron {
  width: 17rpx;
  height: 17rpx;
  border-top: 4rpx solid #bda890;
  border-right: 4rpx solid #bda890;
  transform: rotate(45deg);
  box-sizing: border-box;
}

.confirm-btn {
  height: 86rpx;
  margin-bottom: 28rpx;
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

.record-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10rpx 0 16rpx;
}

.more-link {
  color: #9d8061;
  font-size: 25rpx;
}

.more-link text {
  margin-left: 8rpx;
  font-size: 30rpx;
}

.record-card {
  overflow: hidden;
}

.record-row {
  display: grid;
  grid-template-columns: 58rpx minmax(0, 1fr) 136rpx 190rpx 22rpx;
  align-items: center;
  gap: 16rpx;
  min-height: 78rpx;
  padding: 14rpx 26rpx;
  border-bottom: 1rpx solid #eee7df;
  box-sizing: border-box;
}

.record-row:last-child {
  border-bottom: 0;
}

.record-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  color: #b88645;
  background: #f5eadc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.record-type {
  color: #17120d;
  font-size: 26rpx;
}

.record-value {
  color: #b88645;
  font-size: 29rpx;
  font-weight: 900;
  text-align: right;
}

.record-value.sub {
  color: #b01e1a;
}

.record-time {
  color: #99938d;
  font-size: 23rpx;
  text-align: right;
}

.small {
  width: 14rpx;
  height: 14rpx;
}

.empty-record {
  height: 108rpx;
  color: #99938d;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.tip-card {
  display: grid;
  grid-template-columns: 58rpx minmax(0, 1fr);
  align-items: center;
  gap: 20rpx;
  min-height: 82rpx;
  margin-top: 26rpx;
  padding: 18rpx 26rpx;
  border-radius: 14rpx;
  color: #4d4036;
  background: linear-gradient(100deg, #f3e6d5 0%, #fffaf2 100%);
  box-sizing: border-box;
  font-size: 25rpx;
}

.tip-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  color: #b18448;
  background: #f7eddf;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
