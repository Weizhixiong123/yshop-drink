<template>
  <view class="page-wrap" v-if="member">
    <view class="hero">
      <view class="custom-nav">
        <view class="nav-back" @tap="goBack"><view class="back-arrow"></view></view>
        <view class="nav-title">消费记录</view>
      </view>

      <view class="member-card" @tap="showBalance">
        <image src="/static/texas_bar_bg.png" mode="aspectFill" class="member-card-bg" />
        <view class="member-card-mask"></view>
        <view class="avatar">{{ member.name ? member.name[0] : 'V' }}</view>
        <view class="member-info">
          <view class="member-name">{{ member.name }}</view>
          <view class="member-phone">{{ getDisplayPhone() }}</view>
          <view class="level-tag"><text class="tag-gem">◆</text><text>{{ levelLabel(member.level) }}</text></view>
        </view>
        <view class="card-divider"></view>
        <view class="balance-total">
          <view class="total-label">储值余额</view>
          <view class="total-value">¥{{ member.balance }}</view>
        </view>
        <view class="chevron hero-chevron"></view>
      </view>
    </view>

    <view class="content">
      <view class="summary-grid">
        <view class="summary-item">
          <view class="summary-label">当前本金</view>
          <view class="summary-value">{{ moneyText(member.principalBalance) }}</view>
        </view>
        <view class="summary-item">
          <view class="summary-label">当前赠送</view>
          <view class="summary-value">{{ moneyText(member.bonusBalance) }}</view>
        </view>
        <view class="summary-item">
          <view class="summary-label">套餐消费</view>
          <view class="summary-value">{{ moneyText(summary.packageAmount) }}</view>
        </view>
        <view class="summary-item">
          <view class="summary-label">酒水变动</view>
          <view class="summary-value">{{ summary.wine }}瓶</view>
        </view>
      </view>

      <view class="tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @tap="activeTab = tab.key"
        >{{ tab.label }}</view>
      </view>

      <view class="record-heading">
        <view class="section-title">{{ activeTabLabel }}记录</view>
        <view class="record-count">共 {{ filteredRecords.length }} 条记录</view>
      </view>

      <view class="record-card">
        <view v-if="filteredRecords.length === 0" class="empty-record">暂无消费记录</view>
        <view v-for="item in filteredRecords" :key="item.key" class="record-row" @tap="openRecordDetail(item)">
          <view class="record-icon" :class="item.category">{{ item.icon }}</view>
          <view class="record-copy">
            <view class="record-title">{{ item.title }}</view>
            <view class="record-desc">{{ item.desc }}</view>
            <view class="record-remark">{{ item.remark || item.staffName || '—' }}</view>
          </view>
          <view class="record-side">
            <view class="record-value" :class="{ negative: item.negative, positive: item.positive }">{{ item.valueText }}</view>
            <view class="record-time">{{ item.timeText }}</view>
          </view>
          <view class="chevron"></view>
        </view>
      </view>
    </view>

    <view class="modal-mask" v-if="selectedRecord" @tap="closeRecordDetail"></view>
    <view class="record-modal" v-if="selectedRecord">
      <view class="modal-title">{{ selectedRecord.title }}</view>
      <view class="detail-list">
        <view class="detail-row" v-for="row in selectedRecordRows" :key="row.label">
          <text>{{ row.label }}</text>
          <text>{{ row.value }}</text>
        </view>
      </view>
      <view class="modal-btn" @tap="closeRecordDetail">知道了</view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { memberDetail } from '@/api/staff'

const member = ref(null)
const memberId = ref('')
const activeTab = ref('all')
const selectedRecord = ref(null)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'recharge', label: '充值' },
  { key: 'package', label: '套餐' },
  { key: 'wine', label: '存酒' },
  { key: 'points', label: '积分' },
  { key: 'balance', label: '扣减' }
]

const activeTabLabel = computed(() => tabs.find(item => item.key === activeTab.value)?.label || '全部')

const records = computed(() => {
  const businessRecords = (member.value?.businessRecords || []).map(normalizeBusinessRecord)
  const operationRecords = (member.value?.operationRecords || []).map(normalizeOperationRecord)
  return businessRecords
    .concat(operationRecords)
    .filter(Boolean)
    .sort((a, b) => b.timestamp - a.timestamp)
    .slice(0, 120)
})

const filteredRecords = computed(() => {
  if (activeTab.value === 'all') return records.value
  return records.value.filter(item => item.category === activeTab.value)
})

