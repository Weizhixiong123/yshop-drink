<script setup>
import { computed, onMounted, ref } from 'vue'
import { exportAccount, fetchAccountStat } from '../api/member.js'

const loading = ref(false)
const errorMsg = ref('')
const stat = ref(null)
const date = ref(new Date().toISOString().slice(0, 10))

const records = computed(() => stat.value?.records || [])

const typeLabel = (type) => ({
  member_recharge: '会员充值',
  group_buy_wine: '团购营收',
  package_consume: '会员套餐',
  points_exchange_chips: '积分兑码',
  gift_chips: '店长赠码'
}[type] || type)

const money = (value) => Number(value || 0).toFixed(2)

const loadStat = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const response = await fetchAccountStat({ date: date.value })
    const result = response.data
    if (result?.code !== undefined && Number(result.code) !== 200) {
      throw new Error(result?.msg || '对账数据加载失败')
    }
    stat.value = result?.data || result
  } catch (error) {
    errorMsg.value = error?.response?.data?.msg || error?.message || '对账数据加载失败'
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  try {
    await exportAccount(date.value)
  } catch (error) {
    errorMsg.value = error?.message || '导出失败'
  }
}

onMounted(loadStat)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">门店对账统计</h2>
      <p class="page-desc">营收、码量、积分消耗按流水口径汇总</p>
    </div>

    <div class="card-panel account-panel">
      <form class="filter-bar" @submit.prevent="loadStat">
        <input v-model="date" type="date" />
        <button class="btn-solid filter-btn" type="submit" :disabled="loading">
          {{ loading ? '查询中...' : '查询' }}
        </button>
        <button class="btn-outline filter-btn" type="button" @click="handleExport">导出表单</button>
      </form>

      <div v-if="errorMsg" class="page-error">{{ errorMsg }}</div>

      <div class="summary-grid" v-if="stat">
        <div class="summary-card">
          <span>当日总营收</span>
          <strong>¥{{ money(stat.revenue?.total) }}</strong>
          <small>充值 ¥{{ money(stat.revenue?.recharge) }} / 团购 ¥{{ money(stat.revenue?.groupBuy) }}</small>
        </div>
        <div class="summary-card">
          <span>当日总发放码量</span>
          <strong>{{ stat.chips?.total || 0 }}千</strong>
          <small>套餐{{ stat.chips?.packageCash || 0 }} / 积分{{ stat.chips?.pointsExchange || 0 }} / 团购{{ stat.chips?.groupBuy || 0 }} / 赠送{{ stat.chips?.gift || 0 }}</small>
        </div>
        <div class="summary-card">
          <span>当日总扣积分</span>
          <strong>{{ stat.points?.total || 0 }}</strong>
          <small>固定规则{{ stat.points?.fixed || 0 }} / 自由零散{{ stat.points?.free || 0 }}</small>
        </div>
      </div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>时间</th>
              <th>类型</th>
              <th>操作人</th>
              <th>会员</th>
              <th>金额</th>
              <th>本金</th>
              <th>赠送</th>
              <th>积分</th>
              <th>酒水</th>
              <th>码量(千)</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="11" class="empty-row">加载中...</td>
            </tr>
            <tr v-else-if="!records.length">
              <td colspan="11" class="empty-row">暂无对账流水</td>
            </tr>
            <tr v-for="r in records" :key="r.id">
              <td>{{ r.createTime?.replace('T', ' ') }}</td>
              <td>{{ typeLabel(r.recordType) }}</td>
              <td>{{ r.staffName }}</td>
              <td>{{ r.memberName || '--' }}</td>
              <td>¥{{ money(r.amount) }}</td>
              <td>¥{{ money(r.principalAmount) }}</td>
              <td>¥{{ money(r.bonusAmount) }}</td>
              <td>{{ r.pointsAmount || 0 }}</td>
              <td>{{ r.wineQuantity || 0 }}</td>
              <td>{{ r.chipAmount || 0 }}</td>
              <td class="remark-cell">{{ r.remark || '--' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container { display: flex; flex-direction: column; height: 100%; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: bold; color: var(--text-main); }
.page-desc { color: var(--text-muted); font-size: 14px; margin-top: 5px; }
.account-panel { padding: 30px; flex-grow: 1; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 18px; }
.filter-bar input { height: 42px; padding: 0 12px; background: #f7f7f7; border: 1px solid #e5e5e5; border-radius: 8px; color: var(--text-main); }
.filter-btn { min-width: 94px; height: 42px; padding: 0 16px; }
.btn-outline { background: #fff; border: 1px solid #ddd; border-radius: 8px; color: #333; cursor: pointer; font-weight: bold; }
.page-error { margin-bottom: 16px; padding: 12px 16px; border-radius: 8px; background: rgba(193, 39, 45, 0.1); color: var(--tx-red); font-size: 14px; }
.summary-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 18px; }
.summary-card { border: 1px solid #ececec; border-radius: 8px; padding: 18px; background: #fafafa; display: flex; flex-direction: column; gap: 8px; }
.summary-card span { color: #666; font-size: 13px; font-weight: bold; }
.summary-card strong { color: #111; font-size: 28px; line-height: 1; }
.summary-card small { color: #888; line-height: 1.5; }
.table-responsive { width: 100%; overflow-x: auto; }
.data-table { width: 100%; min-width: 1100px; border-collapse: collapse; text-align: left; }
.data-table th { padding: 14px 12px; color: #111; font-weight: bold; border-bottom: 2px solid #eee; font-size: 14px; }
.data-table td { padding: 14px 12px; color: #333; border-bottom: 1px solid #f0f0f0; vertical-align: middle; font-size: 14px; white-space: nowrap; }
.empty-row { text-align: center; color: var(--text-muted); padding: 32px 12px !important; }
.remark-cell { max-width: 220px; overflow: hidden; text-overflow: ellipsis; }
@media (max-width: 768px) {
  .account-panel { padding: 15px; background: transparent; box-shadow: none; }
  .filter-bar { display: grid; grid-template-columns: 1fr 1fr; }
  .summary-grid { grid-template-columns: 1fr; }
}
</style>