const summary = computed(() => {
  return records.value.reduce((acc, item) => {
    if (item.source === 'business' && item.recordType === 'member_recharge') {
      acc.recharge += Number(item.principalAmount || 0)
    }
    if (item.source === 'business' && item.recordType === 'package_consume') {
      acc.packageAmount += Number(item.amount || 0)
    }
    acc.wine += Number(item.wineQuantity || 0)
    return acc
  }, { recharge: 0, packageAmount: 0, wine: 0 })
})

const selectedRecordRows = computed(() => {
  const item = selectedRecord.value
  if (!item) return []
  return [
    { label: '记录时间', value: item.timeText || '—' },
    { label: '操作员工', value: item.staffName || '—' },
    { label: '流水类型', value: item.title },
    { label: '金额', value: moneyOptional(item.amount) },
    { label: '本金', value: moneyOptional(item.principalAmount) },
    { label: '赠送', value: moneyOptional(item.bonusAmount) },
    { label: '积分', value: countText(item.pointsAmount, '分') },
    { label: '酒水', value: countText(item.wineQuantity, '瓶') },
    { label: '码量', value: countText(item.chipAmount, '千') },
    { label: '档位', value: item.packageCode || '—' },
    { label: '备注', value: item.remark || '—' }
  ].filter(row => row.value !== '—' || ['记录时间', '操作员工', '流水类型', '备注'].includes(row.label))
})

onLoad((options) => {
  memberId.value = options.id || ''
  if (memberId.value) loadMember(memberId.value)
})

const loadMember = async (id) => {
  try {
    const data = await memberDetail(id)
    if (data) {
      member.value = {
        ...data,
        balance: data.balance != null ? Number(data.balance).toFixed(2) : '0.00',
        principalBalance: data.principalBalance != null ? Number(data.principalBalance).toFixed(2) : '0.00',
        bonusBalance: data.bonusBalance != null ? Number(data.bonusBalance).toFixed(2) : '0.00',
        level: data.level || 'normal'
      }
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

const showBalance = () => {
  uni.showToast({ title: `本金¥${member.value?.principalBalance || '0.00'} / 赠送¥${member.value?.bonusBalance || '0.00'}`, icon: 'none' })
}

const normalizeBusinessRecord = (item) => {
  const recordType = String(item.recordType || item.type || '').toLowerCase()
  if (recordType === 'group_buy_wine') {
    return null
  }
  if (recordType === 'package_consume' && String(item.remark || '').includes('余额不足')) {
    return null
  }
  const base = baseRecord(item, 'business')
  const amount = Number(item.amount || 0)
  const principalAmount = Number(item.principalAmount || 0)
  const bonusAmount = Number(item.bonusAmount || 0)
  const pointsAmount = Number(item.pointsAmount || 0)
  const wineQuantity = Number(item.wineQuantity || 0)
  const chipAmount = Number(item.chipAmount || 0)

  const titleMap = {
    member_recharge: '店长储值充值',
    package_consume: '快捷消费套餐',
    points_exchange_chips: '积分兑换'
  }

  const categoryMap = {
    member_recharge: 'recharge',
    package_consume: 'package',
    points_exchange_chips: 'points'
  }

  const category = categoryMap[recordType] || 'balance'
  const title = titleMap[recordType] || '业务流水'
  let desc = item.remark || item.packageCode || '—'
  let valueText = moneyText(amount)
  let negative = false
  let positive = false

  if (recordType === 'member_recharge') {
    desc = `本金${moneyText(principalAmount)} / 赠送${moneyText(bonusAmount)}`
    valueText = `+${moneyText(principalAmount)}`
    positive = true
  } else if (recordType === 'package_consume') {
    desc = `酒水+${wineQuantity}瓶 / 码量${chipAmount}千`
    valueText = `-${moneyText(amount)}`
    negative = true
  } else if (recordType === 'points_exchange_chips') {
    desc = `扣${pointsAmount}积分 / 码量${chipAmount}千`
    valueText = `-${pointsAmount}分`
    negative = true
  }

  return {
    ...base,
    recordType,
    category,
    title,
    desc,
    valueText,
    negative,
    positive,
    icon: iconFor(category),
    amount,
    principalAmount,
    bonusAmount,
    pointsAmount,
    wineQuantity,
    chipAmount,
    packageCode: item.packageCode || ''
  }
}

const normalizeOperationRecord = (item) => {
  const operationType = String(item.operationType || item.type || '').toLowerCase()
  const value = Number(item.operationValue || item.value || 0)
  const beforeValue = Number(item.beforeValue || 0)
  const afterValue = Number(item.afterValue || 0)
  const isSub = operationType.startsWith('sub_')
  const target = operationType.replace(/^add_/, '').replace(/^sub_/, '')
  const category = target === 'wine' ? 'wine' : target === 'points' ? 'points' : 'balance'
  const unit = target === 'wine' ? '瓶' : target === 'points' ? '分' : '元'
  const titleMap = {
    add_wine: '存入酒水',
    sub_wine: '取出酒水',
    add_points: '筹码兑换积分',
    sub_points: '积分兑换筹码',
    add_balance: '手动增加余额',
    sub_balance: '消费扣减'
  }
  const amount = target === 'balance' ? value : 0
  const pointsAmount = target === 'points' ? value : 0
  const wineQuantity = target === 'wine' ? (isSub ? -value : value) : 0
  return {
    ...baseRecord(item, 'operation'),
    operationType,
    category,
    title: titleMap[operationType] || '手动操作',
    desc: `变更前 ${formatPlain(beforeValue)}，变更后 ${formatPlain(afterValue)}`,
    valueText: `${isSub ? '-' : '+'}${formatPlain(value)}${unit}`,
    negative: isSub,
    positive: !isSub,
    icon: iconFor(category),
    amount,
    principalAmount: 0,
    bonusAmount: 0,
    pointsAmount: isSub ? -pointsAmount : pointsAmount,
    wineQuantity,
    chipAmount: 0,
    packageCode: ''
  }
}

const baseRecord = (item, source) => {
  const time = item.createTime || item.time || item.createdAt || ''
  return {
    key: `${source}-${item.id || time}`,
    source,
    id: item.id,
    staffName: item.staffName || item.operator || item.operatorName || '',
    remark: item.remark || '',
    createTime: time,
    timestamp: parseRecordTime(time),
    timeText: formatTime(time)
  }
}

const iconFor = (category) => ({
  recharge: '¥',
  package: '♧',
  wine: '♢',
  points: '☆',
  balance: '扣'
}[category] || '•')

const moneyText = (value) => `¥${Number(value || 0).toFixed(2)}`
const moneyOptional = (value) => Number(value || 0) ? moneyText(value) : '—'
const countText = (value, unit) => Number(value || 0) ? `${formatPlain(value)}${unit}` : '—'
const formatPlain = (value) => Number(value || 0).toString().replace(/\.0+$/, '')

const parseRecordTime = (value) => {
  if (!value) return 0
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value.map(Number)
    return new Date(year, month - 1, day, hour, minute, second).getTime() || 0
  }
  const normalized = String(value).replace('T', ' ').replace(/-/g, '/')
  return new Date(normalized).getTime() || 0
}

const formatTime = (value) => {
  if (!value) return ''
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0] = value.map(Number)
    return `${year}-${padTime(month)}-${padTime(day)} ${padTime(hour)}:${padTime(minute)}`
  }
  const text = String(value).replace('T', ' ')
  return text.length > 16 ? text.slice(0, 16) : text
}

const padTime = (value) => String(value).padStart(2, '0')

const openRecordDetail = (item) => {
  selectedRecord.value = item
}

const closeRecordDetail = () => {
  selectedRecord.value = null
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
  padding: var(--status-bar-height) 22rpx 28rpx;
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
  grid-template-columns: 116rpx minmax(0, 1fr) 1rpx 198rpx 22rpx;
  align-items: center;
  gap: 24rpx;
  min-height: 180rpx;
  padding: 28rpx 30rpx;
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
.balance-total,
.hero-chevron {
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

.total-label {
  color: #e2c69d;
  font-size: 30rpx;
  font-weight: 700;
}

.total-value {
  margin-top: 18rpx;
  color: #f5d8a8;
  font-size: 46rpx;
  font-weight: 900;
  white-space: nowrap;
}

.chevron {
  width: 17rpx;
  height: 17rpx;
  border-top: 4rpx solid #bda890;
  border-right: 4rpx solid #bda890;
  transform: rotate(45deg);
  box-sizing: border-box;
}

.hero-chevron {
  border-color: #d9bf9d;
}

.content {
  padding: 30rpx 26rpx 0;
  border-radius: 22rpx 22rpx 0 0;
  background: #fbf8f2;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-bottom: 26rpx;
}

.summary-item:last-child:nth-child(odd) {
  grid-column: span 2;
}

.summary-item {
  min-height: 112rpx;
  padding: 20rpx 22rpx;
  border-radius: 14rpx;
  background: #ffffff;
  border: 1rpx solid rgba(230, 216, 203, 0.7);
  box-shadow: 0 10rpx 28rpx rgba(69, 43, 20, 0.06);
  box-sizing: border-box;
}

.summary-label {
  color: #8d8074;
  font-size: 24rpx;
  line-height: 1.2;
}

.summary-value {
  margin-top: 14rpx;
  color: #111111;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14rpx;
}

.tab {
  height: 62rpx;
  border-radius: 32rpx;
  border: 1rpx solid #eadccb;
  color: #322a24;
  background: #fffdf9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.tab.active {
  color: #ffffff;
  background: linear-gradient(145deg, #d6a565 0%, #b97b36 100%);
  border-color: transparent;
  font-weight: 900;
}

.record-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 40rpx 0 18rpx;
}

.section-title {
  position: relative;
  padding-left: 26rpx;
  color: #050505;
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

.record-count {
  color: #a09a94;
  font-size: 26rpx;
}

.record-card {
  overflow: hidden;
  border-radius: 14rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 32rpx rgba(69, 43, 20, 0.07);
  border: 1rpx solid rgba(230, 216, 203, 0.66);
}

.record-row {
  display: grid;
  grid-template-columns: 92rpx minmax(0, 1fr) 210rpx 20rpx;
  align-items: center;
  gap: 18rpx;
  min-height: 132rpx;
  padding: 22rpx 26rpx;
  border-bottom: 1rpx solid #eee7df;
  box-sizing: border-box;
}

.record-row:active {
  background: #fffaf4;
}

.record-row:last-child {
  border-bottom: 0;
}

.record-icon {
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  color: #b88645;
  background: #f5eadc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 38rpx;
  font-weight: 900;
}

.record-icon.recharge {
  color: #0f7a3b;
  background: #e5f4ea;
}

.record-icon.package {
  color: #9a5d13;
  background: #f7ead8;
}


.record-icon.wine {
  color: #7a4b9d;
  background: #f1e7f8;
}

.record-icon.points {
  color: #a78018;
  background: #f8f0d6;
}

.record-icon.balance {
  color: #9f201c;
  background: #f7e3e0;
  font-size: 30rpx;
}

.record-title {
  color: #050505;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.2;
}

.record-desc,
.record-remark {
  margin-top: 10rpx;
  color: #6e6964;
  font-size: 25rpx;
  line-height: 1.2;
}

.record-side {
  text-align: right;
}

.record-value {
  color: #050505;
  font-size: 30rpx;
  font-weight: 900;
}

.record-value.negative {
  color: #a91410;
}

.record-value.positive {
  color: #197a36;
}

.record-time {
  margin-top: 18rpx;
  color: #77716c;
  font-size: 25rpx;
}

.empty-record {
  height: 160rpx;
  color: #99938d;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
}

.modal-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.58);
}

.record-modal {
  position: fixed;
  left: 50%;
  top: 50%;
  z-index: 101;
  width: 650rpx;
  max-width: calc(100vw - 56rpx);
  max-height: 78vh;
  transform: translate(-50%, -50%);
  border-radius: 22rpx;
  background: #ffffff;
  overflow: hidden;
  box-shadow: 0 18rpx 56rpx rgba(0, 0, 0, 0.22);
}

.modal-title {
  padding: 34rpx 34rpx 22rpx;
  color: #14100d;
  font-size: 32rpx;
  font-weight: 900;
  text-align: center;
}

.detail-list {
  max-height: 54vh;
  padding: 0 34rpx;
  overflow-y: auto;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  padding: 20rpx 0;
  border-top: 1rpx solid #eee7df;
  color: #241d17;
  font-size: 27rpx;
  line-height: 1.35;
}

.detail-row text:first-child {
  flex: 0 0 150rpx;
  color: #8c8075;
}

.detail-row text:last-child {
  flex: 1;
  text-align: right;
  word-break: break-all;
}

.modal-btn {
  height: 96rpx;
  margin-top: 16rpx;
  border-top: 1rpx solid #eee7df;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #111111;
  font-size: 30rpx;
  font-weight: 900;
  background: #fffaf4;
}
</style>
